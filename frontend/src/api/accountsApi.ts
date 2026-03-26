import { httpClient } from './httpClient';
import type { AccountResponse, CreateAccountPayload } from '../types/api';

export async function getAccounts() {
  const { data } = await httpClient.get<AccountResponse[]>('/api/v1/accounts');
  return data;
}

export async function createAccount(payload: CreateAccountPayload) {
  const { data } = await httpClient.post<AccountResponse>('/api/v1/accounts', payload);
  return data;
}

export async function deleteAccount(accountId: string) {
  await httpClient.delete(`/api/v1/accounts/${accountId}`);
}
