import { httpClient } from './httpClient';
import type { CategoryResponse, CreateCategoryPayload } from '../types/api';

export async function getCategories() {
  const { data } = await httpClient.get<CategoryResponse[]>('/api/v1/categories');
  return data;
}

export async function createCategory(payload: CreateCategoryPayload) {
  const { data } = await httpClient.post<CategoryResponse>('/api/v1/categories', payload);
  return data;
}

export async function deleteCategory(categoryId: string) {
  await httpClient.delete(`/api/v1/categories/${categoryId}`);
}
