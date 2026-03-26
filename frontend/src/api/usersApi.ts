import { httpClient } from './httpClient';
import type { UpdateMyProfileRequest, UserProfile } from '../types/api';

export async function getMyProfile() {
  const { data } = await httpClient.get<UserProfile>('/api/v1/users/me');
  return data;
}

export async function updateMyProfile(payload: UpdateMyProfileRequest) {
  const { data } = await httpClient.put<UserProfile>('/api/v1/users/me', payload);
  return data;
}

export async function deleteMyProfile() {
  await httpClient.delete('/api/v1/users/me');
}
