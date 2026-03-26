import Box from '@mui/material/Box';
import Container from '@mui/material/Container';
import Stack from '@mui/material/Stack';
import Typography from '@mui/material/Typography';
import { useTranslation } from 'react-i18next';
import { Outlet } from 'react-router-dom';
import { LanguageSwitcher } from '../ui/LanguageSwitcher';
import { ThemeModeToggle } from '../ui/ThemeModeToggle';

export function AuthLayout() {
  const { t } = useTranslation();

  return (
    <Box className="auth-layout">
      <Container maxWidth="sm" sx={{ py: 5 }}>
        <Stack direction="row" justifyContent="space-between" alignItems="center" sx={{ mb: 5 }}>
          <Box>
            <Typography variant="h5" fontWeight={800}>
              {t('app.name')}
            </Typography>
            <Typography variant="body2" color="text.secondary">
              {t('app.description')}
            </Typography>
          </Box>
          <Stack direction="row" spacing={1} alignItems="center">
            <LanguageSwitcher />
            <ThemeModeToggle />
          </Stack>
        </Stack>
        <Outlet />
      </Container>
    </Box>
  );
}
