import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';
import { PublicUserResponse } from '../../features/users/models/user.models';
import { UserApiService } from '../../features/users/services/user-api.service';

@Component({
  selector: 'app-navbar',
  imports: [RouterLink, RouterLinkActive, FormsModule],
  templateUrl: './navbar.html',
})
export class Navbar implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly userApi = inject(UserApiService);
  private readonly router = inject(Router);

  initials = signal('?');
  userId = signal<string | null>(null);

  searchQuery = signal('');
  searchResults = signal<PublicUserResponse[]>([]);
  showResults = signal(false);
  isSearching = signal(false);

  ngOnInit(): void {
    this.userApi.getMe().subscribe({
      next: (res) => {
        const user = res.data;
        const first = (user.firstName || '').trim();
        const last = (user.lastName || '').trim();
        const computed = `${first[0] ?? ''}${last[0] ?? ''}`.toUpperCase();
        this.initials.set(computed || (user.username[0] ?? '?').toUpperCase());
        this.userId.set(user.id);
      },
    });
  }

  onSignOut(): void {
    this.authService.clearToken();
  }

  onSearchChange(): void {
    const query = this.searchQuery().trim();

    if (!query) {
      this.searchResults.set([]);
      this.showResults.set(false);
      return;
    }

    this.isSearching.set(true);

    this.userApi.searchUsers(query).subscribe({
      next: (res) => {
        this.searchResults.set(res.data ?? []);
        this.showResults.set(true);
        this.isSearching.set(false);
      },
      error: () => {
        this.isSearching.set(false);
      },
    });
  }

  onSearchFocus(): void {
    if (this.searchResults().length > 0) {
      this.showResults.set(true);
    }
  }

  onSearchBlur(): void {
    setTimeout(() => this.showResults.set(false), 200);
  }

  goToUser(user: PublicUserResponse): void {
    this.searchQuery.set('');
    this.searchResults.set([]);
    this.showResults.set(false);
    this.router.navigate(['/', user.id]);
  }
}
