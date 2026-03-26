import ArrowDownwardRoundedIcon from '@mui/icons-material/ArrowDownwardRounded';
import ArrowUpwardRoundedIcon from '@mui/icons-material/ArrowUpwardRounded';
import Card from '@mui/material/Card';
import CardContent from '@mui/material/CardContent';
import Chip from '@mui/material/Chip';
import Stack from '@mui/material/Stack';
import Typography from '@mui/material/Typography';

interface StatCardProps {
  label: string;
  value: string;
  delta: string;
  positive: boolean;
}

export function StatCard({ label, value, delta, positive }: StatCardProps) {
  return (
    <Card className="glass-card">
      <CardContent>
        <Stack spacing={1.2}>
          <Typography variant="body2" color="text.secondary">
            {label}
          </Typography>
          <Typography variant="h5">{value}</Typography>
          <Chip
            size="small"
            icon={positive ? <ArrowUpwardRoundedIcon /> : <ArrowDownwardRoundedIcon />}
            label={delta}
            color={positive ? 'success' : 'error'}
            sx={{ width: 'fit-content' }}
          />
        </Stack>
      </CardContent>
    </Card>
  );
}
