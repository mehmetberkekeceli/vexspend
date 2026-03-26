import type { PaletteMode } from '@mui/material';
import { createContext } from 'react';

export interface ColorModeContextValue {
  mode: PaletteMode;
  setMode: (nextMode: PaletteMode) => void;
  toggleMode: () => void;
}

export const ColorModeContext = createContext<ColorModeContextValue | undefined>(undefined);
