export interface User {
  id: string;
  email: string;
  role: 'super-admin' | 'master' | 'operador';
  tenant_id?: string;
}

export interface LoginPayload {
  email: string;
  password: string;
}

export interface LoginResponse {
  message: string;
  user?: User;
}
