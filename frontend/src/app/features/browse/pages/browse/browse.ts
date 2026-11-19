import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgStyle } from '@angular/common';
import { Navbar } from '../../../../shared/navbar/navbar';
import { RatingApiService } from '../../../ratings/services/rating-api.service';
import { WatchlistApiService } from '../../../watchlist/services/watchlist-api.service';
import { GenreResponse, MovieResponse } from '../../../movies/models/movie.models';
import { MovieApiService } from '../../../movies/services/movie-api.service';

interface MovieCard {
  id: string;
  title: string;
  meta: string;
  bg: string;
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
  selector: 'app-browse',
  imports: [Navbar, FormsModule, NgStyle],
  templateUrl: './browse.html',
})
export class Browse implements OnInit {
  private readonly movieApi = inject(MovieApiService);
  private readonly watchlistApi = inject(WatchlistApiService);
  private readonly ratingApi = inject(RatingApiService);

  // Filters & Search State
  searchQuery = signal<string>('');
  selectedGenre = signal<string>('All');
  selectedSort = signal<string>('title-asc');
  currentPage = signal<number>(0);
  pageSize = 12;

  // Data State
  genres = signal<string[]>(['All']);
  movies = signal<MovieCard[]>([]);
  totalPages = signal<number>(1);
  totalElements = signal<number>(0);
  isLoading = signal<boolean>(true);
  errorMessage = signal<string | null>(null);

  // Watchlist & Rating state
  addedWatchlistIds = signal<Set<string>>(new Set());
  myRatings = signal<Map<string, number>>(new Map());
  actionMessage = signal<string | null>(null);

  // Modal State
  selectedMovie = signal<MovieResponse | null>(null);
  modalRating = signal<number>(0);
  isRatingSaving = signal<boolean>(false);

  ngOnInit(): void {
    this.loadGenres();
    this.loadMyRatings();
    this.loadMovies();
  }

  loadGenres(): void {
    this.movieApi.getAllGenres().subscribe({
      next: (res) => {
        const names = (res.data ?? []).map((g: GenreResponse) => g.name).sort();
        this.genres.set(['All', ...names]);
      },
      error: () => {
        // Fallback standard genres if api fails
        this.genres.set([
          'All',
          'Action',
          'Adventure',
          'Animation',
          'Comedy',
          'Crime',
          'Drama',
          'History',
          'Horror',
          'Mystery',
          'Romance',
          'Science Fiction',
          'Thriller',
          'War',
        ]);
      },
    });
  }

  loadMyRatings(): void {
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

  loadMovies(): void {
    this.isLoading.set(true);
    this.errorMessage.set(null);

    const [sortBy, direction] = this.parseSort(this.selectedSort());

    this.movieApi
      .searchMovies({
        title: this.searchQuery(),
        genre: this.selectedGenre(),
        page: this.currentPage(),
        size: this.pageSize,
        sortBy,
        direction,
      })
      .subscribe({
        next: (res) => {
          const pageData = res.data;
          const content = pageData?.content ?? [];
          this.movies.set(content.map((m, idx) => this.toCard(m, idx)));
          this.totalPages.set(pageData?.totalPages ?? 1);
          this.totalElements.set(pageData?.totalElements ?? 0);
          this.isLoading.set(false);
        },
        error: (err) => {
          this.isLoading.set(false);
          this.errorMessage.set(err.error?.message ?? 'Could not load movies from server.');
        },
      });
  }

  onSearchChange(): void {
    this.currentPage.set(0);
    this.loadMovies();
  }

  selectGenre(genre: string): void {
    if (this.selectedGenre() !== genre) {
      this.selectedGenre.set(genre);
      this.currentPage.set(0);
      this.loadMovies();
    }
  }

  onSortChange(): void {
    this.currentPage.set(0);
    this.loadMovies();
  }

  goToPage(page: number): void {
    if (page >= 0 && page < this.totalPages() && page !== this.currentPage()) {
      this.currentPage.set(page);
      this.loadMovies();
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }
  }

  onAddToWatchlist(movie: MovieCard, event?: Event): void {
    if (event) {
      event.stopPropagation();
    }
    this.actionMessage.set(null);

    this.watchlistApi.addToWatchlist(movie.id).subscribe({
      next: () => {
        this.addedWatchlistIds.update((set) => {
          const next = new Set(set);
          next.add(movie.id);
          return next;
        });
        this.actionMessage.set(`Added "${movie.title}" to watchlist.`);
      },
      error: (err) => {
        this.actionMessage.set(err.error?.message ?? 'Could not add to watchlist.');
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

  getPosterBg(movie: MovieResponse, index: number = 0): string {
    return movie.posterUrl
      ? `center / cover url('${movie.posterUrl}')`
      : FALLBACK_GRADIENTS[index % FALLBACK_GRADIENTS.length];
  }

  private parseSort(val: string): [string, string] {
    switch (val) {
      case 'year-desc':
        return ['year', 'desc'];
      case 'year-asc':
        return ['year', 'asc'];
      case 'title-desc':
        return ['title', 'desc'];
      case 'title-asc':
      default:
        return ['title', 'asc'];
    }
  }

  private toCard(movie: MovieResponse, index: number): MovieCard {
    const genreNames = (movie.genres ?? []).map((g) => g.name).join(', ');
    const meta = genreNames ? `${movie.releaseYear} · ${genreNames}` : `${movie.releaseYear}`;

    return {
      id: movie.id,
      title: movie.title,
      meta,
      bg: this.getPosterBg(movie, index),
      raw: movie,
    };
  }
}
