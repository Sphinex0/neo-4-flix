import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../../../core/http/api-config';
import { ResponseData } from '../../../core/models/response-data';
import { TwoFactorSetupResponse } from '../models/user.models';

@Injectable({ providedIn: 'root' })
export class UserApiService {
  private readonly http = inject(HttpClient);

  enableTwoFactor(): Observable<ResponseData<TwoFactorSetupResponse>> {
    return this.http.post<ResponseData<TwoFactorSetupResponse>>(`${API_BASE_URL}/api/users/me/2fa/enable`, {});
  }
}
