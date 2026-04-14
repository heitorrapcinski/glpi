import {
  useQuery,
  useMutation,
  useQueryClient,
  keepPreviousData,
} from '@tanstack/react-query';
import api from '../api/client';
import { KNOWLEDGE } from '../api/endpoints';

// ---------------------------------------------------------------------------
// Types
// ---------------------------------------------------------------------------

export interface KnowbaseItem {
  id: string;
  title: string;
  answer: string;
  authorId: string;
  authorName?: string;
  isFaq: boolean;
  viewCount: number;
  categoryIds: string[];
  linkedItems: LinkedItem[];
  beginDate: string | null;
  endDate: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface LinkedItem {
  itemType: string;
  itemId: string;
}

export interface KnowledgeListParams {
  page?: number;
  pageSize?: number;
  sort?: string;
  order?: 'asc' | 'desc';
  categoryId?: string;
  isFaq?: boolean;
  search?: string;
}

// ---------------------------------------------------------------------------
// Query key factory
// ---------------------------------------------------------------------------
export const knowledgeKeys = {
  all: ['knowledge'] as const,
  lists: () => [...knowledgeKeys.all, 'list'] as const,
  list: (params: KnowledgeListParams) =>
    [...knowledgeKeys.lists(), params] as const,
  details: () => [...knowledgeKeys.all, 'detail'] as const,
  detail: (id: string | number) => [...knowledgeKeys.details(), id] as const,
};

// ---------------------------------------------------------------------------
// Hooks
// ---------------------------------------------------------------------------

/** Paginated knowledge article list */
export function useKnowledgeList(params: KnowledgeListParams = {}) {
  return useQuery({
    queryKey: knowledgeKeys.list(params),
    queryFn: () =>
      api.get<KnowbaseItem[]>(
        KNOWLEDGE.LIST,
        params as Record<string, unknown>,
      ),
    placeholderData: keepPreviousData,
  });
}

/** Single knowledge article detail */
export function useKnowledgeDetail(id: string | number) {
  return useQuery({
    queryKey: knowledgeKeys.detail(id),
    queryFn: () => api.get<KnowbaseItem>(KNOWLEDGE.DETAIL(id)),
    enabled: Boolean(id),
  });
}

/** Search knowledge articles */
export function useKnowledgeSearch(
  query: string,
  params: Omit<KnowledgeListParams, 'search'> = {},
) {
  return useQuery({
    queryKey: [...knowledgeKeys.lists(), 'search', query, params],
    queryFn: () =>
      api.get<KnowbaseItem[]>(KNOWLEDGE.LIST, {
        ...params,
        search: query,
      } as Record<string, unknown>),
    enabled: query.length > 0,
    placeholderData: keepPreviousData,
  });
}

/** Increment view counter for a knowledge article */
export function useIncrementKnowledgeView() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string | number) =>
      api.post<void>(KNOWLEDGE.VIEW(id)),
    onSuccess: (_data, id) => {
      queryClient.invalidateQueries({
        queryKey: knowledgeKeys.detail(id),
      });
    },
  });
}
