import axios from 'axios';
import { API_BASE_URL } from '../lib/env';
import type { ApiErrorResponse } from '../types/api';

type TokenGetter = () => string | null;
type UnauthorizedHandler = () => void;

let tokenGetter: TokenGetter = () => null;
let unauthorizedHandler: UnauthorizedHandler = () => {};

export const httpClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 15_000,
});

export function setTokenGetter(getter: TokenGetter) {
  tokenGetter = getter;
}

export function setUnauthorizedHandler(handler: UnauthorizedHandler) {
  unauthorizedHandler = handler;
}

httpClient.interceptors.request.use((config) => {
  const token = tokenGetter();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

httpClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error?.response?.status === 401) {
      unauthorizedHandler();
    }
    return Promise.reject(error);
  },
);

export function extractErrorMessage(error: unknown, fallbackMessage: string) {
  if (axios.isAxiosError<ApiErrorResponse>(error)) {
    if (!error.response) {
      return `${fallbackMessage} (Backend baglantisini kontrol et: ${API_BASE_URL})`;
    }

    const validationEntries = Object.entries(error.response.data?.validationErrors ?? {});
    if (validationEntries.length > 0) {
      const [field, message] = validationEntries[0];
      return `${field}: ${message}`;
    }

    const message = error.response?.data?.message;
    if (message && message !== 'Validation failed') {
      return message;
    }

    return fallbackMessage;
  }
  return fallbackMessage;
}
