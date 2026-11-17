import { Routes } from '@angular/router';
import { Login } from './features/auth/pages/login/login';
import { Register } from './features/auth/pages/register/register';
import { SecureAccount } from './features/auth/pages/secure-account/secure-account';
import { Verify2fa } from './features/auth/pages/verify-2fa/verify-2fa';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'login', component: Login },
  { path: 'register', component: Register },
  { path: 'secure-account', component: SecureAccount },
  { path: 'verify-2fa', component: Verify2fa },
];
