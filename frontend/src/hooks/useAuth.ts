import { useMutation, useQueryClient } from '@tanstack/react-query';
import { useAuthStore } from '../stores/authStore';

// ---------------------------------------------------------------------------
// Query key factory
// ---------------------------------------------------------------------------
export const authKeys = {
  all: ['auth'] as const,
  session: () => [...authKeys.all, 'session'] as const,
};

// ---------------------------------------------------------------------------
// Hook: useAuth
// ---------------------------------------------------------------------------

/**
 * Wraps authStore actions as TanStack Query mutations so that consumers get
 * loading / error / success states and automatic query invalidation.
 */
export function useAuth() {
  const queryClient = useQueryClient();

  const {
    user,
    isAuthenticated,
    isLoading,
    login: storeLogin,
    loginWith2FA: storeLoginWith2FA,
    logout: storeLogout,
    switchProfile: storeSwitchProfile,
    switchEntity: storeSwitchEntity,
  } = useAuthStore();

  const loginMutation = useMutation({
    mutationFn: (vars: {
      username: string;
      password: string;
      rememberMe: boolean;
    }) => storeLogin(vars.username, vars.password, vars.rememberMe),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: authKeys.all });
    },
  });

  const loginWith2FAMutation = useMutation({
    mutationFn: (vars: { totpCode: string }) =>
      storeLoginWith2FA(vars.totpCode),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: authKeys.all });
    },
  });

  const logoutMutation = useMutation({
    mutationFn: () => storeLogout(),
    onSuccess: () => {
      queryClient.clear();
    },
  });

  const switchProfileMutation = useMutation({
    mutationFn: (vars: { profileId: string }) =>
      storeSwitchProfile(vars.profileId),
    onSuccess: () => {
      queryClient.invalidateQueries();
    },
  });

  const switchEntityMutation = useMutation({
    mutationFn: (vars: { entityId: string }) =>
      storeSwitchEntity(vars.entityId),
    onSuccess: () => {
      queryClient.invalidateQueries();
    },
  });

  return {
    user,
    isAuthenticated,
    isLoading,

    login: loginMutation.mutateAsync,
    loginStatus: loginMutation,

    loginWith2FA: loginWith2FAMutation.mutateAsync,
    loginWith2FAStatus: loginWith2FAMutation,

    logout: logoutMutation.mutateAsync,
    logoutStatus: logoutMutation,

    switchProfile: switchProfileMutation.mutateAsync,
    switchProfileStatus: switchProfileMutation,

    switchEntity: switchEntityMutation.mutateAsync,
    switchEntityStatus: switchEntityMutation,
  };
}
