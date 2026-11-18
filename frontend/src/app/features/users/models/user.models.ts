export interface TwoFactorSetupResponse {
  secret: string;
  qrCodeUri: string;
}

export interface ProfileRequest {
  firstName: string;
  lastName: string;
  username: string;
  email: string;
}

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
}

export interface VerifyPasswordRequest {
  password: string;
}

export interface FriendResponse {
  id: string;
  firstName: string;
  lastName: string;
  username: string;
}
