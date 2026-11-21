import { Component, OnInit, inject, signal } from '@angular/core';
import { NgStyle } from '@angular/common';
import { forkJoin } from 'rxjs';
import { Navbar } from '../../../../shared/navbar/navbar';
import { MovieResponse, SharedMovieResponse } from '../../../movies/models/movie.models';
import { MovieApiService } from '../../../movies/services/movie-api.service';
import { MyRatingResponse } from '../../../ratings/models/rating.models';
import { RatingApiService } from '../../../ratings/services/rating-api.service';
import { UserApiService } from '../../../users/services/user-api.service';
import { WatchlistApiService } from '../../services/watchlist-api.service';

interface WatchlistCard {
  id: string;
  title: string;
  meta: string;
  bg: string;
}

interface RatedCard {
  movieId: string;
  title: string;
  meta: string;
  mine: number;
  bg: string;
}

interface SharedCard {
  movieId: string;
  title: string;
  meta: string;
  bg: string;
  sharedByName: string;
  sharedWhen: string;
}

type Tab = 'watch' | 'ratings' | 'shared';

const FALLBACK_GRADIENTS = [
  'linear-gradient(160deg,#6A4423,#120B06)',
  'linear-gradient(160deg,#454C78,#0C0E18)',
  'linear-gradient(160deg,#6E3560,#150A13)',
  'linear-gradient(160deg,#5A4128,#0E0A06)',
  'linear-gradient(160deg,#256270,#08161A)',
];

@Component({
  selector: 'app-watchlist',
  imports: [Navbar, NgStyle],
  templateUrl: './watchlist.html',
})
export class Watchlist implements OnInit {
  private readonly watchlistApi = inject(WatchlistApiService);
  private readonly ratingApi = inject(RatingApiService);
  private readonly movieApi = inject(MovieApiService);
  private readonly userApi = inject(UserApiService);

  activeTab = signal<Tab>('watch');
  isLoading = signal(true);
  loadError = signal<string | null>(null);
  removeMessage = signal<string | null>(null);
  removingIds = signal<Set<string>>(new Set());

  isRatingsLoading = signal(true);
  ratingsError = signal<string | null>(null);
  deleteRatingMessage = signal<string | null>(null);

  ratingToDelete = signal<RatedCard | null>(null);
  isDeletingRating = signal(false);

  editingRating = signal<RatedCard | null>(null);
  editScore = signal(0);
  isSavingRating = signal(false);
  editRatingError = signal<string | null>(null);

  isSharedLoading = signal(true);
  sharedError = signal<string | null>(null);

  watch: WatchlistCard[] = [];
  rated: RatedCard[] = [];
  shared: SharedCard[] = [];

  ngOnInit(): void {
    this.loadWatchlist();
    this.loadMyRatings();
    this.loadShared();
  }

  private loadWatchlist(): void {
    this.watchlistApi.getWatchlist(0, 10).subscribe({
      next: (res) => {
        this.watch = res.data.content.map((movie, index) => this.toWatchlistCard(movie, index));
        this.isLoading.set(false);
      },
      error: (err) => {
        this.isLoading.set(false);
        this.loadError.set(err.error?.message ?? 'Could not load your watchlist from the server.');
      },
    });
  }

  private loadMyRatings(): void {
    this.ratingApi.getMyRatings().subscribe({
      next: (res) => {
        const ratings = res.data;

        if (ratings.length === 0) {
          this.rated = [];
          this.isRatingsLoading.set(false);
          return;
        }

        const movieCalls = ratings.map((rating) => this.movieApi.getMovieById(rating.movieId));
        forkJoin(movieCalls).subscribe({
          next: (movieResponses) => {
            this.rated = ratings.map((rating, index) =>
              this.toRatedCard(rating, movieResponses[index].data, index),
            );
            this.isRatingsLoading.set(false);
          },
          error: (err) => {
            this.isRatingsLoading.set(false);
            this.ratingsError.set(err.error?.message ?? 'Could not load movie details for your ratings.');
          },
        });
      },
      error: (err) => {
        this.isRatingsLoading.set(false);
        this.ratingsError.set(err.error?.message ?? 'Could not load your ratings from the server.');
      },
    });
  }

  private loadShared(): void {
    this.movieApi.getSharedWithMe().subscribe({
      next: (res) => {
        const rows = res.data ?? [];

        if (rows.length === 0) {
          this.shared = [];
          this.isSharedLoading.set(false);
          return;
        }

        const movieCalls = rows.map((row) => this.movieApi.getMovieById(row.movieId));
        const sharerIds = [...new Set(rows.map((row) => row.sharedBy))];
        const userCalls = sharerIds.map((id) => this.userApi.getUserById(id));

        forkJoin([forkJoin(movieCalls), forkJoin(userCalls)]).subscribe({
          next: ([movieResponses, userResponses]) => {
            const sharerNames = new Map(
              sharerIds.map((id, index) => {
                const user = userResponses[index].data;
                return [id, `${user.firstName} ${user.lastName}`.trim() || user.username];
              }),
            );

            this.shared = rows.map((row, index) =>
              this.toSharedCard(row, movieResponses[index].data, sharerNames.get(row.sharedBy) ?? 'Someone', index),
            );
            this.isSharedLoading.set(false);
          },
          error: (err) => {
            this.isSharedLoading.set(false);
            this.sharedError.set(err.error?.message ?? 'Could not load the movies shared with you.');
          },
        });
      },
      error: (err) => {
        this.isSharedLoading.set(false);
        this.sharedError.set(err.error?.message ?? 'Could not load the movies shared with you.');
      },
    });
  }

