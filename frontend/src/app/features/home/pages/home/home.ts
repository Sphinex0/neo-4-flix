import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { NgStyle } from '@angular/common';
import { forkJoin } from 'rxjs';
import { Navbar } from '../../../../shared/navbar/navbar';
import { MovieResponse } from '../../../movies/models/movie.models';
import { MovieApiService } from '../../../movies/services/movie-api.service';
import { RecommendationApiService } from '../../../recommendations/services/recommendation-api.service';
import { WatchlistApiService } from '../../../watchlist/services/watchlist-api.service';
import { RatingApiService } from '../../../ratings/services/rating-api.service';
import { UserApiService } from '../../../users/services/user-api.service';
import { ShareMovieModal } from '../../../movies/components/share-movie-modal/share-movie-modal';

export interface HomeMovieCard {
  id: string;
  title: string;
  meta: string;
  bg: string;
  imdbRating: number | null;
  reason?: string;
  sharedBy?: string;
  raw: MovieResponse;
}

const FALLBACK_GRADIENTS = [
  'linear-gradient(160deg,#6A4423,#120B06)',
  'linear-gradient(160deg,#454C78,#0C0E18)',
  'linear-gradient(160deg,#6E3560,#150A13)',
  'linear-gradient(160deg,#5A4128,#0E0A06)',
  'linear-gradient(160deg,#256270,#08161A)',
];

@Component({
  selector: 'app-home',
  imports: [Navbar, RouterLink, NgStyle, ShareMovieModal],
  templateUrl: './home.html',
})
export class Home implements OnInit {
  private readonly movieApi = inject(MovieApiService);
  private readonly recApi = inject(RecommendationApiService);
  private readonly watchlistApi = inject(WatchlistApiService);
  private readonly ratingApi = inject(RatingApiService);
  private readonly userApi = inject(UserApiService);

  isLoading = signal(true);
  isAdmin = signal(false);

  // Spotlight / Hero Movie
  heroMovie = signal<MovieResponse | null>(null);

  // Movie Rows
  recommendedMovies = signal<HomeMovieCard[]>([]);
  trendingMovies = signal<HomeMovieCard[]>([]);
  sharedWithMeMovies = signal<HomeMovieCard[]>([]);
  actionMovies = signal<HomeMovieCard[]>([]);
  dramaMovies = signal<HomeMovieCard[]>([]);

  // Watchlist & Ratings State
  addedWatchlistIds = signal<Set<string>>(new Set());
  myRatings = signal<Map<string, number>>(new Map());
  actionMessage = signal<string | null>(null);

  // Modals State
  selectedMovie = signal<MovieResponse | null>(null);
  sharingMovie = signal<MovieResponse | null>(null);
  modalRating = signal<number>(0);
  isRatingSaving = signal<boolean>(false);

  ngOnInit(): void {
    this.checkAdmin();
    this.loadMyRatings();
    this.loadWatchlistIds();
    this.loadAllContent();
  }

  private checkAdmin(): void {
    this.userApi.getMe().subscribe({
      next: (res) => {
        this.isAdmin.set(res.data.role === 'ADMIN');
      },
      error: () => {},
    });
  }

  private loadMyRatings(): void {
    this.ratingApi.getMyRatings().subscribe({
      next: (res) => {
        const map = new Map<string, number>();
        for (const r of res.data ?? []) {
          map.set(r.movieId, r.score);
        }
        this.myRatings.set(map);
      },
      error: () => {},
    });
  }

  private loadWatchlistIds(): void {
    this.watchlistApi.getWatchlist(0, 50).subscribe({
      next: (res) => {
        const set = new Set<string>();
        for (const m of res.data?.content ?? []) {
          set.add(m.id);
        }
        this.addedWatchlistIds.set(set);
      },
      error: () => {},
    });
  }

