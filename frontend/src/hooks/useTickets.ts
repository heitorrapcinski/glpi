import {
  useQuery,
  useMutation,
  useQueryClient,
  keepPreviousData,
} from '@tanstack/react-query';
import api from '../api/client';
import { TICKETS } from '../api/endpoints';

// ---------------------------------------------------------------------------
// Types (mirrors design-doc data models)
// ---------------------------------------------------------------------------

export interface Ticket {
  id: string;
  type: number;
  status: number;
  title: string;
  content: string;
  entityId: string;
  priority: number;
  urgency: number;
  impact: number;
  categoryId: string | null;
  isDeleted: boolean;
  actors: Actor[];
  followups: Followup[];
  tasks: Task[];
  solution: Solution | null;
  validations: Validation[];
  sla: SlaContext | null;
  priorityManualOverride: boolean;
  createdAt: string;
  updatedAt: string;
  solvedAt: string | null;
  closedAt: string | null;
}

export interface Actor {
  actorType: number;
  actorKind: 'user' | 'group' | 'supplier';
  actorId: string;
  useNotification: boolean;
  displayName?: string;
}

export interface Followup {
  id: string;
  content: string;
  authorId: string;
  authorName?: string;
  isPrivate: boolean;
  source: string;
  createdAt: string;
}

export interface Task {
  id: string;
  content: string;
  assignedUserId: string;
  assignedUserName?: string;
  status: number;
  isPrivate: boolean;
  plannedStart: string | null;
  plannedEnd: string | null;
  duration: number;
  createdAt: string;
}

export interface Solution {
  id: string;
  content: string;
  authorId: string;
  authorName?: string;
  status: 'pending' | 'accepted' | 'refused';
  createdAt: string;
}

export interface Validation {
  id: string;
  validatorId: string;
  validatorKind: 'user' | 'group';
  validatorName?: string;
  status: number;
  comment: string | null;
  order: number;
}

export interface SlaContext {
  deadline: string | null;
  totalDuration: number;
}

export interface TicketListParams {
  page?: number;
  pageSize?: number;
  sort?: string;
  order?: 'asc' | 'desc';
  status?: number | number[];
  priority?: number | number[];
  entityId?: string;
  assignedUserId?: string;
  categoryId?: string;
  search?: string;
}

export interface TicketCreateData {
  type: number;
  title: string;
  content: string;
  entityId: string;
  urgency: number;
  categoryId?: string;
  actors?: { actorType: number; actorKind: string; actorId: string }[];
}

export interface TicketUpdateData {
  status?: number;
  title?: string;
  content?: string;
  priority?: number;
  urgency?: number;
  impact?: number;
  categoryId?: string;
  actors?: { actorType: number; actorKind: string; actorId: string }[];
}

export interface FollowupFormData {
  content: string;
  isPrivate: boolean;
}

export interface TaskFormData {
  content: string;
  assignedUserId: string;
  status: number;
  plannedStart?: string;
  plannedEnd?: string;
  duration?: number;
  isPrivate: boolean;
}

export interface SolutionFormData {
  content: string;
  solutionTypeId?: string;
}

// ---------------------------------------------------------------------------
// Query key factory
// ---------------------------------------------------------------------------
export const ticketKeys = {
  all: ['tickets'] as const,
  lists: () => [...ticketKeys.all, 'list'] as const,
  list: (params: TicketListParams) => [...ticketKeys.lists(), params] as const,
  details: () => [...ticketKeys.all, 'detail'] as const,
  detail: (id: string | number) => [...ticketKeys.details(), id] as const,
};

// ---------------------------------------------------------------------------
// Hooks
// ---------------------------------------------------------------------------

/** Paginated, filtered, sorted ticket list */
export function useTicketList(params: TicketListParams = {}) {
  return useQuery({
    queryKey: ticketKeys.list(params),
    queryFn: () =>
      api.get<Ticket[]>(TICKETS.LIST, params as Record<string, unknown>),
    placeholderData: keepPreviousData,
  });
}

/** Single ticket detail */
export function useTicketDetail(id: string | number) {
  return useQuery({
    queryKey: ticketKeys.detail(id),
    queryFn: () => api.get<Ticket>(TICKETS.DETAIL(id)),
    enabled: Boolean(id),
  });
}

/** Create a new ticket */
export function useCreateTicket() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: TicketCreateData) =>
      api.post<Ticket>(TICKETS.LIST, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ticketKeys.lists() });
    },
  });
}

/** Update an existing ticket (PATCH) */
export function useUpdateTicket(id: string | number) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: TicketUpdateData) =>
      api.patch<Ticket>(TICKETS.DETAIL(id), data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ticketKeys.detail(id) });
      queryClient.invalidateQueries({ queryKey: ticketKeys.lists() });
    },
  });
}

/** Add a followup to a ticket */
export function useAddTicketFollowup(ticketId: string | number) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: FollowupFormData) =>
      api.post<Followup>(TICKETS.FOLLOWUPS(ticketId), data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ticketKeys.detail(ticketId) });
    },
  });
}

/** Add a task to a ticket */
export function useAddTicketTask(ticketId: string | number) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: TaskFormData) =>
      api.post<Task>(TICKETS.TASKS(ticketId), data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ticketKeys.detail(ticketId) });
    },
  });
}

/** Add a solution to a ticket */
export function useAddTicketSolution(ticketId: string | number) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: SolutionFormData) =>
      api.post<Solution>(TICKETS.SOLUTIONS(ticketId), data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ticketKeys.detail(ticketId) });
    },
  });
}
