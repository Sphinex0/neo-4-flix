import { Component, OnInit, inject, signal } from '@angular/core';
import { NgStyle } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { Navbar } from '../../../../shared/navbar/navbar';
import { MovieResponse } from '../../../movies/models/movie.models';
import { MovieApiService } from '../../../movies/services/movie-api.service';
import { OtherUserRatingItem, SharedRatingItem } from '../../../ratings/models/rating.models';
import { RatingApiService } from '../../../ratings/services/rating-api.service';
import { WatchlistApiService } from '../../../watchlist/services/watchlist-api.service';
import { UserApiService } from '../../services/user-api.service';

interface SharedMovie {
  title: string;
  mine: number;
  theirs: number;
  bg: string;
}

interface RecentRatedMovie {
  id: string;
  title: string;
  meta: string;
  theirs: number;
  when: string;
  bg: string;
}

const FALLBACK_GRADIENTS = [
  'linear-gradient(160deg,#6A4423,#120B06)',
  'linear-gradient(160deg,#454C78,#0C0E18)',
  'linear-gradient(160deg,#6E3560,#150A13)',
  'linear-gradient(160deg,#5A4128,#0E0A06)',
  'linear-gradient(160deg,#256270,#08161A)',
];

@Component({
  selector: 'app-user-profile',
  imports: [Navbar, RouterLink, NgStyle],
  templateUrl: './user-profile.html',
})
export class UserProfile implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly userApi = inject(UserApiService);
  private readonly ratingApi = inject(RatingApiService);
  private readonly movieApi = inject(MovieApiService);
  private readonly watchlistApi = inject(WatchlistApiService);

  isLoading = signal(true);
  loadError = signal<string | null>(null);

  isComparisonLoading = signal(true);
  comparisonError = signal<string | null>(null);

  isFriend = signal(false);
  isFriendActionLoading = signal(false);

  private userId = '';

  initials = signal('?');
  firstName = signal('');
  fullName = signal('');
  username = signal('');
  memberSince = signal('');
  ratingsCount = signal(0);
  averageRating = signal<number | null>(null);

  bothRatedCount = signal(0);
  tasteMatchPercent = signal<number | null>(null);
  bothLoved = signal<SharedMovie[]>([]);
  recentRatings = signal<RecentRatedMovie[]>([]);

  addedWatchlistIds = signal<Set<string>>(new Set());

  ngOnInit(): void {
    this.route.paramMap.subscribe((params) => {
      const userId = params.get('userId');
      if (!userId) {
        this.isLoading.set(false);
        this.loadError.set('No user id in the URL.');
        return;
      }

      this.loadProfile(userId);
    });
  }

  private loadProfile(userId: string): void {
    this.userId = userId;

    this.isLoading.set(true);
    this.loadError.set(null);
    this.isComparisonLoading.set(true);
    this.comparisonError.set(null);
    this.bothLoved.set([]);
    this.recentRatings.set([]);
    this.addedWatchlistIds.set(new Set());

    this.userApi.getUserById(userId).subscribe({
      next: (res) => {
        const user = res.data;
        this.firstName.set(user.firstName);
        this.fullName.set(`${user.firstName} ${user.lastName}`.trim() || user.username);

        const first = user.firstName.trim();
        const last = user.lastName.trim();
        const computed = `${first[0] ?? ''}${last[0] ?? ''}`.toUpperCase();
        this.initials.set(computed || (user.username[0] ?? '?').toUpperCase());

        this.username.set(user.username);
        this.memberSince.set(
          new Date(user.createdAt).toLocaleDateString('en-US', {
            month: 'long',
            year: 'numeric',
          }),
        );
        this.isFriend.set(user.isFriend);

        this.isLoading.set(false);
      },
      error: (err) => {
        this.isLoading.set(false);
        this.loadError.set(err.error?.message ?? 'Could not load this profile.');
      },
    });

    this.ratingApi.getUserRatingStats(userId).subscribe({
      next: (res) => {
        this.ratingsCount.set(res.data.ratingsCount);
        this.averageRating.set(res.data.averageRating);
      },
    });

    this.ratingApi.getComparison(userId).subscribe({
      next: (res) => {
        this.bothRatedCount.set(res.data.bothRatedCount);
        this.tasteMatchPercent.set(res.data.tasteMatchPercent);
        this.loadBothLoved(res.data.bothLoved);
        this.loadRecentRatings(res.data.recentRatings);
      },
      error: (err) => {
        this.isComparisonLoading.set(false);
        this.comparisonError.set(err.error?.message ?? 'Could not load the comparison with this user.');
      },
    });
  }

  private loadBothLoved(items: SharedRatingItem[]): void {
    if (items.length === 0) {
      this.bothLoved.set([]);
      this.isComparisonLoading.set(false);
      return;
    }

    forkJoin(items.map((item) => this.movieApi.getMovieById(item.movieId))).subscribe({
      next: (movieResponses) => {
        this.bothLoved.set(
          items.map((item, index) => ({
            title: movieResponses[index].data.title,
            mine: item.myScore,
            theirs: item.theirScore,
            bg: this.posterOrFallback(movieResponses[index].data, index),
          })),
        );
        this.isComparisonLoading.set(false);
      },
      error: () => {
        this.isComparisonLoading.set(false);
      },
    });
  }

  private loadRecentRatings(items: OtherUserRatingItem[]): void {
    if (items.length === 0) {
      this.recentRatings.set([]);
      return;
    }

    forkJoin(items.map((item) => this.movieApi.getMovieById(item.movieId))).subscribe({
      next: (movieResponses) => {
        this.recentRatings.set(
          items.map((item, index) => {
            const movie = movieResponses[index].data;
            const genreNames = movie.genres.map((g) => g.name).join(', ');

            return {
              id: movie.id,
              title: movie.title,
              meta: genreNames ? `${movie.releaseYear} · ${genreNames}` : `${movie.releaseYear}`,
              theirs: item.theirScore,
              when: this.relativeTime(item.ratedAt),
              bg: this.posterOrFallback(movie, index),
            };
          }),
        );
      },
    });
  }

  toggleFriend(): void {
    if (this.isFriendActionLoading()) {
      return;
    }

    this.isFriendActionLoading.set(true);

    if (this.isFriend()) {
      this.userApi.removeFriend(this.userId).subscribe({
        next: () => {
          this.isFriend.set(false);
          this.isFriendActionLoading.set(false);
        },
        error: () => {
          this.isFriendActionLoading.set(false);
        },
      });
    } else {
      this.userApi.addFriend(this.userId).subscribe({
        next: () => {
          this.isFriend.set(true);
          this.isFriendActionLoading.set(false);
        },
        error: () => {
          this.isFriendActionLoading.set(false);
        },
      });
    }
  }

  onAddToWatchlist(movie: RecentRatedMovie): void {
    this.watchlistApi.addToWatchlist(movie.id).subscribe({
      next: () => {
        this.addedWatchlistIds.update((set) => {
          const next = new Set(set);
          next.add(movie.id);
          return next;
        });
      },
    });
  }

  isAdded(id: string): boolean {
    return this.addedWatchlistIds().has(id);
  }

  private posterOrFallback(movie: MovieResponse, index: number): string {
    return movie.posterUrl
      ? `center / cover url('${movie.posterUrl}')`
      : FALLBACK_GRADIENTS[index % FALLBACK_GRADIENTS.length];
  }

  private relativeTime(iso: string): string {
    const days = Math.floor((Date.now() - new Date(iso).getTime()) / (1000 * 60 * 60 * 24));

    if (days <= 0) return 'today';
    if (days === 1) return 'yesterday';
    if (days < 7) return `${days} days ago`;
    if (days < 14) return 'last week';
    if (days < 30) return `${Math.floor(days / 7)} weeks ago`;
    return `${Math.floor(days / 30)} months ago`;
  }
}
