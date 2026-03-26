import AccountBalanceWalletRoundedIcon from '@mui/icons-material/AccountBalanceWalletRounded';
import DashboardRoundedIcon from '@mui/icons-material/DashboardRounded';
import MenuRoundedIcon from '@mui/icons-material/MenuRounded';
import PaymentsRoundedIcon from '@mui/icons-material/PaymentsRounded';
import RepeatRoundedIcon from '@mui/icons-material/RepeatRounded';
import SettingsRoundedIcon from '@mui/icons-material/SettingsRounded';
import AppBar from '@mui/material/AppBar';
import Avatar from '@mui/material/Avatar';
import Box from '@mui/material/Box';
import Button from '@mui/material/Button';
import Container from '@mui/material/Container';
import Drawer from '@mui/material/Drawer';
import IconButton from '@mui/material/IconButton';
import List from '@mui/material/List';
import ListItemButton from '@mui/material/ListItemButton';
import ListItemIcon from '@mui/material/ListItemIcon';
import ListItemText from '@mui/material/ListItemText';
import Stack from '@mui/material/Stack';
import Toolbar from '@mui/material/Toolbar';
import Typography from '@mui/material/Typography';
import { useState, type ReactNode } from 'react';
import { useTranslation } from 'react-i18next';
import { Outlet, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import { LanguageSwitcher } from '../ui/LanguageSwitcher';
import { ThemeModeToggle } from '../ui/ThemeModeToggle';

interface NavigationItem {
  path: string;
  label: string;
  icon: ReactNode;
}

const drawerWidth = 290;

export function AppShell() {
  const { t } = useTranslation();
  const location = useLocation();
  const navigate = useNavigate();
  const { user, logout } = useAuth();
  const [mobileOpen, setMobileOpen] = useState(false);

  const navigationItems: NavigationItem[] = [
    { path: '/dashboard', label: t('nav.dashboard'), icon: <DashboardRoundedIcon /> },
    { path: '/budgets', label: t('nav.budgets'), icon: <AccountBalanceWalletRoundedIcon /> },
    { path: '/transactions', label: t('nav.transactions'), icon: <PaymentsRoundedIcon /> },
    { path: '/recurring', label: t('nav.recurring'), icon: <RepeatRoundedIcon /> },
    { path: '/settings', label: t('nav.settings'), icon: <SettingsRoundedIcon /> },
  ];

  const handleNavigate = (path: string) => {
    navigate(path);
    setMobileOpen(false);
  };

  const drawerContent = (
    <Box sx={{ p: 2 }}>
      <Typography variant="h6" sx={{ px: 1, mb: 1 }}>
        {t('app.name')}
      </Typography>
      <List>
        {navigationItems.map((item) => (
          <ListItemButton
            key={item.path}
            selected={location.pathname === item.path}
            onClick={() => handleNavigate(item.path)}
            sx={{ borderRadius: 2, mb: 0.5 }}
          >
            <ListItemIcon sx={{ minWidth: 40 }}>{item.icon}</ListItemIcon>
            <ListItemText primary={item.label} />
          </ListItemButton>
        ))}
      </List>
    </Box>
  );

  return (
    <Box sx={{ minHeight: '100vh' }}>
      <AppBar elevation={0} position="sticky">
        <Toolbar sx={{ gap: 1 }}>
          <IconButton
            color="inherit"
            edge="start"
            onClick={() => setMobileOpen(true)}
            sx={{ display: { xs: 'inline-flex', md: 'none' } }}
          >
            <MenuRoundedIcon />
          </IconButton>

          <Typography variant="h6" sx={{ fontWeight: 800, letterSpacing: 0.3 }}>
            {t('app.name')}
          </Typography>

          <Stack direction="row" spacing={0.5} sx={{ ml: 3, display: { xs: 'none', md: 'flex' } }}>
            {navigationItems.map((item) => {
              const selected = location.pathname === item.path;
              return (
                <Button
                  key={item.path}
                  startIcon={item.icon}
                  color={selected ? 'primary' : 'inherit'}
                  variant={selected ? 'contained' : 'text'}
                  onClick={() => handleNavigate(item.path)}
                  sx={{ borderRadius: 2 }}
                >
                  {item.label}
                </Button>
              );
            })}
          </Stack>

          <Box sx={{ ml: 'auto', display: 'flex', alignItems: 'center', gap: 1 }}>
            <LanguageSwitcher />
            <ThemeModeToggle />
            {user && (
              <>
                <Stack direction="row" spacing={1} alignItems="center">
                  <Avatar src={user.profilePhotoUrl ?? undefined} alt={user.username} sx={{ width: 30, height: 30 }}>
                    {user.username.slice(0, 1).toUpperCase()}
                  </Avatar>
                  <Typography
                    variant="body2"
                    sx={{ display: { xs: 'none', lg: 'inline' }, maxWidth: 160 }}
                    noWrap
                  >
                    {user.fullName || user.username}
                  </Typography>
                </Stack>
                <Button variant="outlined" color="inherit" onClick={logout}>
                  {t('auth.logout')}
                </Button>
              </>
            )}
          </Box>
        </Toolbar>
      </AppBar>

      <Drawer
        open={mobileOpen}
        onClose={() => setMobileOpen(false)}
        sx={{
          display: { xs: 'block', md: 'none' },
          '& .MuiDrawer-paper': { width: drawerWidth },
        }}
      >
        {drawerContent}
      </Drawer>

      <Container maxWidth="xl" className="app-shell-main py-4">
        <Outlet />
      </Container>

      <Container maxWidth="xl" sx={{ pb: 3 }}>
        <Typography variant="body2" color="text.secondary">
          {t('app.footer')}
        </Typography>
      </Container>
    </Box>
  );
}
