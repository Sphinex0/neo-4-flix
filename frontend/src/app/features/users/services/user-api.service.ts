import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../../../core/http/api-config';
import { ResponseData } from '../../../core/models/response-data';
import { UserResponse } from '../../auth/models/auth.models';
import {
  ChangePasswordRequest,
  ConfirmTwoFactorRequest,
  FriendResponse,
  ProfileRequest,
  PublicUserResponse,
  TwoFactorSetupResponse,
  VerifyPasswordRequest,
} from '../models/user.models';

@Injectable({ providedIn: 'root' })
export class UserApiService {
  private readonly http = inject(HttpClient);

  getMe(): Observable<ResponseData<UserResponse>> {
    return this.http.get<ResponseData<UserResponse>>(`${API_BASE_URL}/api/users/me`);
  }

  getUserById(id: string): Observable<ResponseData<PublicUserResponse>> {
    return this.http.get<ResponseData<PublicUserResponse>>(`${API_BASE_URL}/api/users/${id}`);
  }

  searchUsers(query: string): Observable<ResponseData<PublicUserResponse[]>> {
    return this.http.get<ResponseData<PublicUserResponse[]>>(`${API_BASE_URL}/api/users/search`, {
      params: { q: query },
    });
  }

  updateMe(request: ProfileRequest): Observable<ResponseData<UserResponse>> {
    return this.http.put<ResponseData<UserResponse>>(`${API_BASE_URL}/api/users/me`, request);
  }

  deleteMe(): Observable<ResponseData<string>> {
    return this.http.delete<ResponseData<string>>(`${API_BASE_URL}/api/users/me`);
  }

  deleteUser(id: string): Observable<ResponseData<string>> {
    return this.http.delete<ResponseData<string>>(`${API_BASE_URL}/api/users/${id}`);
  }

  verifyPassword(request: VerifyPasswordRequest): Observable<ResponseData<string>> {
    return this.http.post<ResponseData<string>>(`${API_BASE_URL}/api/users/me/verify-password`, request);
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

  removeFriend(friendId: string): Observable<ResponseData<string>> {
    return this.http.delete<ResponseData<string>>(`${API_BASE_URL}/api/users/me/friends/${friendId}`);
  }

  enableTwoFactor(): Observable<ResponseData<TwoFactorSetupResponse>> {
    return this.http.post<ResponseData<TwoFactorSetupResponse>>(`${API_BASE_URL}/api/users/me/2fa/enable`, {});
  }

  confirmTwoFactor(request: ConfirmTwoFactorRequest): Observable<ResponseData<string>> {
    return this.http.post<ResponseData<string>>(`${API_BASE_URL}/api/users/me/2fa/confirm`, request);
  }

  disableTwoFactor(): Observable<ResponseData<string>> {
    return this.http.post<ResponseData<string>>(`${API_BASE_URL}/api/users/me/2fa/disable`, {});
  }
}
