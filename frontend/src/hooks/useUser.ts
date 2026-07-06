"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { deleteMyAccount, getMyProfile, updateMyProfile } from "@/lib/api/users";
import { useAuthStore } from "@/lib/store/auth.store";
import type { UpdateProfileRequest } from "@/types/api";

export function useMyProfile() {
  return useQuery({
    queryKey: ["users", "me"],
    queryFn: () => getMyProfile().then((r) => r.data),
  });
}

export function useUpdateProfile() {
  const queryClient = useQueryClient();
  const setAuth = useAuthStore((state) => state.setAuth);
  const user = useAuthStore((state) => state.user);
  const accessToken = useAuthStore((state) => state.accessToken);

  return useMutation({
    mutationFn: (body: UpdateProfileRequest) => updateMyProfile(body).then((r) => r.data),
    onSuccess: (data) => {
      queryClient.setQueryData(["users", "me"], data);
      if (user && accessToken) {
        setAuth({
          user: { ...user, displayName: data.displayName },
          accessToken,
        });
      }
    },
  });
}

export function useDeleteAccount() {
  const logout = useAuthStore((state) => state.logout);
  return useMutation({
    mutationFn: () => deleteMyAccount().then((r) => r.data),
    onSuccess: () => logout(),
  });
}
