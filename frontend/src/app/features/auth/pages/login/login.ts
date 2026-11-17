import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../../core/auth/auth.service';
import { AuthApiService } from '../../services/auth-api.service';

@Component({
  selector: 'app-login',
  imports: [FormsModule, RouterLink],
  templateUrl: './login.html',
})
export class Login {
  private readonly authApi = inject(AuthApiService);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  usernameOrEmail = '';
  password = '';

  isSubmitting = signal(false);
  errorMessage = signal<string | null>(null);
  showPassword = signal(false);

  onSubmit(): void {
    this.isSubmitting.set(true);
    this.errorMessage.set(null);

    this.authApi
      .login({
        usernameOrEmail: this.usernameOrEmail,
        password: this.password,
      })
      .subscribe({
        next: (response) => {
          if (response.data.twoFactorRequired) {
            this.router.navigate(['/verify-2fa'], {
              queryParams: { usernameOrEmail: this.usernameOrEmail },
            });
            return;
          }

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
