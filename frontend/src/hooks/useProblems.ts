import {
  useQuery,
  useMutation,
  useQueryClient,
  keepPreviousData,
} from '@tanstack/react-query';
import api from '../api/client';
import { PROBLEMS } from '../api/endpoints';
import type {
  Actor,
  Followup,
  Task,
  Solution,
  FollowupFormData,
  TaskFormData,
  SolutionFormData,
} from './useTickets';

// ---------------------------------------------------------------------------
// Types
// ---------------------------------------------------------------------------

export interface Problem {
  id: string;
  status: number;
  title: string;
  content: string;
  entityId: string;
  priority: number;
  urgency: number;
  impact: number;
  actors: Actor[];
  linkedTicketIds: string[];
  linkedAssets: { assetType: string; assetId: string }[];
  impactContent: string;
  causeContent: string;
  symptomContent: string;
  followups: Followup[];
  tasks: Task[];
  solution: Solution | null;
  createdAt: string;
  updatedAt: string;
  solvedAt: string | null;
  closedAt: string | null;
}

export interface ProblemListParams {
  page?: number;
  pageSize?: number;
  sort?: string;
  order?: 'asc' | 'desc';
  status?: number | number[];
  priority?: number | number[];
  entityId?: string;
  assignedUserId?: string;
  search?: string;
}

export interface ProblemCreateData {
  title: string;
  content: string;
  urgency: number;
  impact: number;
  categoryId?: string;
  actors?: { actorType: number; actorKind: string; actorId: string }[];
  linkedTicketIds?: string[];
}

export interface ProblemUpdateData {
  status?: number;
  title?: string;
  content?: string;
  priority?: number;
  urgency?: number;
  impact?: number;
  categoryId?: string;
  impactContent?: string;
  causeContent?: string;
  symptomContent?: string;
  actors?: { actorType: number; actorKind: string; actorId: string }[];
}

// ---------------------------------------------------------------------------
// Query key factory
// ---------------------------------------------------------------------------
export const problemKeys = {
  all: ['problems'] as const,
  lists: () => [...problemKeys.all, 'list'] as const,
  list: (params: ProblemListParams) =>
    [...problemKeys.lists(), params] as const,
  details: () => [...problemKeys.all, 'detail'] as const,
  detail: (id: string | number) => [...problemKeys.details(), id] as const,
};

// ---------------------------------------------------------------------------
// Hooks
// ---------------------------------------------------------------------------

/** Paginated, filtered, sorted problem list */
export function useProblemList(params: ProblemListParams = {}) {
  return useQuery({
    queryKey: problemKeys.list(params),
    queryFn: () =>
      api.get<Problem[]>(PROBLEMS.LIST, params as Record<string, unknown>),
    placeholderData: keepPreviousData,
  });
}

/** Single problem detail */
export function useProblemDetail(id: string | number) {
  return useQuery({
    queryKey: problemKeys.detail(id),
    queryFn: () => api.get<Problem>(PROBLEMS.DETAIL(id)),
    enabled: Boolean(id),
  });
}

/** Create a new problem */
export function useCreateProblem() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: ProblemCreateData) =>
      api.post<Problem>(PROBLEMS.LIST, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: problemKeys.lists() });
    },
  });
}

/** Update an existing problem (PATCH) */
export function useUpdateProblem(id: string | number) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: ProblemUpdateData) =>
      api.patch<Problem>(PROBLEMS.DETAIL(id), data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: problemKeys.detail(id) });
      queryClient.invalidateQueries({ queryKey: problemKeys.lists() });
    },
  });
}

/** Add a followup to a problem */
export function useAddProblemFollowup(problemId: string | number) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: FollowupFormData) =>
      api.post<Followup>(PROBLEMS.FOLLOWUPS(problemId), data),
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: problemKeys.detail(problemId),
      });
    },
  });
}

/** Add a task to a problem */
export function useAddProblemTask(problemId: string | number) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: TaskFormData) =>
      api.post<Task>(PROBLEMS.TASKS(problemId), data),
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: problemKeys.detail(problemId),
      });
    },
  });
}

/** Add a solution to a problem */
export function useAddProblemSolution(problemId: string | number) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: SolutionFormData) =>
      api.post<Solution>(PROBLEMS.SOLUTIONS(problemId), data),
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: problemKeys.detail(problemId),
      });
    },
  });
}
