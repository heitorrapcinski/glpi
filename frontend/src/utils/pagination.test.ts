import { describe, it, expect } from 'vitest';
import { computePaginationMeta, truncateSearchResults } from './pagination';

describe('computePaginationMeta', () => {
  it('computes totalPages as ceil(totalElements / pageSize)', () => {
    const meta = computePaginationMeta(101, 10, 1);
    expect(meta.totalPages).toBe(11);
    expect(meta.currentPage).toBe(1);
    expect(meta.pageSize).toBe(10);
    expect(meta.totalElements).toBe(101);
  });

  it('returns totalPages=1 and currentPage=1 when totalElements is 0', () => {
    const meta = computePaginationMeta(0, 50, 1);
    expect(meta.totalPages).toBe(1);
    expect(meta.currentPage).toBe(1);
  });

  it('clamps currentPage to 1 when requested page is below 1', () => {
    const meta = computePaginationMeta(100, 10, -5);
    expect(meta.currentPage).toBe(1);
  });

  it('clamps currentPage to totalPages when requested page exceeds total', () => {
    const meta = computePaginationMeta(50, 10, 999);
    expect(meta.totalPages).toBe(5);
    expect(meta.currentPage).toBe(5);
  });

  it('handles exact division without rounding up', () => {
    const meta = computePaginationMeta(100, 25, 2);
    expect(meta.totalPages).toBe(4);
    expect(meta.currentPage).toBe(2);
  });

  it('handles single element', () => {
    const meta = computePaginationMeta(1, 50, 1);
    expect(meta.totalPages).toBe(1);
    expect(meta.currentPage).toBe(1);
  });
});

describe('truncateSearchResults', () => {
  it('truncates categories exceeding maxPerCategory (default 5)', () => {
    const grouped = {
      tickets: [1, 2, 3, 4, 5, 6, 7],
      assets: [10, 20],
    };
    const result = truncateSearchResults(grouped);

    expect(result.tickets.items).toEqual([1, 2, 3, 4, 5]);
    expect(result.tickets.hasMore).toBe(true);
    expect(result.assets.items).toEqual([10, 20]);
    expect(result.assets.hasMore).toBe(false);
  });

  it('uses custom maxPerCategory', () => {
    const grouped = { items: ['a', 'b', 'c', 'd'] };
    const result = truncateSearchResults(grouped, 2);

    expect(result.items.items).toEqual(['a', 'b']);
    expect(result.items.hasMore).toBe(true);
  });

  it('sets hasMore=false when count equals maxPerCategory', () => {
    const grouped = { exact: [1, 2, 3, 4, 5] };
    const result = truncateSearchResults(grouped, 5);

    expect(result.exact.items).toEqual([1, 2, 3, 4, 5]);
    expect(result.exact.hasMore).toBe(false);
  });

  it('handles empty categories', () => {
    const grouped = { empty: [] as number[] };
    const result = truncateSearchResults(grouped);

    expect(result.empty.items).toEqual([]);
    expect(result.empty.hasMore).toBe(false);
  });

  it('handles empty input object', () => {
    const result = truncateSearchResults({});
    expect(Object.keys(result)).toHaveLength(0);
  });
});
