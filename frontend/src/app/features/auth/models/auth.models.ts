export interface RegisterRequest {
  firstName: string;
  lastName: string;
  username: string;
  email: string;
  password: string;
}

export interface LoginRequest {
  usernameOrEmail: string;
  password: string;
}

export interface TwoFactorVerifyRequest {
  usernameOrEmail: string;
  code: string;
}

export interface AuthResponse {
  token: string | null;
  twoFactorRequired: boolean;
}

export interface UserResponse {
  id: string;
  firstName: string;
  lastName: string;
  username: string;
  email: string;
  role: 'USER' | 'ADMIN';
  twoFactorEnabled: boolean;
  createdAt: string;
  updatedAt: string;
}
