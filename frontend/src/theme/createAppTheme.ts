import type { PaletteMode } from '@mui/material';
import { createTheme } from '@mui/material/styles';

export function createAppTheme(mode: PaletteMode) {
  const isDark = mode === 'dark';

  return createTheme({
    palette: {
      mode,
      primary: {
        main: isDark ? '#4dd0e1' : '#00695c',
      },
      secondary: {
        main: isDark ? '#ffb74d' : '#ef6c00',
      },
      background: {
        default: isDark ? '#0c1117' : '#f4f7fb',
        paper: isDark ? '#121a23' : '#ffffff',
      },
      success: {
        main: '#2e7d32',
      },
      error: {
        main: '#d32f2f',
      },
      warning: {
        main: '#ed6c02',
      },
    },
    shape: {
      borderRadius: 12,
    },
    typography: {
      fontFamily: '"Manrope", "Segoe UI", "Helvetica Neue", Arial, sans-serif',
      h4: {
        fontWeight: 700,
      },
      h5: {
        fontWeight: 700,
      },
      h6: {
        fontWeight: 700,
      },
      button: {
        fontWeight: 600,
        textTransform: 'none',
      },
    },
    components: {
      MuiPaper: {
        styleOverrides: {
          root: {
            backgroundImage: 'none',
          },
        },
      },
      MuiCard: {
        styleOverrides: {
          root: {
            border: isDark ? '1px solid rgba(255,255,255,0.08)' : '1px solid rgba(8,26,45,0.08)',
            boxShadow: isDark
              ? '0 18px 30px rgba(0, 0, 0, 0.28)'
              : '0 12px 24px rgba(8, 26, 45, 0.08)',
          },
        },
      },
      MuiAppBar: {
        styleOverrides: {
          root: {
            background: isDark ? 'rgba(9, 16, 24, 0.88)' : 'rgba(246, 249, 252, 0.88)',
            backdropFilter: 'blur(10px)',
            borderBottom: isDark
              ? '1px solid rgba(255,255,255,0.1)'
              : '1px solid rgba(8,26,45,0.08)',
          },
        },
      },
    },
  });
}
