import { httpClient } from './httpClient';
import type { AuthResponse } from '../types/api';

interface LoginPayload {
  usernameOrEmail: string;
  password: string;
}

interface RegisterPayload {
  username: string;
  email: string;
  password: string;
}

export async function login(payload: LoginPayload) {
  const { data } = await httpClient.post<AuthResponse>('/api/v1/auth/login', payload);
  return data;
}

export async function register(payload: RegisterPayload) {
  const { data } = await httpClient.post<AuthResponse>('/api/v1/auth/register', payload);
  return data;
}
