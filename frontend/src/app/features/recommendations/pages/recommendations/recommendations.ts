import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgStyle } from '@angular/common';
import { Navbar } from '../../../../shared/navbar/navbar';
import { MovieDetailModal } from '../../../movies/components/movie-detail-modal/movie-detail-modal';
import { ShareMovieModal } from '../../../movies/components/share-movie-modal/share-movie-modal';
import { MovieResponse } from '../../../movies/models/movie.models';
import { MovieApiService } from '../../../movies/services/movie-api.service';
import { WatchlistApiService } from '../../../watchlist/services/watchlist-api.service';
import { RecommendedMovie } from '../../models/recommendation.models';
import { RecommendationApiService } from '../../services/recommendation-api.service';

interface RecommendationCard {
  id: string;
  title: string;
  meta: string;
  reason: string;
  bg: string;
  raw: MovieResponse;
}

type Tab = 'for-you' | 'trending';

const FALLBACK_GRADIENTS = [
  'linear-gradient(160deg,#6A4423,#120B06)',
  'linear-gradient(160deg,#454C78,#0C0E18)',
  'linear-gradient(160deg,#6E3560,#150A13)',
  'linear-gradient(160deg,#5A4128,#0E0A06)',
  'linear-gradient(160deg,#256270,#08161A)',
];

@Component({
  selector: 'app-recommendations',
  imports: [Navbar, FormsModule, NgStyle, MovieDetailModal, ShareMovieModal],
  templateUrl: './recommendations.html',
})
export class Recommendations implements OnInit {
  private readonly recApi = inject(RecommendationApiService);
  private readonly watchlistApi = inject(WatchlistApiService);
  private readonly movieApi = inject(MovieApiService);

  activeTab = signal<Tab>('for-you');

  // Filters State
  genres = signal<string[]>(['All']);
  selectedGenre = signal<string>('All');
  selectedYear = signal<number | null>(null);

  selectedMovie = signal<MovieResponse | null>(null);
  sharingMovie = signal<MovieResponse | null>(null);

  isForYouLoading = signal(true);
  forYouError = signal<string | null>(null);

  isTrendingLoading = signal(true);
  trendingError = signal<string | null>(null);

  actionMessage = signal<string | null>(null);
  addedIds = signal<Set<string>>(new Set());

  forYouMovies: RecommendationCard[] = [];
  trendingMovies: RecommendationCard[] = [];

  ngOnInit(): void {
    this.loadGenres();
    this.loadForYou();
    this.loadTrending();
  }

  loadGenres(): void {
    this.movieApi.getAllGenres().subscribe({
      next: (res) => {
        const names = (res.data ?? []).map((g) => g.name).sort();
        this.genres.set(['All', ...names]);
      },
    });
  }

  selectGenre(genre: string): void {
    if (this.selectedGenre() !== genre) {
      this.selectedGenre.set(genre);
      this.reload();
    }
  }

  onYearChange(): void {
    this.reload();
  }

  clearFilters(): void {
    this.selectedGenre.set('All');
    this.selectedYear.set(null);
    this.reload();
  }

  reload(): void {
    this.loadForYou();
    this.loadTrending();
  }

  showForYou(): void {
    this.activeTab.set('for-you');
  }

  showTrending(): void {
    this.activeTab.set('trending');
  }

  forYouTabStyle(): Record<string, string> {
    const active = this.activeTab() === 'for-you';
    return {
      background: active ? '#FF5A5F' : 'transparent',
      color: active ? '#0D1017' : '#C9D0DC',
      'border-color': active ? '#FF5A5F' : '#5B6579',
    };
  }

  trendingTabStyle(): Record<string, string> {
    const active = this.activeTab() === 'trending';
    return {
      background: active ? '#FF5A5F' : 'transparent',
      color: active ? '#0D1017' : '#C9D0DC',
      'border-color': active ? '#FF5A5F' : '#5B6579',
    };
  }

  private loadForYou(): void {
    this.isForYouLoading.set(true);
    this.forYouError.set(null);
    const genre = this.selectedGenre() !== 'All' ? this.selectedGenre() : null;
    const year = this.selectedYear();

    this.recApi.getRecommendations(genre, year, 15).subscribe({
      next: (res) => {
        const list = res.data ?? [];
        this.forYouMovies = list.map((movie, index) => this.toCard(movie, index));
        this.isForYouLoading.set(false);
      },
      error: (err) => {
        this.isForYouLoading.set(false);
        this.forYouError.set(err.error?.message ?? 'Could not load your recommendations.');
      },
    });
  }

  private loadTrending(): void {
    this.isTrendingLoading.set(true);
    this.trendingError.set(null);
    const genre = this.selectedGenre() !== 'All' ? this.selectedGenre() : null;
    const year = this.selectedYear();

    this.recApi.getTrending(genre, year, 15).subscribe({
      next: (res) => {
        const list = res.data ?? [];
        this.trendingMovies = list.map((movie, index) => this.toCard(movie, index));
        this.isTrendingLoading.set(false);
      },
      error: (err) => {
        this.isTrendingLoading.set(false);
        this.trendingError.set(err.error?.message ?? 'Could not load trending movies.');
      },
    });
  }

  onAddToWatchlist(movie: { id: string; title: string }): void {
    if (this.isAdded(movie.id)) {
      return;
    }

    this.actionMessage.set(null);
    this.addedIds.update((set) => {
      const next = new Set(set);
      next.add(movie.id);
      return next;
    });

    this.watchlistApi.addToWatchlist(movie.id).subscribe({
      next: () => {
        this.actionMessage.set(`Added "${movie.title}" to your watchlist.`);
      },
      error: (err) => {
        this.addedIds.update((set) => {
          const next = new Set(set);
          next.delete(movie.id);
          return next;
        });
        this.actionMessage.set(err.error?.message ?? 'Could not add movie to watchlist.');
      },
    });
  }

  isAdded(id: string): boolean {
    return this.addedIds().has(id);
  }

  openModal(movie: MovieResponse): void {
    this.selectedMovie.set(movie);
  }

  closeModal(): void {
    this.selectedMovie.set(null);
  }

  openShareModal(movie: MovieResponse): void {
    this.selectedMovie.set(null);
    this.sharingMovie.set(movie);
  }

  onShareModalClosed(): void {
    this.sharingMovie.set(null);
  }

  private toCard(movie: RecommendedMovie, index: number): RecommendationCard {
    const genreNames = movie.genres?.length ? movie.genres.join(', ') : '';
    const meta = genreNames ? `${movie.releaseYear} · ${genreNames}` : `${movie.releaseYear}`;
    const bg = movie.posterUrl
      ? `center / cover url('${movie.posterUrl}')`
      : FALLBACK_GRADIENTS[index % FALLBACK_GRADIENTS.length];

    return {
      id: movie.id,
      title: movie.title,
      meta,
      reason: movie.recommendationReason ?? 'Recommended',
      bg,
      raw: {
        id: movie.id,
        title: movie.title,
        description: movie.description,
        releaseYear: movie.releaseYear,
        durationMinutes: movie.durationMinutes,
        posterUrl: movie.posterUrl,
        trailerUrl: movie.trailerUrl,
        director: movie.director,
        imdbRating: movie.imdbRating,
        genres: movie.genres?.map((name) => ({ id: name, name })) ?? [],
        createdAt: '',
        updatedAt: '',
      },
    };
  }
}
