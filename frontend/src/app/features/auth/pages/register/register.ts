import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthApiService } from '../../services/auth-api.service';

@Component({
  selector: 'app-register',
  imports: [FormsModule, RouterLink],
  templateUrl: './register.html',
})
export class Register {
  private readonly authService = inject(AuthApiService);
  private readonly router = inject(Router);

  firstName = '';
  lastName = '';
  username = '';
  email = '';
  password = '';

  isSubmitting = signal(false);
  errorMessage = signal<string | null>(null);

  onSubmit(): void {
    this.isSubmitting.set(true);
    this.errorMessage.set(null);

    this.authService
      .register({
        firstName: this.firstName,
        lastName: this.lastName,
        username: this.username,
        email: this.email,
        password: this.password,
      })
      .subscribe({
        next: () => this.router.navigate(['/secure-account']),
        error: (err) => {
          this.isSubmitting.set(false);
          this.errorMessage.set(err.error?.message ?? 'Something went wrong. Please try again.');
        },
      });
  }
}
