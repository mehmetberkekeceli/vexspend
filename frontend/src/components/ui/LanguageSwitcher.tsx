import TranslateRoundedIcon from '@mui/icons-material/TranslateRounded';
import FormControl from '@mui/material/FormControl';
import InputLabel from '@mui/material/InputLabel';
import MenuItem from '@mui/material/MenuItem';
import Select, { type SelectChangeEvent } from '@mui/material/Select';
import { useTranslation } from 'react-i18next';

export function LanguageSwitcher() {
  const { t, i18n } = useTranslation();

  const currentLanguage = i18n.resolvedLanguage?.startsWith('en') ? 'en' : 'tr';

  const handleLanguageChange = (event: SelectChangeEvent<string>) => {
    void i18n.changeLanguage(event.target.value);
  };

  return (
    <FormControl size="small" sx={{ minWidth: 130 }}>
      <InputLabel id="language-switcher-label">{t('settings.language')}</InputLabel>
      <Select
        labelId="language-switcher-label"
        value={currentLanguage}
        label={t('settings.language')}
        onChange={handleLanguageChange}
        startAdornment={<TranslateRoundedIcon sx={{ mr: 1, ml: 0.5, fontSize: 18 }} />}
      >
        <MenuItem value="tr">Turkce</MenuItem>
        <MenuItem value="en">English</MenuItem>
      </Select>
    </FormControl>
  );
}
