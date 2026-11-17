import { Routes } from '@angular/router';
import { authGuard } from './core/auth/auth.guard';
import { Login } from './features/auth/pages/login/login';
import { Register } from './features/auth/pages/register/register';
import { SecureAccount } from './features/auth/pages/secure-account/secure-account';
import { Verify2fa } from './features/auth/pages/verify-2fa/verify-2fa';
import { Home } from './features/home/pages/home/home';

export const routes: Routes = [
  { path: '', component: Home, canActivate: [authGuard] },
  { path: 'login', component: Login },
  { path: 'register', component: Register },
  { path: 'secure-account', component: SecureAccount },
  { path: 'verify-2fa', component: Verify2fa },
];
