import CircularProgress from '@mui/material/CircularProgress';
import Stack from '@mui/material/Stack';
import { Suspense, lazy } from 'react';
import { Navigate, Route, Routes } from 'react-router-dom';
import { AuthLayout } from '../components/layout/AuthLayout';
import { AppShell } from '../components/layout/AppShell';
import { ProtectedRoute } from '../components/routing/ProtectedRoute';
import { PublicOnlyRoute } from '../components/routing/PublicOnlyRoute';

const DashboardPage = lazy(async () => import('../pages/DashboardPage').then((module) => ({ default: module.DashboardPage })));
const BudgetsPage = lazy(async () => import('../pages/BudgetsPage').then((module) => ({ default: module.BudgetsPage })));
const TransactionsPage = lazy(async () =>
  import('../pages/TransactionsPage').then((module) => ({ default: module.TransactionsPage })),
);
const RecurringPage = lazy(async () => import('../pages/RecurringPage').then((module) => ({ default: module.RecurringPage })));
const SettingsPage = lazy(async () => import('../pages/SettingsPage').then((module) => ({ default: module.SettingsPage })));
const LoginPage = lazy(async () => import('../pages/auth/LoginPage').then((module) => ({ default: module.LoginPage })));
const RegisterPage = lazy(async () =>
  import('../pages/auth/RegisterPage').then((module) => ({ default: module.RegisterPage })),
);

export function AppRouter() {
  return (
    <Suspense
      fallback={
        <Stack sx={{ minHeight: '100vh', alignItems: 'center', justifyContent: 'center' }}>
          <CircularProgress />
        </Stack>
      }
    >
      <Routes>
        <Route element={<PublicOnlyRoute />}>
          <Route element={<AuthLayout />}>
            <Route path="/auth/login" element={<LoginPage />} />
            <Route path="/auth/register" element={<RegisterPage />} />
          </Route>
        </Route>

        <Route element={<ProtectedRoute />}>
          <Route element={<AppShell />}>
            <Route index element={<Navigate replace to="/dashboard" />} />
            <Route path="/dashboard" element={<DashboardPage />} />
            <Route path="/budgets" element={<BudgetsPage />} />
            <Route path="/transactions" element={<TransactionsPage />} />
            <Route path="/recurring" element={<RecurringPage />} />
            <Route path="/settings" element={<SettingsPage />} />
          </Route>
        </Route>

        <Route path="*" element={<Navigate replace to="/auth/login" />} />
      </Routes>
    </Suspense>
  );
}
