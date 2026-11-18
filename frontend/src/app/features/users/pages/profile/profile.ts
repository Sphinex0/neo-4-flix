import { Component, inject, signal } from '@angular/core';
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
export class Profile {
  private readonly userApi = inject(UserApiService);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  firstName = 'Hamza';
  lastName = 'El Khawlani';
  username = 'hamza';
  email = 'hamza@example.com';

  currentPassword = '';
  newPassword = '';
  twoFactorCode = '';
  deleteConfirmPassword = '';

  saved = signal(false);
  isSaving = signal(false);
  saveError = signal<string | null>(null);

  twoFactorState = signal<TwoFactorState>('off');
  updatedAt = signal('Today');

  showDeleteModal = signal(false);
  isDeleting = signal(false);
  deleteError = signal<string | null>(null);

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
        next: () => {
          this.isSaving.set(false);
          this.saved.set(true);
          this.updatedAt.set('Just now');
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
}
