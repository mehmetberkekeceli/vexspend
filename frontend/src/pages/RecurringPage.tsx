import AddRoundedIcon from '@mui/icons-material/AddRounded';
import AutorenewRoundedIcon from '@mui/icons-material/AutorenewRounded';
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
import {
  createRecurringTemplate,
  deleteRecurringTemplate,
  getRecurringTemplates,
  processRecurring,
} from '../api/recurringApi';
import { getBudgetItems } from '../api/budgetsApi';
import { extractErrorMessage } from '../api/httpClient';
import { useReferenceData } from '../hooks/useReferenceData';
import { formatMoney } from '../lib/format';
import type { RecurrenceFrequency, TransactionType } from '../types/api';

const todayIso = new Date().toISOString().slice(0, 10);

export function RecurringPage() {
  const { t, i18n } = useTranslation();
  const locale = i18n.resolvedLanguage?.startsWith('tr') ? 'tr-TR' : 'en-US';
  const queryClient = useQueryClient();
  const [dialogOpen, setDialogOpen] = useState(false);
  const [formError, setFormError] = useState<string | null>(null);
  const [formState, setFormState] = useState({
    type: 'EXPENSE' as TransactionType,
    frequency: 'MONTHLY' as RecurrenceFrequency,
    accountId: '',
    categoryId: '',
    budgetId: '',
    budgetItemId: '',
    amount: '',
    startDate: todayIso,
    endDate: '',
    merchant: '',
    note: '',
  });

  const recurringQuery = useQuery({
    queryKey: ['recurring'],
    queryFn: getRecurringTemplates,
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
    mutationFn: createRecurringTemplate,
    onSuccess: async () => {
      setDialogOpen(false);
      setFormError(null);
      await queryClient.invalidateQueries({ queryKey: ['recurring'] });
      await queryClient.invalidateQueries({ queryKey: ['transactions'] });
      setFormState({
        type: 'EXPENSE',
        frequency: 'MONTHLY',
        accountId: '',
        categoryId: '',
        budgetId: '',
        budgetItemId: '',
        amount: '',
        startDate: todayIso,
        endDate: '',
        merchant: '',
        note: '',
      });
    },
    onError: (error) => {
      setFormError(extractErrorMessage(error, t('recurring.errors.createFailed')));
    },
  });

  const deleteMutation = useMutation({
    mutationFn: deleteRecurringTemplate,
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['recurring'] });
      await queryClient.invalidateQueries({ queryKey: ['transactions'] });
    },
  });

  const processMutation = useMutation({
    mutationFn: () => processRecurring(todayIso),
    onSuccess: async (result) => {
      await queryClient.invalidateQueries({ queryKey: ['recurring'] });
      await queryClient.invalidateQueries({ queryKey: ['transactions'] });
      await queryClient.invalidateQueries({ queryKey: ['dashboard-report'] });
      setFormError(t('recurring.processSuccess', { count: result.processedTemplateCount }));
    },
    onError: (error) => {
      setFormError(extractErrorMessage(error, t('recurring.errors.processFailed')));
    },
  });

  const handleCreate = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    if (!formState.accountId || !formState.categoryId || !formState.amount || !formState.startDate) {
      setFormError(t('errors.formIncomplete'));
      return;
    }

    if (formState.type === 'EXPENSE' && (!formState.budgetId || !formState.budgetItemId)) {
      setFormError(t('errors.expenseNeedsBudget'));
      return;
    }

    createMutation.mutate({
      accountId: formState.accountId,
      categoryId: formState.categoryId,
      budgetId: formState.type === 'EXPENSE' ? formState.budgetId : undefined,
      budgetItemId: formState.type === 'EXPENSE' ? formState.budgetItemId : undefined,
      type: formState.type,
      amount: Number(formState.amount),
      frequency: formState.frequency,
      startDate: formState.startDate,
      endDate: formState.endDate || undefined,
      merchant: formState.merchant || undefined,
      note: formState.note || undefined,
    });
  };

  return (
    <Stack spacing={3}>
      <Box className="d-flex flex-wrap align-items-center justify-content-between gap-2">
        <Box>
          <Typography variant="h4">{t('recurring.title')}</Typography>
          <Typography color="text.secondary">{t('recurring.subtitle')}</Typography>
        </Box>
        <Box className="d-flex gap-2">
          <Button variant="contained" startIcon={<AddRoundedIcon />} onClick={() => setDialogOpen(true)}>
            {t('recurring.createButton')}
          </Button>
          <Button
            variant="outlined"
            startIcon={<AutorenewRoundedIcon />}
            onClick={() => processMutation.mutate()}
            disabled={processMutation.isPending}
          >
            {t('recurring.processButton')}
          </Button>
        </Box>
      </Box>

      {formError && <Alert severity="info">{formError}</Alert>}

      <Card className="glass-card">
        <CardContent sx={{ p: 0 }}>
          <Box className="table-responsive">
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>{t('recurring.table.type')}</TableCell>
                  <TableCell>{t('recurring.table.amount')}</TableCell>
                  <TableCell>{t('recurring.table.frequency')}</TableCell>
                  <TableCell>{t('recurring.table.nextExecution')}</TableCell>
                  <TableCell>{t('recurring.table.category')}</TableCell>
                  <TableCell>{t('recurring.table.account')}</TableCell>
                  <TableCell>{t('recurring.table.note')}</TableCell>
                  <TableCell>{t('recurring.table.active')}</TableCell>
                  <TableCell />
                </TableRow>
              </TableHead>
              <TableBody>
                {!recurringQuery.isLoading && (recurringQuery.data?.length ?? 0) === 0 && (
                  <TableRow>
                    <TableCell colSpan={9}>{t('common.emptyState')}</TableCell>
                  </TableRow>
                )}
                {(recurringQuery.data ?? []).map((item) => (
                  <TableRow key={item.id}>
                    <TableCell>
                      <Chip
                        size="small"
                        color={item.type === 'INCOME' ? 'success' : 'warning'}
                        label={t(`transactions.type.${item.type}`)}
                      />
                    </TableCell>
                    <TableCell>{formatMoney(item.amount, 'USD', locale)}</TableCell>
                    <TableCell>{t(`recurring.frequency.${item.frequency}`)}</TableCell>
                    <TableCell>{item.nextExecutionDate}</TableCell>
                    <TableCell>{item.categoryName}</TableCell>
                    <TableCell>{item.accountName}</TableCell>
                    <TableCell>{item.note ?? '-'}</TableCell>
                    <TableCell>
                      <Chip
                        size="small"
                        color={item.active ? 'success' : 'default'}
                        label={item.active ? t('recurring.status.active') : t('recurring.status.inactive')}
                      />
                    </TableCell>
                    <TableCell align="right">
                      <IconButton
                        color="error"
                        onClick={() => deleteMutation.mutate(item.id)}
                        disabled={deleteMutation.isPending}
                      >
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

      <Dialog fullWidth maxWidth="md" open={dialogOpen} onClose={() => setDialogOpen(false)}>
        <DialogTitle>{t('recurring.createTitle')}</DialogTitle>
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
                label={t('recurring.fields.type')}
                value={formState.type}
                onChange={(event) =>
                  setFormState((current) => ({
                    ...current,
                    type: event.target.value as TransactionType,
                    budgetId: '',
                    budgetItemId: '',
                    categoryId: '',
                  }))
                }
              >
                <MenuItem value="EXPENSE">{t('transactions.type.EXPENSE')}</MenuItem>
                <MenuItem value="INCOME">{t('transactions.type.INCOME')}</MenuItem>
              </TextField>

              <TextField
                select
                label={t('recurring.fields.frequency')}
                value={formState.frequency}
                onChange={(event) =>
                  setFormState((current) => ({
                    ...current,
                    frequency: event.target.value as RecurrenceFrequency,
                  }))
                }
              >
                <MenuItem value="DAILY">{t('recurring.frequency.DAILY')}</MenuItem>
                <MenuItem value="WEEKLY">{t('recurring.frequency.WEEKLY')}</MenuItem>
                <MenuItem value="MONTHLY">{t('recurring.frequency.MONTHLY')}</MenuItem>
              </TextField>

              <TextField
                select
                required
                label={t('recurring.fields.account')}
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
                label={t('recurring.fields.category')}
                value={formState.categoryId}
                onChange={(event) => setFormState((current) => ({ ...current, categoryId: event.target.value }))}
              >
                {filteredCategories.map((category) => (
                  <MenuItem key={category.id} value={category.id}>
                    {category.name}
                  </MenuItem>
                ))}
              </TextField>

              {formState.type === 'EXPENSE' && (
                <>
                  <TextField
                    select
                    required
                    label={t('recurring.fields.budget')}
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
                    label={t('recurring.fields.budgetItem')}
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
                required
                type="number"
                label={t('recurring.fields.amount')}
                inputProps={{ min: 0.01, step: 0.01 }}
                value={formState.amount}
                onChange={(event) => setFormState((current) => ({ ...current, amount: event.target.value }))}
              />
              <TextField
                required
                type="date"
                label={t('recurring.fields.startDate')}
                InputLabelProps={{ shrink: true }}
                value={formState.startDate}
                onChange={(event) => setFormState((current) => ({ ...current, startDate: event.target.value }))}
              />
              <TextField
                type="date"
                label={t('recurring.fields.endDate')}
                InputLabelProps={{ shrink: true }}
                value={formState.endDate}
                onChange={(event) => setFormState((current) => ({ ...current, endDate: event.target.value }))}
              />
              <TextField
                label={t('recurring.fields.merchant')}
                value={formState.merchant}
                onChange={(event) => setFormState((current) => ({ ...current, merchant: event.target.value }))}
              />
              <TextField
                multiline
                minRows={2}
                label={t('recurring.fields.note')}
                value={formState.note}
                onChange={(event) => setFormState((current) => ({ ...current, note: event.target.value }))}
                sx={{ gridColumn: { md: '1 / span 2' } }}
              />
            </Box>
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setDialogOpen(false)}>{t('common.actions.cancel')}</Button>
            <Button type="submit" variant="contained" disabled={createMutation.isPending}>
              {t('recurring.createButton')}
            </Button>
          </DialogActions>
        </Box>
      </Dialog>
    </Stack>
  );
}
