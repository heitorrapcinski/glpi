import {
  useQuery,
  useMutation,
  useQueryClient,
  keepPreviousData,
} from '@tanstack/react-query';
import api from '../api/client';
import { CHANGES } from '../api/endpoints';
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

export interface PlanningDocuments {
  impactContent: string;
  controlListContent: string;
  rolloutPlanContent: string;
  backoutPlanContent: string;
  checklistContent: string;
}

export interface ValidationStep {
  id: string;
  validatorId: string;
  validatorKind: 'user' | 'group';
  validatorName?: string;
  status: number;
  comment: string | null;
  order: number;
}

export interface Change {
  id: string;
  status: number;
  title: string;
  content: string;
  entityId: string;
  priority: number;
  urgency: number;
  impact: number;
  actors: Actor[];
  planningDocuments: PlanningDocuments;
  validationSteps: ValidationStep[];
  linkedTicketIds: string[];
  linkedProblemIds: string[];
  linkedAssets: { assetType: string; assetId: string }[];
  followups: Followup[];
  tasks: Task[];
  solution: Solution | null;
  createdAt: string;
  updatedAt: string;
  closedAt: string | null;
}

export interface ChangeListParams {
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

export interface ChangeCreateData {
  title: string;
  content: string;
  urgency: number;
  impact: number;
  categoryId?: string;
  actors?: { actorType: number; actorKind: string; actorId: string }[];
  linkedTicketIds?: string[];
  linkedProblemIds?: string[];
}

export interface ChangeUpdateData {
  status?: number;
  title?: string;
  content?: string;
  priority?: number;
  urgency?: number;
  impact?: number;
  categoryId?: string;
  planningDocuments?: Partial<PlanningDocuments>;
  actors?: { actorType: number; actorKind: string; actorId: string }[];
}

export interface ValidationActionData {
  status: number; // 2 = Approved, 3 = Refused
  comment?: string;
}

// ---------------------------------------------------------------------------
// Query key factory
// ---------------------------------------------------------------------------
export const changeKeys = {
  all: ['changes'] as const,
  lists: () => [...changeKeys.all, 'list'] as const,
  list: (params: ChangeListParams) => [...changeKeys.lists(), params] as const,
  details: () => [...changeKeys.all, 'detail'] as const,
  detail: (id: string | number) => [...changeKeys.details(), id] as const,
};

// ---------------------------------------------------------------------------
// Hooks
// ---------------------------------------------------------------------------

/** Paginated, filtered, sorted change list */
export function useChangeList(params: ChangeListParams = {}) {
  return useQuery({
    queryKey: changeKeys.list(params),
    queryFn: () =>
      api.get<Change[]>(CHANGES.LIST, params as Record<string, unknown>),
    placeholderData: keepPreviousData,
  });
}

/** Single change detail */
export function useChangeDetail(id: string | number) {
  return useQuery({
    queryKey: changeKeys.detail(id),
    queryFn: () => api.get<Change>(CHANGES.DETAIL(id)),
    enabled: Boolean(id),
  });
}

/** Create a new change */
export function useCreateChange() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: ChangeCreateData) =>
      api.post<Change>(CHANGES.LIST, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: changeKeys.lists() });
    },
  });
}

/** Update an existing change (PATCH) */
export function useUpdateChange(id: string | number) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: ChangeUpdateData) =>
      api.patch<Change>(CHANGES.DETAIL(id), data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: changeKeys.detail(id) });
      queryClient.invalidateQueries({ queryKey: changeKeys.lists() });
    },
  });
}

/** Add a followup to a change */
export function useAddChangeFollowup(changeId: string | number) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: FollowupFormData) =>
      api.post<Followup>(CHANGES.FOLLOWUPS(changeId), data),
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: changeKeys.detail(changeId),
      });
    },
  });
}

/** Add a task to a change */
export function useAddChangeTask(changeId: string | number) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: TaskFormData) =>
      api.post<Task>(CHANGES.TASKS(changeId), data),
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: changeKeys.detail(changeId),
      });
    },
  });
}

/** Add a solution to a change */
export function useAddChangeSolution(changeId: string | number) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: SolutionFormData) =>
      api.post<Solution>(CHANGES.SOLUTIONS(changeId), data),
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: changeKeys.detail(changeId),
      });
    },
  });
}

/** Approve or refuse a validation step on a change */
export function useChangeValidationAction(changeId: string | number) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: ValidationActionData & { validationId: string }) =>
      api.patch<ValidationStep>(
        `${CHANGES.VALIDATIONS(changeId)}/${data.validationId}`,
        { status: data.status, comment: data.comment },
      ),
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: changeKeys.detail(changeId),
      });
    },
  });
}
