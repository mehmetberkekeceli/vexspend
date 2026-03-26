import AddCardRoundedIcon from '@mui/icons-material/AddCardRounded';
import DeleteOutlineRoundedIcon from '@mui/icons-material/DeleteOutlineRounded';
import Alert from '@mui/material/Alert';
import Box from '@mui/material/Box';
import Button from '@mui/material/Button';
import Card from '@mui/material/Card';
import CardContent from '@mui/material/CardContent';
import Chip from '@mui/material/Chip';
import Dialog from '@mui/material/Dialog';
import DialogActions from '@mui/material/DialogActions';
import DialogContent from '@mui/material/DialogContent';
import DialogTitle from '@mui/material/DialogTitle';
import IconButton from '@mui/material/IconButton';
import MenuItem from '@mui/material/MenuItem';
import Stack from '@mui/material/Stack';
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableCell from '@mui/material/TableCell';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import TextField from '@mui/material/TextField';
import Typography from '@mui/material/Typography';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useMemo, useState, type FormEvent } from 'react';
import { useTranslation } from 'react-i18next';
import { getBudgetItems } from '../api/budgetsApi';
import { extractErrorMessage } from '../api/httpClient';
import { createTransaction, deleteTransaction, getTransactions } from '../api/transactionsApi';
import { useReferenceData } from '../hooks/useReferenceData';
import { formatMoney } from '../lib/format';
import type { TransactionType } from '../types/api';

const todayIso = new Date().toISOString().slice(0, 10);

