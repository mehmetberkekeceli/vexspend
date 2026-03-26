import DeleteOutlineRoundedIcon from '@mui/icons-material/DeleteOutlineRounded';
import LoadingButton from '@mui/lab/LoadingButton';
import Alert from '@mui/material/Alert';
import Box from '@mui/material/Box';
import Button from '@mui/material/Button';
import Card from '@mui/material/Card';
import CardContent from '@mui/material/CardContent';
import FormControlLabel from '@mui/material/FormControlLabel';
import IconButton from '@mui/material/IconButton';
import MenuItem from '@mui/material/MenuItem';
import Stack from '@mui/material/Stack';
import Switch from '@mui/material/Switch';
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableCell from '@mui/material/TableCell';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import TextField from '@mui/material/TextField';
import Typography from '@mui/material/Typography';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useState, type FormEvent } from 'react';
import { useTranslation } from 'react-i18next';
import { createAccount, deleteAccount, getAccounts } from '../api/accountsApi';
import { createCategory, deleteCategory, getCategories } from '../api/categoriesApi';
import { extractErrorMessage } from '../api/httpClient';
import { deleteMyProfile, updateMyProfile } from '../api/usersApi';
import { LanguageSwitcher } from '../components/ui/LanguageSwitcher';
import { API_BASE_URL } from '../lib/env';
import { useAuth } from '../hooks/useAuth';
import { useColorMode } from '../hooks/useColorMode';
import type { AccountType, CategoryType, UserProfile } from '../types/api';

const accountTypeOptions: AccountType[] = ['CASH', 'BANK', 'CREDIT_CARD', 'E_WALLET', 'OTHER'];
const categoryTypeOptions: CategoryType[] = ['EXPENSE', 'INCOME'];

export function SettingsPage() {
  const { t } = useTranslation();
  const { mode, toggleMode } = useColorMode();
  const { user } = useAuth();

  return (
    <Stack spacing={3}>
      <Box>
        <Typography variant="h4">{t('settings.title')}</Typography>
        <Typography color="text.secondary">{t('settings.subtitle')}</Typography>
      </Box>

      <Box
        sx={{
          display: 'grid',
          gap: 2,
          gridTemplateColumns: {
            xs: '1fr',
            lg: 'repeat(2, minmax(0, 1fr))',
          },
        }}
      >
        <Card className="glass-card">
          <CardContent>
            <Stack spacing={2}>
              <Typography variant="h6">{t('settings.language')}</Typography>
              <LanguageSwitcher />
            </Stack>
          </CardContent>
        </Card>

        <Card className="glass-card">
          <CardContent>
            <Stack spacing={2}>
              <Typography variant="h6">{t('settings.darkMode')}</Typography>
              <FormControlLabel
                control={<Switch checked={mode === 'dark'} onChange={toggleMode} />}
                label={mode === 'dark' ? 'On' : 'Off'}
              />
            </Stack>
          </CardContent>
        </Card>
      </Box>

      <Card className="glass-card">
        <CardContent>
          <Stack spacing={2}>
            <Typography variant="h6">{t('settings.apiBaseUrl')}</Typography>
            <TextField fullWidth value={API_BASE_URL} InputProps={{ readOnly: true }} />
          </Stack>
        </CardContent>
      </Card>

      {user ? <ProfileSettingsCard user={user} /> : <Alert severity="warning">{t('settings.profileNotFound')}</Alert>}
      <AccountSettingsCard />
      <CategorySettingsCard />
    </Stack>
  );
}

