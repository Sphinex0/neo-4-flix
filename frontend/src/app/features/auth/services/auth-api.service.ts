import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../../../core/http/api-config';
import { ResponseData } from '../../../core/models/response-data';
import { AuthResponse, LoginRequest, RegisterRequest, TwoFactorVerifyRequest, UserResponse } from '../models/auth.models';

@Injectable({ providedIn: 'root' })
export class AuthApiService {
  private readonly http = inject(HttpClient);

  register(request: RegisterRequest): Observable<ResponseData<UserResponse>> {
    return this.http.post<ResponseData<UserResponse>>(`${API_BASE_URL}/api/auth/register`, request);
  }

  login(request: LoginRequest): Observable<ResponseData<AuthResponse>> {
    return this.http.post<ResponseData<AuthResponse>>(`${API_BASE_URL}/api/auth/login`, request);
  }

  verifyTwoFactor(request: TwoFactorVerifyRequest): Observable<ResponseData<AuthResponse>> {
    return this.http.post<ResponseData<AuthResponse>>(`${API_BASE_URL}/api/auth/2fa/verify`, request);
  }
}
