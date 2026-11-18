import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../../core/auth/auth.service';
import { UserApiService } from '../../services/user-api.service';

type TwoFactorState = 'off' | 'setup' | 'on';

@Component({
  selector: 'app-profile',
  imports: [FormsModule, RouterLink],
  templateUrl: './profile.html',
})
export class Profile implements OnInit {
  private readonly userApi = inject(UserApiService);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  firstName = '';
  lastName = '';
  username = '';
  email = '';

  currentPassword = '';
  newPassword = '';
  twoFactorCode = '';
  deleteConfirmPassword = '';

  isLoading = signal(true);
  loadError = signal<string | null>(null);

  saved = signal(false);
  isSaving = signal(false);
  saveError = signal<string | null>(null);

  twoFactorState = signal<TwoFactorState>('off');
  memberSince = signal('');
  updatedAt = signal('');

  showDeleteModal = signal(false);
  isDeleting = signal(false);
  deleteError = signal<string | null>(null);

  ngOnInit(): void {
    this.loadProfile();
  }

  loadProfile(): void {
    this.isLoading.set(true);
    this.loadError.set(null);

    this.userApi.getMe().subscribe({
      next: (res) => {
        const user = res.data;
        this.firstName = user.firstName;
        this.lastName = user.lastName;
        this.username = user.username;
        this.email = user.email;
        this.twoFactorState.set(user.twoFactorEnabled ? 'on' : 'off');
        this.memberSince.set(this.formatMonthYear(user.createdAt));
        this.updatedAt.set(this.formatDate(user.updatedAt));
        this.isLoading.set(false);
      },
      error: (err) => {
        this.isLoading.set(false);
        this.loadError.set(err.error?.message ?? 'Could not load your profile. Please try again.');
      },
    });
  }

  initials(): string {
    const first = (this.firstName || '').trim();
    const last = (this.lastName || '').trim();
    const initials = `${first[0] ?? ''}${last[0] ?? ''}`.toUpperCase();

    return initials || (this.username[0] ?? '?').toUpperCase();
  }

  fullName(): string {
    const name = `${this.firstName} ${this.lastName}`.trim();
    return name || this.username;
  }

  onFieldChange(): void {
    this.saved.set(false);
  }

  onSave(): void {
    this.isSaving.set(true);
    this.saveError.set(null);

    this.userApi
      .updateMe({
        firstName: this.firstName,
        lastName: this.lastName,
        username: this.username,
        email: this.email,
      })
      .subscribe({
        next: (res) => {
          this.isSaving.set(false);
          this.saved.set(true);
          this.updatedAt.set(this.formatDate(res.data.updatedAt));
        },
        error: (err) => {
          this.isSaving.set(false);
          this.saveError.set(err.error?.message ?? 'Could not save your profile. Please try again.');
        },
      });
  }

  startTwoFactorSetup(): void {
    this.twoFactorState.set('setup');
  }

  confirmTwoFactorSetup(): void {
    this.twoFactorState.set('on');
  }

  turnOffTwoFactor(): void {
    this.twoFactorState.set('off');
  }

  askDelete(): void {
    this.deleteError.set(null);
    this.showDeleteModal.set(true);
  }

  cancelDelete(): void {
    this.showDeleteModal.set(false);
    this.deleteConfirmPassword = '';
  }

  confirmDelete(): void {
    this.isDeleting.set(true);
    this.deleteError.set(null);

    this.userApi.deleteMe().subscribe({
      next: () => {
        this.authService.clearToken();
        this.router.navigate(['/login']);
      },
      error: (err) => {
        this.isDeleting.set(false);
        this.deleteError.set(err.error?.message ?? 'Could not delete your account. Please try again.');
      },
    });
  }

  private formatDate(iso: string): string {
    return new Date(iso).toLocaleDateString('en-US', { month: 'long', day: 'numeric', year: 'numeric' });
  }

  private formatMonthYear(iso: string): string {
    return new Date(iso).toLocaleDateString('en-US', { month: 'long', year: 'numeric' });
  }
}
