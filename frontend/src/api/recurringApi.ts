import { httpClient } from './httpClient';
import type { CreateRecurringPayload, ProcessRecurringResult, RecurringTransactionResponse } from '../types/api';

export async function getRecurringTemplates() {
  const { data } = await httpClient.get<RecurringTransactionResponse[]>('/api/v1/recurring-transactions');
  return data;
}

export async function createRecurringTemplate(payload: CreateRecurringPayload) {
  const { data } = await httpClient.post<RecurringTransactionResponse>('/api/v1/recurring-transactions', payload);
  return data;
}

export async function deleteRecurringTemplate(templateId: string) {
  await httpClient.delete(`/api/v1/recurring-transactions/${templateId}`);
}

export async function processRecurring(date?: string) {
  const { data } = await httpClient.post<ProcessRecurringResult>('/api/v1/recurring-transactions/process-due', {}, {
    params: date ? { date } : undefined,
  });
  return data;
}