  onRemove(movie: WatchlistCard): void {
    if (this.isRemoving(movie.id)) {
      return;
    }

    this.removeMessage.set(null);
    this.removingIds.update((set) => new Set(set).add(movie.id));

    this.watchlistApi.removeFromWatchlist(movie.id).subscribe({
      next: () => {
        this.watch = this.watch.filter((m) => m.id !== movie.id);
        this.removingIds.update((set) => {
          const next = new Set(set);
          next.delete(movie.id);
          return next;
        });
      },
      error: (err) => {
        this.removingIds.update((set) => {
          const next = new Set(set);
          next.delete(movie.id);
          return next;
        });
        this.removeMessage.set(err.error?.message ?? 'Could not remove this movie.');
      },
    });
  }

  isRemoving(id: string): boolean {
    return this.removingIds().has(id);
  }

  askDeleteRating(movie: RatedCard): void {
    this.deleteRatingMessage.set(null);
    this.ratingToDelete.set(movie);
  }

  cancelDeleteRating(): void {
    this.ratingToDelete.set(null);
  }

  confirmDeleteRating(): void {
    const movie = this.ratingToDelete();
    if (!movie) {
      return;
    }

    this.isDeletingRating.set(true);

    this.ratingApi.deleteRating(movie.movieId).subscribe({
      next: () => {
        this.rated = this.rated.filter((m) => m.movieId !== movie.movieId);
        this.isDeletingRating.set(false);
        this.ratingToDelete.set(null);
      },
      error: (err) => {
        this.isDeletingRating.set(false);
        this.ratingToDelete.set(null);
        this.deleteRatingMessage.set(err.error?.message ?? 'Could not delete this rating.');
      },
    });
  }

  askChangeRating(movie: RatedCard): void {
    this.editRatingError.set(null);
    this.editScore.set(movie.mine);
    this.editingRating.set(movie);
  }

  cancelChangeRating(): void {
    this.editingRating.set(null);
  }

  setEditScore(score: number): void {
    this.editScore.set(score);
  }

  confirmChangeRating(): void {
    const movie = this.editingRating();
    if (!movie) {
      return;
    }

    const newScore = this.editScore();
    this.isSavingRating.set(true);
    this.editRatingError.set(null);

    this.ratingApi.changeRating(movie.movieId, { score: newScore }).subscribe({
      next: () => {
        this.rated = this.rated.map((m) => (m.movieId === movie.movieId ? { ...m, mine: newScore } : m));
        this.isSavingRating.set(false);
        this.editingRating.set(null);
      },
      error: (err) => {
        this.isSavingRating.set(false);
        this.editRatingError.set(err.error?.message ?? 'Could not update this rating.');
      },
    });
  }

  private toWatchlistCard(movie: MovieResponse, index: number): WatchlistCard {
    const genreNames = movie.genres.map((g) => g.name).join(', ');

    return {
      id: movie.id,
      title: movie.title,
      meta: genreNames ? `${movie.releaseYear} · ${genreNames}` : `${movie.releaseYear}`,
      bg: movie.posterUrl
        ? `center / cover url('${movie.posterUrl}')`
        : FALLBACK_GRADIENTS[index % FALLBACK_GRADIENTS.length],
    };
  }

  private toRatedCard(rating: MyRatingResponse, movie: MovieResponse, index: number): RatedCard {
    const genreNames = movie.genres.map((g) => g.name).join(', ');

    return {
      movieId: rating.movieId,
      title: movie.title,
      meta: genreNames ? `${movie.releaseYear} · ${genreNames}` : `${movie.releaseYear}`,
      mine: rating.score,
      bg: movie.posterUrl
        ? `center / cover url('${movie.posterUrl}')`
        : FALLBACK_GRADIENTS[index % FALLBACK_GRADIENTS.length],
    };
  }

  private toSharedCard(row: SharedMovieResponse, movie: MovieResponse, sharedByName: string, index: number): SharedCard {
    const genreNames = movie.genres.map((g) => g.name).join(', ');

    return {
      movieId: row.movieId,
      title: movie.title,
      meta: genreNames ? `${movie.releaseYear} · ${genreNames}` : `${movie.releaseYear}`,
      bg: movie.posterUrl
        ? `center / cover url('${movie.posterUrl}')`
        : FALLBACK_GRADIENTS[index % FALLBACK_GRADIENTS.length],
      sharedByName,
      sharedWhen: this.relativeTime(row.sharedAt),
    };
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

  showWatch(): void {
    this.activeTab.set('watch');
  }

  showRatings(): void {
    this.activeTab.set('ratings');
  }

  showShared(): void {
    this.activeTab.set('shared');
  }

  watchTabStyle(): Record<string, string> {
    const active = this.activeTab() === 'watch';
    return {
      background: active ? '#FF5A5F' : 'transparent',
      color: active ? '#0D1017' : '#C9D0DC',
      'border-color': active ? '#FF5A5F' : '#5B6579',
    };
  }

  ratingsTabStyle(): Record<string, string> {
    const active = this.activeTab() === 'ratings';
    return {
      background: active ? '#FF5A5F' : 'transparent',
      color: active ? '#0D1017' : '#C9D0DC',
      'border-color': active ? '#FF5A5F' : '#5B6579',
    };
  }

  sharedTabStyle(): Record<string, string> {
    const active = this.activeTab() === 'shared';
    return {
      background: active ? '#FF5A5F' : 'transparent',
      color: active ? '#0D1017' : '#C9D0DC',
      'border-color': active ? '#FF5A5F' : '#5B6579',
    };
  }
}
