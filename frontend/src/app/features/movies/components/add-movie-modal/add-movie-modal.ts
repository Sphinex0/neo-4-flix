import { Component, EventEmitter, Output, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MovieApiService } from '../../services/movie-api.service';

@Component({
  selector: 'app-add-movie-modal',
  imports: [FormsModule],
  templateUrl: './add-movie-modal.html',
})
export class AddMovieModal {
  private readonly movieApi = inject(MovieApiService);

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

  close(): void {
    this.closed.emit();
  }

  onSubmit(): void {
    if (!this.title.trim() || !this.description.trim() || !this.releaseYear || !this.durationMinutes) {
      this.errorMessage.set('Please fill in title, description, release year and duration.');
      return;
    }

    this.isSaving.set(true);
    this.errorMessage.set(null);

    this.movieApi
      .createMovie({
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
      })
      .subscribe({
        next: () => {
          this.isSaving.set(false);
          this.closed.emit();
        },
        error: (err) => {
          this.isSaving.set(false);
          this.errorMessage.set(err.error?.message ?? 'Could not add the movie. Please try again.');
        },
      });
  }
}
