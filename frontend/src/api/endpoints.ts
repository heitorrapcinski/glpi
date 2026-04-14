// ---------------------------------------------------------------------------
// API endpoint constants for all backend services
// ---------------------------------------------------------------------------

// Auth
export const AUTH = {
  LOGIN: '/auth/login',
  REFRESH: '/auth/refresh',
  LOGOUT: '/auth/logout',
  TWO_FA: '/auth/2fa',
} as const;

// Identity
export const IDENTITY = {
  PROFILES: '/identity/profiles',
  ENTITIES: '/identity/entities',
  USERS: '/identity/users',
} as const;

// Tickets
export const TICKETS = {
  LIST: '/tickets',
  DETAIL: (id: string | number) => `/tickets/${id}`,
  FOLLOWUPS: (id: string | number) => `/tickets/${id}/followups`,
  TASKS: (id: string | number) => `/tickets/${id}/tasks`,
  SOLUTIONS: (id: string | number) => `/tickets/${id}/solutions`,
} as const;

// Problems
export const PROBLEMS = {
  LIST: '/problems',
  DETAIL: (id: string | number) => `/problems/${id}`,
  FOLLOWUPS: (id: string | number) => `/problems/${id}/followups`,
  TASKS: (id: string | number) => `/problems/${id}/tasks`,
  SOLUTIONS: (id: string | number) => `/problems/${id}/solutions`,
} as const;

// Changes
export const CHANGES = {
  LIST: '/changes',
  DETAIL: (id: string | number) => `/changes/${id}`,
  FOLLOWUPS: (id: string | number) => `/changes/${id}/followups`,
  TASKS: (id: string | number) => `/changes/${id}/tasks`,
  SOLUTIONS: (id: string | number) => `/changes/${id}/solutions`,
  VALIDATIONS: (id: string | number) => `/changes/${id}/validations`,
} as const;

// Assets
export const ASSETS = {
  LIST: '/assets',
  BY_TYPE: (type: string) => `/assets/${type}`,
  DETAIL: (type: string, id: string | number) => `/assets/${type}/${id}`,
  SOFTWARE: '/assets/software',
  LICENSES: '/assets/licenses',
} as const;

// Knowledge Base
export const KNOWLEDGE = {
  LIST: '/knowledge',
  DETAIL: (id: string | number) => `/knowledge/${id}`,
  VIEW: (id: string | number) => `/knowledge/${id}/view`,
} as const;

// Search
export const SEARCH = {
  QUERY: '/search',
} as const;

// Dashboard
export const DASHBOARD = {
  STATS: '/dashboard/stats',
} as const;
