import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../../../core/http/api-config';
import { ResponseData } from '../../../core/models/response-data';
import { UserResponse } from '../../auth/models/auth.models';
import { ChangePasswordRequest, FriendResponse, ProfileRequest, TwoFactorSetupResponse } from '../models/user.models';

@Injectable({ providedIn: 'root' })
export class UserApiService {
  private readonly http = inject(HttpClient);

  getMe(): Observable<ResponseData<UserResponse>> {
    return this.http.get<ResponseData<UserResponse>>(`${API_BASE_URL}/api/users/me`);
  }

  updateMe(request: ProfileRequest): Observable<ResponseData<UserResponse>> {
    return this.http.put<ResponseData<UserResponse>>(`${API_BASE_URL}/api/users/me`, request);
  }

  deleteMe(): Observable<ResponseData<string>> {
    return this.http.delete<ResponseData<string>>(`${API_BASE_URL}/api/users/me`);
  }

  changePassword(request: ChangePasswordRequest): Observable<ResponseData<string>> {
    return this.http.put<ResponseData<string>>(`${API_BASE_URL}/api/users/me/password`, request);
  }

  getMyFriends(): Observable<ResponseData<FriendResponse[]>> {
    return this.http.get<ResponseData<FriendResponse[]>>(`${API_BASE_URL}/api/users/me/friends`);
  }

  addFriend(friendId: string): Observable<ResponseData<FriendResponse>> {
    return this.http.post<ResponseData<FriendResponse>>(`${API_BASE_URL}/api/users/me/friends/${friendId}`, {});
  }

  enableTwoFactor(): Observable<ResponseData<TwoFactorSetupResponse>> {
    return this.http.post<ResponseData<TwoFactorSetupResponse>>(`${API_BASE_URL}/api/users/me/2fa/enable`, {});
  }

  disableTwoFactor(): Observable<ResponseData<string>> {
    return this.http.post<ResponseData<string>>(`${API_BASE_URL}/api/users/me/2fa/disable`, {});
  }
}
