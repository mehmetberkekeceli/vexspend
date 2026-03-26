import { httpClient } from './httpClient';
import type { DashboardReport } from '../types/api';

export async function getDashboardReport(trendMonths = 3) {
  const { data } = await httpClient.get<DashboardReport>('/api/v1/reports/dashboard', {
    params: { trendMonths },
  });
  return data;
}
