import { Component, EventEmitter, Input, OnInit, Output, inject, signal } from '@angular/core';
import { FriendResponse } from '../../../users/models/user.models';
import { UserApiService } from '../../../users/services/user-api.service';
import { MovieApiService } from '../../services/movie-api.service';

@Component({
  selector: 'app-share-movie-modal',
  imports: [],
  templateUrl: './share-movie-modal.html',
})
export class ShareMovieModal implements OnInit {
  private readonly userApi = inject(UserApiService);
  private readonly movieApi = inject(MovieApiService);

  @Input({ required: true }) movieId!: string;
  @Input({ required: true }) movieTitle!: string;
  @Output() closed = new EventEmitter<void>();

  isLoading = signal(true);
  loadError = signal<string | null>(null);
  friends: FriendResponse[] = [];

  sharingWithId = signal<string | null>(null);
  sharedWithIds = signal<Set<string>>(new Set());
  errorMessage = signal<string | null>(null);

  ngOnInit(): void {
    this.userApi.getMyFriends().subscribe({
      next: (res) => {
        this.friends = res.data ?? [];
        this.isLoading.set(false);
      },
      error: (err) => {
        this.isLoading.set(false);
        this.loadError.set(err.error?.message ?? 'Could not load your friends.');
      },
    });
  }

  close(): void {
    this.closed.emit();
  }

  shareWith(friend: FriendResponse): void {
    if (this.sharingWithId() || this.hasShared(friend.id)) {
      return;
    }

    this.errorMessage.set(null);
    this.sharingWithId.set(friend.id);

    this.movieApi.shareMovie(this.movieId, friend.id).subscribe({
      next: () => {
        this.sharingWithId.set(null);
        this.sharedWithIds.update((set) => new Set(set).add(friend.id));
      },
      error: (err) => {
        this.sharingWithId.set(null);
        this.errorMessage.set(err.error?.message ?? 'Could not share this movie.');
      },
    });
  }

  hasShared(id: string): boolean {
    return this.sharedWithIds().has(id);
  }
}
