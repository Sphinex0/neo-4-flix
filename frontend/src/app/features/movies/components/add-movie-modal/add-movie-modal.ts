import { Component, EventEmitter, Input, OnInit, Output, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MovieResponse } from '../../models/movie.models';
import { MovieApiService } from '../../services/movie-api.service';

@Component({
  selector: 'app-add-movie-modal',
  imports: [FormsModule],
  templateUrl: './add-movie-modal.html',
})
export class AddMovieModal implements OnInit {
  private readonly movieApi = inject(MovieApiService);

  @Input() movie: MovieResponse | null = null;
  @Output() closed = new EventEmitter<void>();

  title = '';
  description = '';
  releaseYear: number | null = null;
  durationMinutes: number | null = null;
  posterUrl = '';
  trailerUrl = '';
  director = '';
  genres = '';

  isSaving = signal(false);
  errorMessage = signal<string | null>(null);

  ngOnInit(): void {
    if (!this.movie) {
      return;
    }

    this.title = this.movie.title;
    this.description = this.movie.description;
    this.releaseYear = this.movie.releaseYear;
    this.durationMinutes = this.movie.durationMinutes;
    this.posterUrl = this.movie.posterUrl ?? '';
    this.trailerUrl = this.movie.trailerUrl ?? '';
    this.director = this.movie.director ?? '';
    this.genres = this.movie.genres.map((genre) => genre.name).join(', ');
  }

  close(): void {
    this.closed.emit();
  }

  hasRequiredFields(): boolean {
    return !!this.title.trim() && !!this.description.trim() && !!this.releaseYear && !!this.durationMinutes;
  }

  hasChanges(): boolean {
    if (!this.movie) {
      return true;
    }

    return (
      this.title.trim() !== this.movie.title ||
      this.description.trim() !== this.movie.description ||
      this.releaseYear !== this.movie.releaseYear ||
      this.durationMinutes !== this.movie.durationMinutes ||
      (this.posterUrl.trim() || null) !== this.movie.posterUrl ||
      (this.trailerUrl.trim() || null) !== this.movie.trailerUrl ||
      (this.director.trim() || null) !== this.movie.director ||
      this.genres !== this.movie.genres.map((genre) => genre.name).join(', ')
    );
  }

  onSubmit(): void {
    if (!this.title.trim() || !this.description.trim() || !this.releaseYear || !this.durationMinutes) {
      this.errorMessage.set('Please fill in title, description, release year and duration.');
      return;
    }

    this.isSaving.set(true);
    this.errorMessage.set(null);

    const request = {
      title: this.title.trim(),
      description: this.description.trim(),
      releaseYear: this.releaseYear,
      durationMinutes: this.durationMinutes,
      posterUrl: this.posterUrl.trim() || null,
      trailerUrl: this.trailerUrl.trim() || null,
      director: this.director.trim() || null,
      genres: this.genres
        .split(',')
        .map((genre) => genre.trim())
        .filter((genre) => genre.length > 0),
    };

    const call$ = this.movie ? this.movieApi.updateMovie(this.movie.id, request) : this.movieApi.createMovie(request);

    call$.subscribe({
      next: () => {
        this.isSaving.set(false);
        this.closed.emit();
      },
      error: (err) => {
        this.isSaving.set(false);
        this.errorMessage.set(err.error?.message ?? 'Could not save the movie. Please try again.');
      },
    });
  }
}
