import { createContext } from 'react';
import type { UserProfile } from '../types/api';

export interface LoginInput {
  usernameOrEmail: string;
  password: string;
}

export interface RegisterInput {
  username: string;
  email: string;
  password: string;
}

export interface AuthContextValue {
  isAuthenticated: boolean;
  isBootstrapping: boolean;
  accessToken: string | null;
  user: UserProfile | null;
  login: (input: LoginInput) => Promise<void>;
  register: (input: RegisterInput) => Promise<void>;
  logout: () => void;
  refreshProfile: () => Promise<void>;
  setUser: (nextUser: UserProfile) => void;
}

export const AuthContext = createContext<AuthContextValue | undefined>(undefined);
