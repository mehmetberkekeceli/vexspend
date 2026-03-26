import AddRoundedIcon from '@mui/icons-material/AddRounded';
import DeleteOutlineRoundedIcon from '@mui/icons-material/DeleteOutlineRounded';
import PlaylistAddRoundedIcon from '@mui/icons-material/PlaylistAddRounded';
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
import LinearProgress from '@mui/material/LinearProgress';
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
import { createBudget, createBudgetItem, deleteBudget, getBudgets } from '../api/budgetsApi';
import { getCategories } from '../api/categoriesApi';
import { extractErrorMessage } from '../api/httpClient';
import { formatMoney } from '../lib/format';

type BudgetStatus = 'onTrack' | 'warning' | 'exceeded';

interface BudgetRow {
  id: string;
  name: string;
  period: string;
  allocated: string;
  spent: string;
  usage: number;
  status: BudgetStatus;
}

const todayIso = new Date().toISOString().slice(0, 10);
const nextMonthIso = new Date(new Date().setMonth(new Date().getMonth() + 1)).toISOString().slice(0, 10);

export function BudgetsPage() {
  const { t, i18n } = useTranslation();
  const locale = i18n.resolvedLanguage?.startsWith('tr') ? 'tr-TR' : 'en-US';
  const queryClient = useQueryClient();
  const [feedbackMessage, setFeedbackMessage] = useState<string | null>(null);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [createDialogOpen, setCreateDialogOpen] = useState(false);
  const [itemDialogBudgetId, setItemDialogBudgetId] = useState<string | null>(null);
  const [createForm, setCreateForm] = useState({
    name: '',
    currencyCode: 'USD',
    periodStart: todayIso,
    periodEnd: nextMonthIso,
    totalLimit: '',
  });
  const [itemForm, setItemForm] = useState({
    name: '',
    categoryId: '',
    allocatedAmount: '',
  });

  const budgetsQuery = useQuery({
    queryKey: ['budgets'],
    queryFn: getBudgets,
  });

  const categoriesQuery = useQuery({
    queryKey: ['categories'],
    queryFn: getCategories,
  });

  const expenseCategories = useMemo(
    () => (categoriesQuery.data ?? []).filter((category) => category.type === 'EXPENSE'),
    [categoriesQuery.data],
  );

  const createBudgetMutation = useMutation({
    mutationFn: createBudget,
    onSuccess: async () => {
      setCreateDialogOpen(false);
      setCreateForm({
        name: '',
        currencyCode: 'USD',
        periodStart: todayIso,
        periodEnd: nextMonthIso,
        totalLimit: '',
      });
      setErrorMessage(null);
      setFeedbackMessage(t('budgets.success.created'));
      await queryClient.invalidateQueries({ queryKey: ['budgets'] });
      await queryClient.invalidateQueries({ queryKey: ['dashboard-report'] });
    },
    onError: (error) => {
      setErrorMessage(extractErrorMessage(error, t('budgets.errors.createFailed')));
    },
  });

  const createBudgetItemMutation = useMutation({
    mutationFn: ({ budgetId, payload }: { budgetId: string; payload: { name: string; categoryId: string; allocatedAmount: number } }) =>
      createBudgetItem(budgetId, payload),
    onSuccess: async () => {
      setItemDialogBudgetId(null);
      setItemForm({ name: '', categoryId: '', allocatedAmount: '' });
      setErrorMessage(null);
      setFeedbackMessage(t('budgets.success.itemCreated'));
      await queryClient.invalidateQueries({ queryKey: ['budgets'] });
      await queryClient.invalidateQueries({ queryKey: ['dashboard-report'] });
    },
    onError: (error) => {
      setErrorMessage(extractErrorMessage(error, t('budgets.errors.createItemFailed')));
    },
  });

  const deleteBudgetMutation = useMutation({
    mutationFn: deleteBudget,
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['budgets'] });
      await queryClient.invalidateQueries({ queryKey: ['dashboard-report'] });
    },
    onError: (error) => {
      setErrorMessage(extractErrorMessage(error, t('budgets.errors.deleteFailed')));
    },
  });

  const budgetRows: BudgetRow[] =
    budgetsQuery.data?.map((budget) => {
      const usage = budget.totalLimit > 0 ? (budget.spentAmount / budget.totalLimit) * 100 : 0;
      const status: BudgetStatus = usage >= 100 ? 'exceeded' : usage >= 90 ? 'warning' : 'onTrack';
      return {
        id: budget.id,
        name: budget.name,
        period: `${budget.periodStart} - ${budget.periodEnd}`,
        allocated: formatMoney(budget.totalLimit, budget.currencyCode, locale),
        spent: formatMoney(budget.spentAmount, budget.currencyCode, locale),
        usage,
        status,
      };
    }) ?? [];

  const resolveColor = (status: BudgetStatus) => {
    if (status === 'onTrack') return 'success';
    if (status === 'warning') return 'warning';
    return 'error';
  };

  const handleCreateBudget = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    createBudgetMutation.mutate({
      name: createForm.name,
      currencyCode: createForm.currencyCode.toUpperCase(),
      periodStart: createForm.periodStart,
      periodEnd: createForm.periodEnd,
      totalLimit: Number(createForm.totalLimit),
    });
  };

  const handleCreateBudgetItem = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!itemDialogBudgetId) return;
    createBudgetItemMutation.mutate({
      budgetId: itemDialogBudgetId,
      payload: {
        name: itemForm.name,
        categoryId: itemForm.categoryId,
        allocatedAmount: Number(itemForm.allocatedAmount),
      },
    });
  };

  return (
    <Stack spacing={3}>
      <Box className="d-flex flex-wrap align-items-center justify-content-between gap-2">
        <Box>
          <Typography variant="h4">{t('budgets.title')}</Typography>
          <Typography color="text.secondary">{t('budgets.subtitle')}</Typography>
        </Box>
        <Button variant="contained" startIcon={<AddRoundedIcon />} onClick={() => setCreateDialogOpen(true)}>
          {t('common.actions.newBudget')}
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
                  <TableCell>{t('budgets.table.name')}</TableCell>
                  <TableCell>{t('budgets.table.period')}</TableCell>
                  <TableCell>{t('budgets.table.allocated')}</TableCell>
                  <TableCell>{t('budgets.table.spent')}</TableCell>
                  <TableCell>{t('budgets.table.status')}</TableCell>
                  <TableCell />
                </TableRow>
              </TableHead>
              <TableBody>
                {!budgetsQuery.isLoading && budgetRows.length === 0 && (
                  <TableRow>
                    <TableCell colSpan={6}>{t('common.emptyState')}</TableCell>
                  </TableRow>
                )}
                {budgetRows.map((row) => (
                  <TableRow key={row.id} hover>
                    <TableCell>{row.name}</TableCell>
                    <TableCell>{row.period}</TableCell>
                    <TableCell>{row.allocated}</TableCell>
                    <TableCell>
                      <Stack spacing={1}>
                        <Typography variant="body2">{row.spent}</Typography>
                        <LinearProgress
                          color={resolveColor(row.status)}
                          value={row.usage}
                          variant="determinate"
                          sx={{ maxWidth: 180 }}
                        />
                      </Stack>
                    </TableCell>
                    <TableCell>
                      <Chip color={resolveColor(row.status)} label={t(`budgets.status.${row.status}`)} size="small" />
                    </TableCell>
                    <TableCell align="right">
                      <IconButton color="primary" onClick={() => setItemDialogBudgetId(row.id)}>
                        <PlaylistAddRoundedIcon />
                      </IconButton>
                      <IconButton color="error" onClick={() => deleteBudgetMutation.mutate(row.id)}>
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

      <Dialog open={createDialogOpen} onClose={() => setCreateDialogOpen(false)} fullWidth maxWidth="sm">
        <DialogTitle>{t('budgets.createTitle')}</DialogTitle>
        <Box component="form" onSubmit={handleCreateBudget}>
          <DialogContent>
            <Stack spacing={2}>
              <TextField
                required
                label={t('budgets.fields.name')}
                value={createForm.name}
                onChange={(event) => setCreateForm((current) => ({ ...current, name: event.target.value }))}
              />
              <TextField
                required
                label={t('budgets.fields.currencyCode')}
                value={createForm.currencyCode}
                onChange={(event) => setCreateForm((current) => ({ ...current, currencyCode: event.target.value }))}
              />
              <TextField
                required
                type="date"
                label={t('budgets.fields.periodStart')}
                InputLabelProps={{ shrink: true }}
                value={createForm.periodStart}
                onChange={(event) => setCreateForm((current) => ({ ...current, periodStart: event.target.value }))}
              />
              <TextField
                required
                type="date"
                label={t('budgets.fields.periodEnd')}
                InputLabelProps={{ shrink: true }}
                value={createForm.periodEnd}
                onChange={(event) => setCreateForm((current) => ({ ...current, periodEnd: event.target.value }))}
              />
              <TextField
                required
                type="number"
                inputProps={{ min: 0.01, step: 0.01 }}
                label={t('budgets.fields.totalLimit')}
                value={createForm.totalLimit}
                onChange={(event) => setCreateForm((current) => ({ ...current, totalLimit: event.target.value }))}
              />
            </Stack>
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setCreateDialogOpen(false)}>{t('common.actions.cancel')}</Button>
            <Button type="submit" variant="contained" disabled={createBudgetMutation.isPending}>
              {t('budgets.createButton')}
            </Button>
          </DialogActions>
        </Box>
      </Dialog>

      <Dialog open={Boolean(itemDialogBudgetId)} onClose={() => setItemDialogBudgetId(null)} fullWidth maxWidth="sm">
        <DialogTitle>{t('budgets.addItemTitle')}</DialogTitle>
        <Box component="form" onSubmit={handleCreateBudgetItem}>
          <DialogContent>
            <Stack spacing={2}>
              <TextField
                required
                label={t('budgets.fields.name')}
                value={itemForm.name}
                onChange={(event) => setItemForm((current) => ({ ...current, name: event.target.value }))}
              />
              <TextField
                select
                required
                label={t('budgets.fields.category')}
                value={itemForm.categoryId}
                onChange={(event) => setItemForm((current) => ({ ...current, categoryId: event.target.value }))}
              >
                {expenseCategories.map((category) => (
                  <MenuItem key={category.id} value={category.id}>
                    {category.name}
                  </MenuItem>
                ))}
              </TextField>
              <TextField
                required
                type="number"
                inputProps={{ min: 0.01, step: 0.01 }}
                label={t('budgets.fields.allocatedAmount')}
                value={itemForm.allocatedAmount}
                onChange={(event) => setItemForm((current) => ({ ...current, allocatedAmount: event.target.value }))}
              />
            </Stack>
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setItemDialogBudgetId(null)}>{t('common.actions.cancel')}</Button>
            <Button type="submit" variant="contained" disabled={createBudgetItemMutation.isPending}>
              {t('budgets.addItemButton')}
            </Button>
          </DialogActions>
        </Box>
      </Dialog>
    </Stack>
  );
}
