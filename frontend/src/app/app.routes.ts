import { Routes } from '@angular/router';
import { authGuard } from './core/auth/auth.guard';
import { Login } from './features/auth/pages/login/login';
import { Register } from './features/auth/pages/register/register';
import { SecureAccount } from './features/auth/pages/secure-account/secure-account';
import { Verify2fa } from './features/auth/pages/verify-2fa/verify-2fa';
import { Browse } from './features/browse/pages/browse/browse';
import { Home } from './features/home/pages/home/home';
import { Recommendations } from './features/recommendations/pages/recommendations/recommendations';
import { Profile } from './features/users/pages/profile/profile';
import { Watchlist } from './features/watchlist/pages/watchlist/watchlist';

export const routes: Routes = [
  { path: '', component: Home, canActivate: [authGuard] },
  { path: 'login', component: Login },
  { path: 'register', component: Register },
  { path: 'secure-account', component: SecureAccount },
  { path: 'verify-2fa', component: Verify2fa },
  { path: 'profile', component: Profile, canActivate: [authGuard] },
  { path: 'search', component: Browse, canActivate: [authGuard] },
  { path: 'recommendations', component: Recommendations, canActivate: [authGuard] },
  { path: 'my-list', component: Watchlist, canActivate: [authGuard] },
];
