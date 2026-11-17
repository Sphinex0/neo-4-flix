import { Injectable, PLATFORM_ID, inject, signal } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';

const TOKEN_KEY = 'neo4flix_token';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly isBrowser = isPlatformBrowser(inject(PLATFORM_ID));
  private readonly tokenSignal = signal<string | null>(this.readToken());

  readonly token = this.tokenSignal.asReadonly();

  isAuthenticated(): boolean {
    return this.tokenSignal() !== null;
  }

  setToken(token: string): void {
    this.tokenSignal.set(token);

    if (this.isBrowser) {
      localStorage.setItem(TOKEN_KEY, token);
    }
  }

  clearToken(): void {
    this.tokenSignal.set(null);

    if (this.isBrowser) {
      localStorage.removeItem(TOKEN_KEY);
    }
  }

  private readToken(): string | null {
    if (!this.isBrowser) {
      return null;
    }

    return localStorage.getItem(TOKEN_KEY);
  }
}
