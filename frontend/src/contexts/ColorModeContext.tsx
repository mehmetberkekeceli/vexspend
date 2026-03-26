import { CssBaseline, ThemeProvider } from '@mui/material';
import type { PaletteMode } from '@mui/material';
import { useCallback, useMemo, useState, type PropsWithChildren } from 'react';
import { ColorModeContext } from './colorModeContextValue';
import { createAppTheme } from '../theme/createAppTheme';

const STORAGE_KEY = 'vexspend-color-mode';

function resolveInitialMode(): PaletteMode {
  const fallbackMode: PaletteMode = 'dark';

  if (typeof window === 'undefined') {
    return fallbackMode;
  }

  const storedMode = window.localStorage.getItem(STORAGE_KEY);
  if (storedMode === 'dark' || storedMode === 'light') {
    return storedMode;
  }

  return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
}

export function ColorModeProvider({ children }: PropsWithChildren) {
  const [mode, setModeState] = useState<PaletteMode>(resolveInitialMode);

  const persistMode = (nextMode: PaletteMode) => {
    if (typeof window !== 'undefined') {
      window.localStorage.setItem(STORAGE_KEY, nextMode);
    }
  };

  const setMode = useCallback((nextMode: PaletteMode) => {
    setModeState(nextMode);
    persistMode(nextMode);
  }, []);

  const toggleMode = useCallback(() => {
    setModeState((previousMode) => {
      const nextMode = previousMode === 'dark' ? 'light' : 'dark';
      persistMode(nextMode);
      return nextMode;
    });
  }, []);

  const contextValue = useMemo(
    () => ({
      mode,
      setMode,
      toggleMode,
    }),
    [mode, setMode, toggleMode],
  );

  const theme = useMemo(() => createAppTheme(mode), [mode]);

  return (
    <ColorModeContext.Provider value={contextValue}>
      <ThemeProvider theme={theme}>
        <CssBaseline />
        {children}
      </ThemeProvider>
    </ColorModeContext.Provider>
  );
}
