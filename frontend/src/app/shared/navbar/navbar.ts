import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';
import { AddMovieModal } from '../../features/movies/components/add-movie-modal/add-movie-modal';
import { UserApiService } from '../../features/users/services/user-api.service';

@Component({
  selector: 'app-navbar',
  imports: [RouterLink, RouterLinkActive, AddMovieModal],
  templateUrl: './navbar.html',
})
export class Navbar implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly userApi = inject(UserApiService);

  initials = signal('?');
  userId = signal<string | null>(null);
  isAdmin = signal(false);

  showAddMovieModal = signal(false);

  ngOnInit(): void {
    this.userApi.getMe().subscribe({
      next: (res) => {
        const user = res.data;
        const first = (user.firstName || '').trim();
        const last = (user.lastName || '').trim();
        const computed = `${first[0] ?? ''}${last[0] ?? ''}`.toUpperCase();
        this.initials.set(computed || (user.username[0] ?? '?').toUpperCase());
        this.userId.set(user.id);
        this.isAdmin.set(user.role === 'ADMIN');
      },
    });
  }

  onSignOut(): void {
    this.authService.clearToken();
  }

  onMovieModalClosed(): void {
    this.showAddMovieModal.set(false);
  }
}
