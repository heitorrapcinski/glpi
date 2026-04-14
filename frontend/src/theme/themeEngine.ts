/**
 * Theme Engine — Runtime palette switching for GLPI PWA Frontend.
 *
 * Replicates the legacy GLPI palette system by setting data attributes
 * on the document root element. CSS palette files under `palettes/`
 * use `[data-glpi-theme="{name}"]` selectors to apply token overrides.
 */

/** Palettes that activate dark mode (`data-glpi-theme-dark="1"`). */
export const DARK_PALETTES: readonly string[] = [
  'dark',
  'darker',
  'midnight',
  'auror_dark',
] as const;

/** All 18 supported palette names from the legacy GLPI theme system. */
export const SUPPORTED_PALETTES: readonly string[] = [
  'default',
  'classic',
  'dark',
  'darker',
  'midnight',
  'auror',
  'auror_dark',
  'teclib',
  'aerialgreen',
  'automn',
  'clockworkorange',
  'flood',
  'greenflat',
  'hipster',
  'icecream',
  'lightblue',
  'premiumred',
  'purplehaze',
  'vintage',
] as const;

/**
 * Apply a theme palette to the document root.
 *
 * Sets `data-glpi-theme` to the palette name and `data-glpi-theme-dark`
 * to `"1"` for dark palettes or `"0"` for light palettes.
 */
export function applyTheme(themeName: string): void {
  document.documentElement.setAttribute('data-glpi-theme', themeName);
  const isDark = DARK_PALETTES.includes(themeName);
  document.documentElement.setAttribute('data-glpi-theme-dark', isDark ? '1' : '0');
}
