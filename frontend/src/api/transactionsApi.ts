import { httpClient } from './httpClient';
import type { CreateTransactionPayload, TransactionResponse } from '../types/api';

export async function getTransactions() {
  const { data } = await httpClient.get<TransactionResponse[]>('/api/v1/transactions');
  return data;
}

export async function createTransaction(payload: CreateTransactionPayload) {
  const { data } = await httpClient.post<TransactionResponse>('/api/v1/transactions', payload);
  return data;
}

export async function deleteTransaction(transactionId: string) {
  await httpClient.delete(`/api/v1/transactions/${transactionId}`);
}
