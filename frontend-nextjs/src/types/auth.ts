// Types for authentication and user session

export interface UserSession {
  'user-id': number;
  email: string;
  role: 'master' | 'operador';
  'tenant-id': number;
  exp: number;
}

export interface LoginRequest {
  email: string;
  password: string;
  subdomain: string;
}

export interface LoginResponse {
  message: string;
  token: string;
}
