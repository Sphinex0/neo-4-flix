import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

type TwoFactorState = 'off' | 'setup' | 'on';

@Component({
  selector: 'app-profile',
  imports: [FormsModule, RouterLink],
  templateUrl: './profile.html',
})
export class Profile {
  firstName = 'Hamza';
  lastName = 'El Khawlani';
  username = 'hamza';
  email = 'hamza@example.com';

  currentPassword = '';
  newPassword = '';
  twoFactorCode = '';
  deleteConfirmPassword = '';

  saved = signal(false);
  twoFactorState = signal<TwoFactorState>('off');
  deleting = signal(false);
  updatedAt = signal('Today');

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
    this.saved.set(true);
    this.updatedAt.set('Just now');
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
    this.deleting.set(true);
  }

  cancelDelete(): void {
    this.deleting.set(false);
  }
}
