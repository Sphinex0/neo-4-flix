import { Component, EventEmitter, Input, Output } from '@angular/core';
import { NgStyle } from '@angular/common';
import { MovieResponse } from '../../models/movie.models';

const FALLBACK_GRADIENT = 'linear-gradient(160deg,#6A4423,#120B06)';

@Component({
  selector: 'app-movie-detail-modal',
  imports: [NgStyle],
  templateUrl: './movie-detail-modal.html',
})
export class MovieDetailModal {
  @Input({ required: true }) movie!: MovieResponse;
  @Input() isAdmin = false;
  @Input() isAdded = false;

  @Output() closed = new EventEmitter<void>();
  @Output() addToWatchlist = new EventEmitter<void>();
  @Output() share = new EventEmitter<void>();
  @Output() edit = new EventEmitter<void>();
  @Output() deleteMovie = new EventEmitter<void>();

  close(): void {
    this.closed.emit();
  }

  onAddToWatchlist(): void {
    this.addToWatchlist.emit();
  }

  onShare(): void {
    this.share.emit();
  }

  onEdit(): void {
    this.edit.emit();
  }

  onDelete(): void {
    this.deleteMovie.emit();
  }

  getPosterBg(): string {
    return this.movie.posterUrl ? `center / cover url('${this.movie.posterUrl}')` : FALLBACK_GRADIENT;
  }
}
