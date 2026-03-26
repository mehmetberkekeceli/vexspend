import { httpClient } from './httpClient';
import type { BudgetItemResponse, BudgetResponse, CreateBudgetItemPayload, CreateBudgetPayload } from '../types/api';

export async function getBudgets() {
  const { data } = await httpClient.get<BudgetResponse[]>('/api/v1/budgets');
  return data;
}

export async function createBudget(payload: CreateBudgetPayload) {
  const { data } = await httpClient.post<BudgetResponse>('/api/v1/budgets', payload);
  return data;
}

export async function deleteBudget(budgetId: string) {
  await httpClient.delete(`/api/v1/budgets/${budgetId}`);
}

export async function getBudgetItems(budgetId: string) {
  const { data } = await httpClient.get<BudgetItemResponse[]>(`/api/v1/budgets/${budgetId}/items`);
  return data;
}

export async function createBudgetItem(budgetId: string, payload: CreateBudgetItemPayload) {
  const { data } = await httpClient.post<BudgetItemResponse>(`/api/v1/budgets/${budgetId}/items`, payload);
  return data;
}