function ProfileSettingsCard({ user }: { user: UserProfile }) {
  const { t } = useTranslation();
  const { setUser, logout } = useAuth();
  const [username, setUsername] = useState(user.username);
  const [email, setEmail] = useState(user.email);
  const [fullName, setFullName] = useState(user.fullName ?? '');
  const [profilePhotoUrl, setProfilePhotoUrl] = useState(user.profilePhotoUrl ?? '');
  const [feedback, setFeedback] = useState<string | null>(null);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const updateProfileMutation = useMutation({
    mutationFn: updateMyProfile,
    onSuccess: (nextUser) => {
      setUser(nextUser);
      setFeedback(t('settings.saved'));
      setErrorMessage(null);
    },
    onError: (error) => {
      setErrorMessage(extractErrorMessage(error, t('settings.saveFailed')));
      setFeedback(null);
    },
  });

  const deleteProfileMutation = useMutation({
    mutationFn: deleteMyProfile,
    onSuccess: () => {
      logout();
    },
    onError: (error) => {
      setErrorMessage(extractErrorMessage(error, t('settings.deleteAccountFailed')));
    },
  });

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setFeedback(null);
    setErrorMessage(null);
    updateProfileMutation.mutate({
      username,
      email,
      fullName,
      profilePhotoUrl,
    });
  };

  return (
    <Card className="glass-card">
      <CardContent>
        <Stack component="form" spacing={2} onSubmit={handleSubmit}>
          <Typography variant="h6">{t('settings.profileSection')}</Typography>

          {feedback && <Alert severity="success">{feedback}</Alert>}
          {errorMessage && <Alert severity="error">{errorMessage}</Alert>}

          <TextField
            label={t('auth.username')}
            value={username}
            onChange={(event) => setUsername(event.target.value)}
            required
          />
          <TextField
            label={t('auth.email')}
            value={email}
            onChange={(event) => setEmail(event.target.value)}
            required
          />
          <TextField
            label={t('settings.fullName')}
            value={fullName}
            onChange={(event) => setFullName(event.target.value)}
          />
          <TextField
            label={t('settings.profilePhotoUrl')}
            value={profilePhotoUrl}
            onChange={(event) => setProfilePhotoUrl(event.target.value)}
          />

          <Stack direction="row" spacing={1.5}>
            <LoadingButton loading={updateProfileMutation.isPending} type="submit" variant="contained">
              {t('settings.save')}
            </LoadingButton>
            <Button
              color="error"
              variant="outlined"
              onClick={() => deleteProfileMutation.mutate()}
              disabled={deleteProfileMutation.isPending}
            >
              {t('settings.deleteAccount')}
            </Button>
          </Stack>
        </Stack>
      </CardContent>
    </Card>
  );
}

