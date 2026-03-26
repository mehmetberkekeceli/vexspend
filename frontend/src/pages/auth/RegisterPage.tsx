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
import { Link as RouterLink, useNavigate } from 'react-router-dom';
import { extractErrorMessage } from '../../api/httpClient';
import { useAuth } from '../../hooks/useAuth';

export function RegisterPage() {
  const { t } = useTranslation();
  const { register } = useAuth();
  const navigate = useNavigate();
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setSubmitting(true);
    setErrorMessage(null);
    try {
      await register({ username, email, password });
      navigate('/dashboard', { replace: true });
    } catch (error) {
      setErrorMessage(extractErrorMessage(error, t('auth.errors.registerFailed')));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Card className="glass-card">
      <CardContent sx={{ p: 4 }}>
        <Stack component="form" spacing={2.5} onSubmit={handleSubmit}>
          <BoxHeading title={t('auth.registerTitle')} subtitle={t('auth.registerSubtitle')} />

          {errorMessage && <Alert severity="error">{errorMessage}</Alert>}

          <TextField
            label={t('auth.username')}
            value={username}
            onChange={(event) => setUsername(event.target.value)}
            required
            autoComplete="username"
            autoFocus
            helperText={t('auth.usernameHint')}
            inputProps={{
              minLength: 3,
              maxLength: 60,
              pattern: '^[a-zA-Z0-9._-]+$',
            }}
          />
          <TextField
            label={t('auth.email')}
            type="email"
            value={email}
            onChange={(event) => setEmail(event.target.value)}
            required
            autoComplete="email"
          />
          <TextField
            type="password"
            label={t('auth.password')}
            value={password}
            helperText={t('auth.passwordHint')}
            onChange={(event) => setPassword(event.target.value)}
            required
            autoComplete="new-password"
            inputProps={{
              minLength: 8,
              maxLength: 72,
              pattern: '^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[\\W_]).+$',
            }}
          />

          <LoadingButton loading={submitting} type="submit" variant="contained" size="large">
            {t('auth.registerButton')}
          </LoadingButton>

          <Typography variant="body2" color="text.secondary">
            {t('auth.hasAccount')}{' '}
            <Link component={RouterLink} to="/auth/login">
              {t('auth.loginButton')}
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
