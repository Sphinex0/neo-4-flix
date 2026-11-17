import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../../core/auth/auth.service';
import { AuthApiService } from '../../services/auth-api.service';

@Component({
  selector: 'app-verify-2fa',
  imports: [FormsModule, RouterLink],
  templateUrl: './verify-2fa.html',
})
export class Verify2fa {
  private readonly authApi = inject(AuthApiService);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  usernameOrEmail = this.route.snapshot.queryParamMap.get('usernameOrEmail') ?? '';

  digit1 = '';
  digit2 = '';
  digit3 = '';
  digit4 = '';
  digit5 = '';
  digit6 = '';

  isSubmitting = signal(false);
  errorMessage = signal<string | null>(null);

  onSubmit(): void {
    this.isSubmitting.set(true);
    this.errorMessage.set(null);

    const code = this.digit1 + this.digit2 + this.digit3 + this.digit4 + this.digit5 + this.digit6;

    this.authApi
      .verifyTwoFactor({
        usernameOrEmail: this.usernameOrEmail,
        code,
      })
      .subscribe({
        next: (response) => {
          this.authService.setToken(response.data.token!);
          this.router.navigate(['/']);
        },
        error: (err) => {
          this.isSubmitting.set(false);
          this.errorMessage.set(err.error?.message ?? 'Something went wrong. Please try again.');
        },
      });
  }
}