function AccountSettingsCard() {
  const { t } = useTranslation();
  const queryClient = useQueryClient();
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [formState, setFormState] = useState({
    name: '',
    type: 'BANK' as AccountType,
    currencyCode: 'USD',
    initialBalance: '',
  });

  const accountsQuery = useQuery({
    queryKey: ['accounts'],
    queryFn: getAccounts,
  });

  const createAccountMutation = useMutation({
    mutationFn: createAccount,
    onSuccess: async () => {
      setFormState({ name: '', type: 'BANK', currencyCode: 'USD', initialBalance: '' });
      await queryClient.invalidateQueries({ queryKey: ['accounts'] });
      await queryClient.invalidateQueries({ queryKey: ['dashboard-report'] });
      setErrorMessage(null);
    },
    onError: (error) => setErrorMessage(extractErrorMessage(error, t('settings.accountCreateFailed'))),
  });

  const deleteAccountMutation = useMutation({
    mutationFn: deleteAccount,
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['accounts'] });
      await queryClient.invalidateQueries({ queryKey: ['dashboard-report'] });
    },
    onError: (error) => setErrorMessage(extractErrorMessage(error, t('settings.accountDeleteFailed'))),
  });

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    createAccountMutation.mutate({
      name: formState.name,
      type: formState.type,
      currencyCode: formState.currencyCode.toUpperCase(),
      initialBalance: Number(formState.initialBalance || 0),
    });
  };

  return (
    <Card className="glass-card">
      <CardContent>
        <Stack spacing={2}>
          <Typography variant="h6">{t('settings.accountSection')}</Typography>
          {errorMessage && <Alert severity="error">{errorMessage}</Alert>}

          <Box component="form" onSubmit={handleSubmit}>
            <Box
              sx={{
                display: 'grid',
                gap: 1.5,
                gridTemplateColumns: { xs: '1fr', md: 'repeat(4, minmax(0, 1fr))' },
              }}
            >
              <TextField
                required
                label={t('settings.fields.accountName')}
                value={formState.name}
                onChange={(event) => setFormState((current) => ({ ...current, name: event.target.value }))}
              />
              <TextField
                select
                label={t('settings.fields.accountType')}
                value={formState.type}
                onChange={(event) =>
                  setFormState((current) => ({
                    ...current,
                    type: event.target.value as AccountType,
                  }))
                }
              >
                {accountTypeOptions.map((type) => (
                  <MenuItem key={type} value={type}>
                    {t(`settings.accountTypes.${type}`)}
                  </MenuItem>
                ))}
              </TextField>
              <TextField
                required
                label={t('settings.fields.currencyCode')}
                value={formState.currencyCode}
                onChange={(event) => setFormState((current) => ({ ...current, currencyCode: event.target.value }))}
              />
              <TextField
                required
                type="number"
                inputProps={{ min: 0, step: 0.01 }}
                label={t('settings.fields.initialBalance')}
                value={formState.initialBalance}
                onChange={(event) => setFormState((current) => ({ ...current, initialBalance: event.target.value }))}
              />
            </Box>
            <Button type="submit" variant="contained" sx={{ mt: 1.5 }} disabled={createAccountMutation.isPending}>
              {t('settings.accountCreateButton')}
            </Button>
          </Box>

          <Box className="table-responsive">
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>{t('settings.fields.accountName')}</TableCell>
                  <TableCell>{t('settings.fields.accountType')}</TableCell>
                  <TableCell>{t('settings.fields.currencyCode')}</TableCell>
                  <TableCell />
                </TableRow>
              </TableHead>
              <TableBody>
                {(accountsQuery.data ?? []).map((account) => (
                  <TableRow key={account.id}>
                    <TableCell>{account.name}</TableCell>
                    <TableCell>{t(`settings.accountTypes.${account.type}`)}</TableCell>
                    <TableCell>{account.currencyCode}</TableCell>
                    <TableCell align="right">
                      <IconButton color="error" onClick={() => deleteAccountMutation.mutate(account.id)}>
                        <DeleteOutlineRoundedIcon />
                      </IconButton>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </Box>
        </Stack>
      </CardContent>
    </Card>
  );
}

function CategorySettingsCard() {
  const { t } = useTranslation();
  const queryClient = useQueryClient();
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [formState, setFormState] = useState({
    name: '',
    type: 'EXPENSE' as CategoryType,
    colorHex: '#22c55e',
    icon: '',
  });

  const categoriesQuery = useQuery({
    queryKey: ['categories'],
    queryFn: getCategories,
  });

  const createCategoryMutation = useMutation({
    mutationFn: createCategory,
    onSuccess: async () => {
      setFormState({ name: '', type: 'EXPENSE', colorHex: '#22c55e', icon: '' });
      await queryClient.invalidateQueries({ queryKey: ['categories'] });
      setErrorMessage(null);
    },
    onError: (error) => setErrorMessage(extractErrorMessage(error, t('settings.categoryCreateFailed'))),
  });

  const deleteCategoryMutation = useMutation({
    mutationFn: deleteCategory,
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['categories'] });
    },
    onError: (error) => setErrorMessage(extractErrorMessage(error, t('settings.categoryDeleteFailed'))),
  });

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    createCategoryMutation.mutate({
      name: formState.name,
      type: formState.type,
      colorHex: formState.colorHex,
      icon: formState.icon || undefined,
    });
  };

  return (
    <Card className="glass-card">
      <CardContent>
        <Stack spacing={2}>
          <Typography variant="h6">{t('settings.categorySection')}</Typography>
          {errorMessage && <Alert severity="error">{errorMessage}</Alert>}

          <Box component="form" onSubmit={handleSubmit}>
            <Box
              sx={{
                display: 'grid',
                gap: 1.5,
                gridTemplateColumns: { xs: '1fr', md: 'repeat(4, minmax(0, 1fr))' },
              }}
            >
              <TextField
                required
                label={t('settings.fields.categoryName')}
                value={formState.name}
                onChange={(event) => setFormState((current) => ({ ...current, name: event.target.value }))}
              />
              <TextField
                select
                label={t('settings.fields.categoryType')}
                value={formState.type}
                onChange={(event) =>
                  setFormState((current) => ({
                    ...current,
                    type: event.target.value as CategoryType,
                  }))
                }
              >
                {categoryTypeOptions.map((type) => (
                  <MenuItem key={type} value={type}>
                    {t(`transactions.type.${type}`)}
                  </MenuItem>
                ))}
              </TextField>
              <TextField
                label={t('settings.fields.colorHex')}
                value={formState.colorHex}
                onChange={(event) => setFormState((current) => ({ ...current, colorHex: event.target.value }))}
              />
              <TextField
                label={t('settings.fields.icon')}
                value={formState.icon}
                onChange={(event) => setFormState((current) => ({ ...current, icon: event.target.value }))}
              />
            </Box>
            <Button type="submit" variant="contained" sx={{ mt: 1.5 }} disabled={createCategoryMutation.isPending}>
              {t('settings.categoryCreateButton')}
            </Button>
          </Box>

          <Box className="table-responsive">
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>{t('settings.fields.categoryName')}</TableCell>
                  <TableCell>{t('settings.fields.categoryType')}</TableCell>
                  <TableCell>{t('settings.fields.colorHex')}</TableCell>
                  <TableCell />
                </TableRow>
              </TableHead>
              <TableBody>
                {(categoriesQuery.data ?? []).map((category) => (
                  <TableRow key={category.id}>
                    <TableCell>{category.name}</TableCell>
                    <TableCell>{t(`transactions.type.${category.type}`)}</TableCell>
                    <TableCell>{category.colorHex ?? '-'}</TableCell>
                    <TableCell align="right">
                      <IconButton color="error" onClick={() => deleteCategoryMutation.mutate(category.id)}>
                        <DeleteOutlineRoundedIcon />
                      </IconButton>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </Box>
        </Stack>
      </CardContent>
    </Card>
  );
}
