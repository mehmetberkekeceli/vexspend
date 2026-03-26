import { useCallback, useEffect, useMemo, useState, type PropsWithChildren } from 'react';
import { login as loginRequest, register as registerRequest } from '../api/authApi';
import { setTokenGetter, setUnauthorizedHandler } from '../api/httpClient';
import { getMyProfile } from '../api/usersApi';
import type { UserProfile } from '../types/api';
import { AuthContext, type LoginInput, type RegisterInput } from './authContextValue';

const STORAGE_KEY = 'vexspend-auth-session';

interface AuthSession {
  accessToken: string;
  expiresAt: string;
  user: UserProfile;
}

function isTokenExpired(expiresAt: string) {
  return new Date(expiresAt).getTime() <= Date.now();
}

function getStoredSession(): AuthSession | null {
  if (typeof window === 'undefined') {
    return null;
  }

  const raw = window.localStorage.getItem(STORAGE_KEY);
  if (!raw) {
    return null;
  }

  try {
    const parsed = JSON.parse(raw) as AuthSession;
    if (!parsed.accessToken || !parsed.expiresAt || !parsed.user) {
      return null;
    }
    if (isTokenExpired(parsed.expiresAt)) {
      return null;
    }
    return parsed;
  } catch {
    return null;
  }
}

function storeSession(session: AuthSession | null) {
  if (typeof window === 'undefined') {
    return;
  }
  if (!session) {
    window.localStorage.removeItem(STORAGE_KEY);
    return;
  }
  window.localStorage.setItem(STORAGE_KEY, JSON.stringify(session));
}

export function AuthProvider({ children }: PropsWithChildren) {
  const initialSession = useMemo(() => getStoredSession(), []);
  const [accessToken, setAccessToken] = useState<string | null>(initialSession?.accessToken ?? null);
  const [expiresAt, setExpiresAt] = useState<string | null>(initialSession?.expiresAt ?? null);
  const [user, setUserState] = useState<UserProfile | null>(initialSession?.user ?? null);
  const [isBootstrapping, setIsBootstrapping] = useState(Boolean(initialSession));

  const logout = useCallback(() => {
    setAccessToken(null);
    setExpiresAt(null);
    setUserState(null);
    storeSession(null);
  }, []);

  const applySession = useCallback((session: AuthSession) => {
    setAccessToken(session.accessToken);
    setExpiresAt(session.expiresAt);
    setUserState(session.user);
    storeSession(session);
  }, []);

  const refreshProfile = useCallback(async () => {
    if (!accessToken || !expiresAt || !user) {
      return;
    }
    const me = await getMyProfile();
    applySession({
      accessToken,
      expiresAt,
      user: me,
    });
  }, [accessToken, applySession, expiresAt, user]);

  const setUser = useCallback(
    (nextUser: UserProfile) => {
      if (!accessToken || !expiresAt) {
        return;
      }
      applySession({
        accessToken,
        expiresAt,
        user: nextUser,
      });
    },
    [accessToken, applySession, expiresAt],
  );

  const login = useCallback(
    async (input: LoginInput) => {
      const response = await loginRequest(input);
      applySession({
        accessToken: response.accessToken,
        expiresAt: response.expiresAt,
        user: response.user,
      });
    },
    [applySession],
  );

  const register = useCallback(
    async (input: RegisterInput) => {
      const response = await registerRequest(input);
      applySession({
        accessToken: response.accessToken,
        expiresAt: response.expiresAt,
        user: response.user,
      });
    },
    [applySession],
  );

  useEffect(() => {
    if (!initialSession) {
      return;
    }

    void getMyProfile()
      .then((me) => {
        applySession({
          accessToken: initialSession.accessToken,
          expiresAt: initialSession.expiresAt,
          user: me,
        });
      })
      .catch(() => {
        logout();
      })
      .finally(() => {
        setIsBootstrapping(false);
      });
  }, [applySession, initialSession, logout]);

  useEffect(() => {
    setTokenGetter(() => accessToken);
  }, [accessToken]);

  useEffect(() => {
    setUnauthorizedHandler(logout);
  }, [logout]);

  useEffect(() => {
    if (!expiresAt || !accessToken) {
      return;
    }

    const remaining = new Date(expiresAt).getTime() - Date.now();
    const timer = window.setTimeout(() => {
      logout();
    }, Math.max(remaining, 0));

    return () => window.clearTimeout(timer);
  }, [accessToken, expiresAt, logout]);

  const value = useMemo(
    () => ({
      isAuthenticated: Boolean(accessToken && user && expiresAt && !isTokenExpired(expiresAt)),
      isBootstrapping,
      accessToken,
      user,
      login,
      register,
      logout,
      refreshProfile,
      setUser,
    }),
    [accessToken, expiresAt, isBootstrapping, login, logout, refreshProfile, register, setUser, user],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
