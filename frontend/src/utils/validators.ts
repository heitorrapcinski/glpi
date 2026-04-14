/**
 * Validation error mapping utilities for inline form field error display.
 */

/**
 * A single field-level validation error from the API.
 */
export interface FieldError {
  field: string;
  message: string;
}

/**
 * Converts API validation errors to a field-keyed record for inline display.
 *
 * Accepts either:
 * - A `Record<string, string[]>` (as returned by ApiError.details)
 * - An array of `{ field, message }` objects
 *
 * Returns a `Record<string, string[]>` where each key is a field name
 * and the value is an array of error messages for that field.
 */
export function mapValidationErrors(
  errors: Record<string, string[]> | FieldError[],
): Record<string, string[]> {
  if (Array.isArray(errors)) {
    const result: Record<string, string[]> = {};

    for (const { field, message } of errors) {
      if (!result[field]) {
        result[field] = [];
      }
      result[field].push(message);
    }

    return result;
  }

  // Already in Record<string, string[]> format — return a shallow copy
  const result: Record<string, string[]> = {};
  for (const [field, messages] of Object.entries(errors)) {
    result[field] = [...messages];
  }
  return result;
}
