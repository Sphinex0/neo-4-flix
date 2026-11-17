import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { UserApiService } from '../../../users/services/user-api.service';

@Component({
  selector: 'app-secure-account',
  imports: [RouterLink],
  templateUrl: './secure-account.html',
})
export class SecureAccount implements OnInit {
  private readonly userApi = inject(UserApiService);

  secret = signal<string | null>(null);
  errorMessage = signal<string | null>(null);

  ngOnInit(): void {
    this.userApi.enableTwoFactor().subscribe({
      next: (response) => {
        this.secret.set(response.data.secret);
      },
      error: (err) => {
        this.errorMessage.set(err.error?.message ?? 'Something went wrong. Please try again.');
      },
    });
  }
}
