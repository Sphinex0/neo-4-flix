import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { QRCodeComponent } from 'angularx-qrcode';
import { UserApiService } from '../../../users/services/user-api.service';

@Component({
  selector: 'app-secure-account',
  imports: [FormsModule, RouterLink, QRCodeComponent],
  templateUrl: './secure-account.html',
})
export class SecureAccount implements OnInit {
  private readonly userApi = inject(UserApiService);
  private readonly router = inject(Router);

  secret = signal<string | null>(null);
  qrCodeUri = signal<string | null>(null);
  errorMessage = signal<string | null>(null);

  code = '';
  isSubmitting = signal(false);

  ngOnInit(): void {
    this.userApi.enableTwoFactor().subscribe({
      next: (response) => {
        this.secret.set(response.data.secret);
        this.qrCodeUri.set(response.data.qrCodeUri);
      },
      error: (err) => {
        this.errorMessage.set(err.error?.message ?? 'Something went wrong. Please try again.');
      },
    });
  }

  onConfirm(): void {
    this.isSubmitting.set(true);
    this.errorMessage.set(null);

    this.userApi.confirmTwoFactor({ code: this.code }).subscribe({
      next: () => {
        this.router.navigate(['/']);
      },
      error: (err) => {
        this.isSubmitting.set(false);
        this.errorMessage.set(err.error?.message ?? 'Could not confirm the code. Please try again.');
      },
    });
  }
}
