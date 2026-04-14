import { useQuery, keepPreviousData } from '@tanstack/react-query';
import { useEffect, useRef, useState } from 'react';
import api from '../api/client';
import { SEARCH } from '../api/endpoints';

// ---------------------------------------------------------------------------
// Types
// ---------------------------------------------------------------------------

export interface SearchResult {
  id: string;
  type: string;
  title: string;
  status?: number;
  icon?: string;
}

export interface CategorizedSearchResults {
  tickets: SearchResult[];
  problems: SearchResult[];
  changes: SearchResult[];
  assets: SearchResult[];
  knowledge: SearchResult[];
}

export interface SearchResponse {
  results: CategorizedSearchResults;
  hasMore: Record<string, boolean>;
}

// ---------------------------------------------------------------------------
// Query key factory
// ---------------------------------------------------------------------------
export const searchKeys = {
  all: ['search'] as const,
  query: (q: string) => [...searchKeys.all, q] as const,
};

// ---------------------------------------------------------------------------
// Debounce hook
// ---------------------------------------------------------------------------

function useDebouncedValue<T>(value: T, delayMs: number): T {
  const [debounced, setDebounced] = useState(value);
  const timerRef = useRef<ReturnType<typeof setTimeout>>();

  useEffect(() => {
    timerRef.current = setTimeout(() => setDebounced(value), delayMs);
    return () => clearTimeout(timerRef.current);
  }, [value, delayMs]);

  return debounced;
}

// ---------------------------------------------------------------------------
// Hook
// ---------------------------------------------------------------------------

/**
 * Global search with 300ms debounce and categorized results.
 * Returns at most 5 results per category (server-side truncation expected).
 */
export function useGlobalSearch(rawQuery: string) {
  const query = useDebouncedValue(rawQuery.trim(), 300);

  const result = useQuery({
    queryKey: searchKeys.query(query),
    queryFn: () =>
      api.get<SearchResponse>(SEARCH.QUERY, {
        q: query,
        maxPerCategory: 5,
      }),
    enabled: query.length > 0,
    placeholderData: keepPreviousData,
  });

  return {
    ...result,
    debouncedQuery: query,
  };
}
