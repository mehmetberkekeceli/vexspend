import BoltRoundedIcon from '@mui/icons-material/BoltRounded';
import Alert from '@mui/material/Alert';
import Card from '@mui/material/Card';
import CardContent from '@mui/material/CardContent';
import Divider from '@mui/material/Divider';
import LinearProgress from '@mui/material/LinearProgress';
import List from '@mui/material/List';
import ListItem from '@mui/material/ListItem';
import ListItemText from '@mui/material/ListItemText';
import Skeleton from '@mui/material/Skeleton';
import Stack from '@mui/material/Stack';
import Typography from '@mui/material/Typography';
import Box from '@mui/material/Box';
import { useQuery } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { getDashboardReport } from '../api/reportApi';
import { formatMoney } from '../lib/format';
import { StatCard } from '../components/ui/StatCard';

export function DashboardPage() {
  const { t, i18n } = useTranslation();
  const locale = i18n.resolvedLanguage?.startsWith('tr') ? 'tr-TR' : 'en-US';

  const { data, isLoading, isError } = useQuery({
    queryKey: ['dashboard-report'],
    queryFn: () => getDashboardReport(3),
  });

  const currencyCode = data?.accountBalances[0]?.currencyCode ?? 'USD';
  const totalIncome = data?.totalIncome ?? 0;
  const totalExpense = data?.totalExpense ?? 0;
  const netCashflow = data?.netCashflow ?? 0;
  const savingsRateValue = totalIncome > 0 ? (netCashflow / totalIncome) * 100 : 0;

  const stats = data
    ? [
        {
          label: t('dashboard.stats.income'),
          value: formatMoney(totalIncome, currencyCode, locale),
          delta: `${(totalIncome > 0 ? '+' : '')}${totalIncome.toFixed(2)}`,
          positive: totalIncome >= 0,
        },
        {
          label: t('dashboard.stats.expense'),
          value: formatMoney(totalExpense, currencyCode, locale),
          delta: `${(totalExpense > 0 ? '+' : '')}${totalExpense.toFixed(2)}`,
          positive: totalExpense <= totalIncome,
        },
        {
          label: t('dashboard.stats.netCashflow'),
          value: formatMoney(netCashflow, currencyCode, locale),
          delta: `${(netCashflow > 0 ? '+' : '')}${netCashflow.toFixed(2)}`,
          positive: netCashflow >= 0,
        },
        {
          label: t('dashboard.stats.savingsRate'),
          value: `${savingsRateValue.toFixed(1)}%`,
          delta: `${savingsRateValue >= 0 ? '+' : ''}${savingsRateValue.toFixed(1)}%`,
          positive: savingsRateValue >= 0,
        },
      ]
    : [];

  const budgetUsage = data?.budgetLimitTotal ? (data.budgetSpentTotal / data.budgetLimitTotal) * 100 : 0;

  return (
    <Stack spacing={3}>
      <Box>
        <Typography variant="h4">{t('dashboard.title')}</Typography>
        <Typography color="text.secondary">{t('dashboard.subtitle')}</Typography>
      </Box>

      <Box
        sx={{
          display: 'grid',
          gap: 2,
          gridTemplateColumns: {
            xs: '1fr',
            md: 'repeat(2, minmax(0, 1fr))',
            xl: 'repeat(4, minmax(0, 1fr))',
          },
        }}
      >
        {isLoading &&
          Array.from({ length: 4 }).map((_, index) => (
            <Card className="glass-card" key={`dashboard-stat-skeleton-${index}`}>
              <CardContent>
                <Skeleton height={90} />
              </CardContent>
            </Card>
          ))}
        {!isLoading &&
          stats.map((stat) => (
            <StatCard
              key={stat.label}
              label={stat.label}
              value={stat.value}
              delta={stat.delta}
              positive={stat.positive}
            />
          ))}
      </Box>

      {isError && <Alert severity="error">{t('errors.dashboardLoadFailed')}</Alert>}

      <Box
        sx={{
          display: 'grid',
          gap: 2,
          gridTemplateColumns: {
            xs: '1fr',
            lg: '1.2fr 0.8fr',
          },
        }}
      >
        <Card className="glass-card">
          <CardContent>
            <Stack spacing={2}>
              <Typography variant="h6">{t('dashboard.usageTitle')}</Typography>
              <Typography color="text.secondary">
                {data
                  ? t('dashboard.usageDescriptionDynamic', {
                      percent: Math.round(budgetUsage),
                    })
                  : t('dashboard.usageDescription')}
              </Typography>
              <LinearProgress color="secondary" variant="determinate" value={Math.min(100, Math.max(0, budgetUsage))} />
            </Stack>
          </CardContent>
        </Card>

        <Card className="glass-card">
          <CardContent>
            <Stack direction="row" justifyContent="space-between" alignItems="center" sx={{ mb: 1 }}>
              <Typography variant="h6">{t('dashboard.accountsTitle')}</Typography>
              <BoltRoundedIcon color="secondary" />
            </Stack>
            <List disablePadding>
              {(data?.accountBalances ?? []).map((item, index) => (
                <Box key={item.accountId}>
                  <ListItem disableGutters>
                    <ListItemText
                      primary={item.accountName}
                      secondary={formatMoney(item.currentBalance, item.currencyCode, locale)}
                    />
                  </ListItem>
                  {index !== (data?.accountBalances.length ?? 0) - 1 && <Divider />}
                </Box>
              ))}
              {!data?.accountBalances?.length && !isLoading && (
                <ListItem disableGutters>
                  <ListItemText primary={t('common.emptyState')} />
                </ListItem>
              )}
            </List>
          </CardContent>
        </Card>
      </Box>
    </Stack>
  );
}
