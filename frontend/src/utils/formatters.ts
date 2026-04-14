/**
 * Locale-aware date and number formatting utilities using the Intl API.
 */

/**
 * Formats a date string or Date object to a locale-appropriate string
 * using Intl.DateTimeFormat.
 *
 * @param date - ISO 8601 date string or Date object
 * @param locale - BCP 47 locale string (e.g. 'en-US', 'fr-FR')
 * @param options - Optional Intl.DateTimeFormatOptions overrides
 * @returns Formatted date string
 */
export function formatDate(
  date: string | Date,
  locale: string = 'en-US',
  options?: Intl.DateTimeFormatOptions,
): string {
  const dateObj = typeof date === 'string' ? new Date(date) : date;

  if (isNaN(dateObj.getTime())) {
    return '';
  }

  const defaultOptions: Intl.DateTimeFormatOptions = {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  };

  return new Intl.DateTimeFormat(locale, options ?? defaultOptions).format(dateObj);
}

/**
 * Formats a number to a locale-appropriate string using Intl.NumberFormat.
 *
 * @param value - The number to format
 * @param locale - BCP 47 locale string (e.g. 'en-US', 'fr-FR')
 * @param options - Optional Intl.NumberFormatOptions overrides
 * @returns Formatted number string
 */
export function formatNumber(
  value: number,
  locale: string = 'en-US',
  options?: Intl.NumberFormatOptions,
): string {
  return new Intl.NumberFormat(locale, options).format(value);
}