  loadAllContent(): void {
    this.isLoading.set(true);

    // Fetch Recommended, Trending, Action, Drama, and Shared in parallel
    const recommended$ = this.recApi.getRecommendations(null, 12);
    const trending$ = this.recApi.getTrending(null, 12);
    const action$ = this.movieApi.searchMovies({ genre: 'Action', size: 12, sortBy: 'year', direction: 'desc' });
    const drama$ = this.movieApi.searchMovies({ genre: 'Drama', size: 12, sortBy: 'year', direction: 'desc' });
    const shared$ = this.movieApi.getSharedWithMe();

    forkJoin([recommended$, trending$, action$, drama$, shared$]).subscribe({
      next: ([recRes, trendRes, actionRes, dramaRes, sharedRes]) => {
        // 1. Process Recommended
        const recList = (recRes.data ?? []).map((m, idx) => ({
          id: m.id,
          title: m.title,
          meta: m.genres?.length ? `${m.releaseYear} · ${m.genres.join(', ')}` : `${m.releaseYear}`,
          bg: this.getPosterBg(m.posterUrl, idx),
          imdbRating: m.imdbRating,
          reason: m.recommendationReason,
          raw: this.recommendedToMovieResponse(m),
        }));
        this.recommendedMovies.set(recList);

        // 2. Process Trending
        const trendList = (trendRes.data ?? []).map((m, idx) => ({
          id: m.id,
          title: m.title,
          meta: m.genres?.length ? `${m.releaseYear} · ${m.genres.join(', ')}` : `${m.releaseYear}`,
          bg: this.getPosterBg(m.posterUrl, idx),
          imdbRating: m.imdbRating,
          reason: m.recommendationReason,
          raw: this.recommendedToMovieResponse(m),
        }));
        this.trendingMovies.set(trendList);

        // Pick Hero Movie from Trending or Recommended
        if (trendList.length > 0) {
          this.heroMovie.set(trendList[0].raw);
        } else if (recList.length > 0) {
          this.heroMovie.set(recList[0].raw);
        }

        // 3. Process Action
        const actionList = (actionRes.data?.content ?? []).map((m, idx) => ({
          id: m.id,
          title: m.title,
          meta: m.genres?.length ? `${m.releaseYear} · ${m.genres.map((g) => g.name).join(', ')}` : `${m.releaseYear}`,
          bg: this.getPosterBg(m.posterUrl, idx),
          imdbRating: m.imdbRating,
          raw: m,
        }));
        this.actionMovies.set(actionList);

        // 4. Process Drama
        const dramaList = (dramaRes.data?.content ?? []).map((m, idx) => ({
          id: m.id,
          title: m.title,
          meta: m.genres?.length ? `${m.releaseYear} · ${m.genres.map((g) => g.name).join(', ')}` : `${m.releaseYear}`,
          bg: this.getPosterBg(m.posterUrl, idx),
          imdbRating: m.imdbRating,
          raw: m,
        }));
        this.dramaMovies.set(dramaList);

        // 5. Process Shared with me
        const sharedRows = sharedRes.data ?? [];
        if (sharedRows.length > 0) {
          const movieCalls = sharedRows.map((r) => this.movieApi.getMovieById(r.movieId));
          const sharerIds = [...new Set(sharedRows.map((r) => r.sharedBy))];
          const userCalls = sharerIds.map((id) => this.userApi.getUserById(id));

          forkJoin([forkJoin(movieCalls), forkJoin(userCalls)]).subscribe({
            next: ([movieResponses, userResponses]) => {
              const sharerMap = new Map<string, string>();
              userResponses.forEach((u) => {
                if (u.data) {
                  sharerMap.set(u.data.id, u.data.username);
                }
              });

              const sharedCards: HomeMovieCard[] = sharedRows.map((row, idx) => {
                const movie = movieResponses[idx]?.data;
                const sharerUsername = sharerMap.get(row.sharedBy) ?? 'a friend';
                const genreNames = movie?.genres?.map((g) => g.name).join(', ') ?? '';
                const meta = genreNames ? `${movie?.releaseYear} · ${genreNames}` : `${movie?.releaseYear}`;

                return {
                  id: movie?.id ?? row.movieId,
                  title: movie?.title ?? 'Movie',
                  meta,
                  bg: this.getPosterBg(movie?.posterUrl, idx),
                  imdbRating: movie?.imdbRating ?? null,
                  sharedBy: `@${sharerUsername}`,
                  raw: movie ?? ({} as MovieResponse),
                };
              });

              this.sharedWithMeMovies.set(sharedCards);
            },
            error: () => {},
          });
        }

        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
      },
    });
  }

  scrollRow(elementId: string, direction: 'left' | 'right'): void {
    const el = document.getElementById(elementId);
    if (el) {
      const scrollAmount = direction === 'left' ? -480 : 480;
      el.scrollBy({ left: scrollAmount, behavior: 'smooth' });
    }
  }

  onAddToWatchlist(movieId: string, title: string, event?: Event): void {
    if (event) {
      event.stopPropagation();
    }
    this.actionMessage.set(null);

    this.watchlistApi.addToWatchlist(movieId).subscribe({
      next: () => {
        this.addedWatchlistIds.update((set) => {
          const next = new Set(set);
          next.add(movieId);
          return next;
        });
        this.actionMessage.set(`Added "${title}" to your watchlist.`);
      },
      error: (err) => {
        this.actionMessage.set(err.error?.message ?? 'Could not add movie to watchlist.');
      },
    });
  }

  isAdded(id: string): boolean {
    return this.addedWatchlistIds().has(id);
  }

  openModal(movie: MovieResponse): void {
    this.selectedMovie.set(movie);
    const existingScore = this.myRatings().get(movie.id) ?? 0;
    this.modalRating.set(existingScore);
  }

  closeModal(): void {
    this.selectedMovie.set(null);
  }

  openShareModal(movie: MovieResponse, event?: Event): void {
    if (event) {
      event.stopPropagation();
    }
    this.sharingMovie.set(movie);
  }

  closeShareModal(): void {
    this.sharingMovie.set(null);
  }

  setRating(score: number): void {
    const movie = this.selectedMovie();
    if (!movie) return;

    this.isRatingSaving.set(true);
    const existing = this.myRatings().get(movie.id);

    const call$ = existing
      ? this.ratingApi.changeRating(movie.id, { score })
      : this.ratingApi.rateMovie({ movieId: movie.id, score });

    call$.subscribe({
      next: () => {
        this.myRatings.update((map) => {
          const next = new Map(map);
          next.set(movie.id, score);
          return next;
        });
        this.modalRating.set(score);
        this.isRatingSaving.set(false);
      },
      error: () => {
        this.isRatingSaving.set(false);
      },
    });
  }

  getPosterBg(posterUrl?: string | null, index: number = 0): string {
    return posterUrl
      ? `center / cover url('${posterUrl}')`
      : FALLBACK_GRADIENTS[index % FALLBACK_GRADIENTS.length];
  }

  private recommendedToMovieResponse(rec: any): MovieResponse {
    return {
      id: rec.id,
      title: rec.title,
      description: rec.description ?? '',
      releaseYear: rec.releaseYear,
      durationMinutes: rec.durationMinutes,
      posterUrl: rec.posterUrl,
      trailerUrl: rec.trailerUrl,
      director: rec.director,
      imdbRating: rec.imdbRating,
      genres: (rec.genres ?? []).map((name: string) => ({ id: name, name })),
      createdAt: '',
      updatedAt: '',
    };
  }
}
