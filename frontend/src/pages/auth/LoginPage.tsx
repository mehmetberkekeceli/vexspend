import LoadingButton from '@mui/lab/LoadingButton';
import Alert from '@mui/material/Alert';
import Card from '@mui/material/Card';
import CardContent from '@mui/material/CardContent';
import Link from '@mui/material/Link';
import Stack from '@mui/material/Stack';
import TextField from '@mui/material/TextField';
import Typography from '@mui/material/Typography';
import { useState, type FormEvent } from 'react';
import { useTranslation } from 'react-i18next';
import { Link as RouterLink, useLocation, useNavigate } from 'react-router-dom';
import { extractErrorMessage } from '../../api/httpClient';
import { useAuth } from '../../hooks/useAuth';

interface LocationState {
  from?: string;
}

export function LoginPage() {
  const { t } = useTranslation();
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [usernameOrEmail, setUsernameOrEmail] = useState('');
  const [password, setPassword] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const from = (location.state as LocationState | null)?.from ?? '/dashboard';

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setSubmitting(true);
    setErrorMessage(null);
    try {
      await login({ usernameOrEmail, password });
      navigate(from, { replace: true });
    } catch (error) {
      setErrorMessage(extractErrorMessage(error, t('auth.errors.loginFailed')));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Card className="glass-card">
      <CardContent sx={{ p: 4 }}>
        <Stack component="form" spacing={2.5} onSubmit={handleSubmit}>
          <BoxHeading title={t('auth.loginTitle')} subtitle={t('auth.loginSubtitle')} />

          {errorMessage && <Alert severity="error">{errorMessage}</Alert>}

          <TextField
            label={t('auth.usernameOrEmail')}
            value={usernameOrEmail}
            onChange={(event) => setUsernameOrEmail(event.target.value)}
            required
            autoComplete="username"
            autoFocus
          />
          <TextField
            type="password"
            label={t('auth.password')}
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            required
            autoComplete="current-password"
          />

          <LoadingButton loading={submitting} type="submit" variant="contained" size="large">
            {t('auth.loginButton')}
          </LoadingButton>

          <Typography variant="body2" color="text.secondary">
            {t('auth.noAccount')}{' '}
            <Link component={RouterLink} to="/auth/register">
              {t('auth.registerButton')}
            </Link>
          </Typography>
        </Stack>
      </CardContent>
    </Card>
  );
}

function BoxHeading({ title, subtitle }: { title: string; subtitle: string }) {
  return (
    <Stack spacing={0.8}>
      <Typography variant="h4">{title}</Typography>
      <Typography color="text.secondary">{subtitle}</Typography>
    </Stack>
  );
}
