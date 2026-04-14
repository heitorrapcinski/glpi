import { describe, it, expect } from 'vitest';
import { mapValidationErrors, type FieldError } from './validators';

describe('mapValidationErrors', () => {
  describe('with FieldError array input', () => {
    it('maps a single error per field', () => {
      const errors: FieldError[] = [
        { field: 'title', message: 'Title is required' },
        { field: 'content', message: 'Content is required' },
      ];

      const result = mapValidationErrors(errors);

      expect(result).toEqual({
        title: ['Title is required'],
        content: ['Content is required'],
      });
    });

    it('groups multiple errors for the same field', () => {
      const errors: FieldError[] = [
        { field: 'email', message: 'Email is required' },
        { field: 'email', message: 'Email format is invalid' },
        { field: 'name', message: 'Name is required' },
      ];

      const result = mapValidationErrors(errors);

      expect(result).toEqual({
        email: ['Email is required', 'Email format is invalid'],
        name: ['Name is required'],
      });
    });

    it('returns empty record for empty array', () => {
      expect(mapValidationErrors([])).toEqual({});
    });
  });

  describe('with Record<string, string[]> input', () => {
    it('returns a copy of the record', () => {
      const errors: Record<string, string[]> = {
        title: ['Title is required'],
        urgency: ['Invalid urgency value'],
      };

      const result = mapValidationErrors(errors);

      expect(result).toEqual(errors);
      // Ensure it's a copy, not the same reference
      expect(result).not.toBe(errors);
      expect(result.title).not.toBe(errors.title);
    });

    it('returns empty record for empty object', () => {
      expect(mapValidationErrors({})).toEqual({});
    });
  });
});