export function TransactionsPage() {
  const { t, i18n } = useTranslation();
  const locale = i18n.resolvedLanguage?.startsWith('tr') ? 'tr-TR' : 'en-US';
  const queryClient = useQueryClient();
  const [dialogOpen, setDialogOpen] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [feedbackMessage, setFeedbackMessage] = useState<string | null>(null);
  const [formState, setFormState] = useState({
    type: 'EXPENSE' as TransactionType,
    accountId: '',
    categoryId: '',
    budgetId: '',
    budgetItemId: '',
    amount: '',
    transactionDate: todayIso,
    merchant: '',
    note: '',
  });

  const transactionsQuery = useQuery({
    queryKey: ['transactions'],
    queryFn: getTransactions,
  });

  const { accountsQuery, categoriesQuery, budgetsQuery } = useReferenceData();

  const filteredCategories = useMemo(
    () => (categoriesQuery.data ?? []).filter((category) => category.type === formState.type),
    [categoriesQuery.data, formState.type],
  );

  const budgetItemsQuery = useQuery({
    queryKey: ['budget-items', formState.budgetId],
    queryFn: () => getBudgetItems(formState.budgetId),
    enabled: Boolean(formState.budgetId),
  });

  const createMutation = useMutation({
    mutationFn: createTransaction,
    onSuccess: async () => {
      setDialogOpen(false);
      setErrorMessage(null);
      setFeedbackMessage(t('transactions.success.created'));
      setFormState({
        type: 'EXPENSE',
        accountId: '',
        categoryId: '',
        budgetId: '',
        budgetItemId: '',
        amount: '',
        transactionDate: todayIso,
        merchant: '',
        note: '',
      });
      await queryClient.invalidateQueries({ queryKey: ['transactions'] });
      await queryClient.invalidateQueries({ queryKey: ['budgets'] });
      await queryClient.invalidateQueries({ queryKey: ['dashboard-report'] });
    },
    onError: (error) => {
      setErrorMessage(extractErrorMessage(error, t('transactions.errors.createFailed')));
    },
  });

  const deleteMutation = useMutation({
    mutationFn: deleteTransaction,
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['transactions'] });
      await queryClient.invalidateQueries({ queryKey: ['budgets'] });
      await queryClient.invalidateQueries({ queryKey: ['dashboard-report'] });
    },
    onError: (error) => {
      setErrorMessage(extractErrorMessage(error, t('transactions.errors.deleteFailed')));
    },
  });

  const handleCreate = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    if (!formState.accountId || !formState.categoryId || !formState.amount || !formState.transactionDate) {
      setErrorMessage(t('errors.formIncomplete'));
      return;
    }

    if (formState.type === 'EXPENSE' && (!formState.budgetId || !formState.budgetItemId)) {
      setErrorMessage(t('errors.expenseNeedsBudget'));
      return;
    }

    createMutation.mutate({
      accountId: formState.accountId,
      type: formState.type,
      amount: Number(formState.amount),
      transactionDate: formState.transactionDate,
      categoryId: formState.categoryId,
      budgetId: formState.type === 'EXPENSE' ? formState.budgetId : undefined,
      budgetItemId: formState.type === 'EXPENSE' ? formState.budgetItemId : undefined,
      merchant: formState.merchant || undefined,
      note: formState.note || undefined,
    });
  };

  return (
    <Stack spacing={3}>
      <Box className="d-flex flex-wrap align-items-center justify-content-between gap-2">
        <Box>
          <Typography variant="h4">{t('transactions.title')}</Typography>
          <Typography color="text.secondary">{t('transactions.subtitle')}</Typography>
        </Box>
        <Button startIcon={<AddCardRoundedIcon />} variant="contained" onClick={() => setDialogOpen(true)}>
          {t('common.actions.addTransaction')}
        </Button>
      </Box>

      {feedbackMessage && <Alert severity="success">{feedbackMessage}</Alert>}
      {errorMessage && <Alert severity="error">{errorMessage}</Alert>}

      <Card className="glass-card">
        <CardContent sx={{ p: 0 }}>
          <Box className="table-responsive">
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>{t('transactions.table.date')}</TableCell>
                  <TableCell>{t('transactions.table.category')}</TableCell>
                  <TableCell>{t('transactions.table.account')}</TableCell>
                  <TableCell>{t('transactions.table.type')}</TableCell>
                  <TableCell>{t('transactions.table.amount')}</TableCell>
                  <TableCell>{t('transactions.table.note')}</TableCell>
                  <TableCell />
                </TableRow>
              </TableHead>
              <TableBody>
                {!transactionsQuery.isLoading && (transactionsQuery.data?.length ?? 0) === 0 && (
                  <TableRow>
                    <TableCell colSpan={7}>{t('common.emptyState')}</TableCell>
                  </TableRow>
                )}
                {(transactionsQuery.data ?? []).map((row) => (
                  <TableRow key={row.id} hover>
                    <TableCell>{row.transactionDate}</TableCell>
                    <TableCell>{row.categoryName}</TableCell>
                    <TableCell>{row.accountName}</TableCell>
                    <TableCell>
                      <Chip
                        size="small"
                        color={row.type === 'INCOME' ? 'success' : 'error'}
                        label={t(`transactions.type.${row.type}`)}
                      />
                    </TableCell>
                    <TableCell>{formatMoney(row.amount, 'USD', locale)}</TableCell>
                    <TableCell>{row.note ?? row.merchant ?? '-'}</TableCell>
                    <TableCell align="right">
                      <IconButton color="error" onClick={() => deleteMutation.mutate(row.id)}>
                        <DeleteOutlineRoundedIcon />
                      </IconButton>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </Box>
        </CardContent>
      </Card>

      <Dialog open={dialogOpen} onClose={() => setDialogOpen(false)} fullWidth maxWidth="md">
        <DialogTitle>{t('transactions.createTitle')}</DialogTitle>
        <Box component="form" onSubmit={handleCreate}>
          <DialogContent>
            <Box
              sx={{
                display: 'grid',
                gap: 2,
                gridTemplateColumns: { xs: '1fr', md: 'repeat(2, minmax(0,1fr))' },
              }}
            >
              <TextField
                select
                label={t('transactions.fields.type')}
                value={formState.type}
                onChange={(event) =>
                  setFormState((current) => ({
                    ...current,
                    type: event.target.value as TransactionType,
                    categoryId: '',
                    budgetId: '',
                    budgetItemId: '',
                  }))
                }
              >
                <MenuItem value="EXPENSE">{t('transactions.type.EXPENSE')}</MenuItem>
                <MenuItem value="INCOME">{t('transactions.type.INCOME')}</MenuItem>
              </TextField>

              <TextField
                select
                required
                label={t('transactions.fields.account')}
                value={formState.accountId}
                onChange={(event) => setFormState((current) => ({ ...current, accountId: event.target.value }))}
              >
                {(accountsQuery.data ?? []).map((account) => (
                  <MenuItem key={account.id} value={account.id}>
                    {account.name}
                  </MenuItem>
                ))}
              </TextField>

              <TextField
                select
                required
                label={t('transactions.fields.category')}
                value={formState.categoryId}
                onChange={(event) => setFormState((current) => ({ ...current, categoryId: event.target.value }))}
              >
                {filteredCategories.map((category) => (
                  <MenuItem key={category.id} value={category.id}>
                    {category.name}
                  </MenuItem>
                ))}
              </TextField>

              <TextField
                required
                type="number"
                label={t('transactions.fields.amount')}
                inputProps={{ min: 0.01, step: 0.01 }}
                value={formState.amount}
                onChange={(event) => setFormState((current) => ({ ...current, amount: event.target.value }))}
              />

              <TextField
                required
                type="date"
                label={t('transactions.fields.date')}
                InputLabelProps={{ shrink: true }}
                value={formState.transactionDate}
                onChange={(event) => setFormState((current) => ({ ...current, transactionDate: event.target.value }))}
              />

              {formState.type === 'EXPENSE' && (
                <>
                  <TextField
                    select
                    required
                    label={t('transactions.fields.budget')}
                    value={formState.budgetId}
                    onChange={(event) =>
                      setFormState((current) => ({
                        ...current,
                        budgetId: event.target.value,
                        budgetItemId: '',
                      }))
                    }
                  >
                    {(budgetsQuery.data ?? []).map((budget) => (
                      <MenuItem key={budget.id} value={budget.id}>
                        {budget.name}
                      </MenuItem>
                    ))}
                  </TextField>
                  <TextField
                    select
                    required
                    label={t('transactions.fields.budgetItem')}
                    value={formState.budgetItemId}
                    onChange={(event) => setFormState((current) => ({ ...current, budgetItemId: event.target.value }))}
                  >
                    {(budgetItemsQuery.data ?? []).map((item) => (
                      <MenuItem key={item.id} value={item.id}>
                        {item.name}
                      </MenuItem>
                    ))}
                  </TextField>
                </>
              )}

              <TextField
                label={t('transactions.fields.merchant')}
                value={formState.merchant}
                onChange={(event) => setFormState((current) => ({ ...current, merchant: event.target.value }))}
              />
              <TextField
                multiline
                minRows={2}
                label={t('transactions.fields.note')}
                value={formState.note}
                onChange={(event) => setFormState((current) => ({ ...current, note: event.target.value }))}
                sx={{ gridColumn: { md: '1 / span 2' } }}
              />
            </Box>
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setDialogOpen(false)}>{t('common.actions.cancel')}</Button>
            <Button type="submit" variant="contained" disabled={createMutation.isPending}>
              {t('transactions.createButton')}
            </Button>
          </DialogActions>
        </Box>
      </Dialog>
    </Stack>
  );
}
