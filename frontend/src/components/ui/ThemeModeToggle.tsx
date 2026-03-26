import DarkModeRoundedIcon from '@mui/icons-material/DarkModeRounded';
import LightModeRoundedIcon from '@mui/icons-material/LightModeRounded';
import IconButton from '@mui/material/IconButton';
import Tooltip from '@mui/material/Tooltip';
import { useTranslation } from 'react-i18next';
import { useColorMode } from '../../hooks/useColorMode';

export function ThemeModeToggle() {
  const { mode, toggleMode } = useColorMode();
  const { t } = useTranslation();

  return (
    <Tooltip title={t('settings.darkMode')}>
      <IconButton color="inherit" onClick={toggleMode} aria-label={t('settings.darkMode')}>
        {mode === 'dark' ? <LightModeRoundedIcon /> : <DarkModeRoundedIcon />}
      </IconButton>
    </Tooltip>
  );
}
