import { useQuery } from '@tanstack/react-query';
import { getAccounts } from '../api/accountsApi';
import { getBudgets } from '../api/budgetsApi';
import { getCategories } from '../api/categoriesApi';

export function useReferenceData() {
  const accountsQuery = useQuery({
    queryKey: ['accounts'],
    queryFn: getAccounts,
  });

  const categoriesQuery = useQuery({
    queryKey: ['categories'],
    queryFn: getCategories,
  });

  const budgetsQuery = useQuery({
    queryKey: ['budgets'],
    queryFn: getBudgets,
  });

  return {
    accountsQuery,
    categoriesQuery,
    budgetsQuery,
  };
}
