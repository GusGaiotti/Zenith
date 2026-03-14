"use client";

import { useMutation } from "@tanstack/react-query";
import { login, logout, register } from "@/lib/api/auth";
import { useAuthStore } from "@/lib/store/auth.store";
import type { LoginRequest, RegisterRequest } from "@/types/api";

function mapUser(data: { userId: number; email: string; displayName: string; aiAccessAllowed: boolean }) {
  return { id: data.userId, email: data.email, displayName: data.displayName, aiAccessAllowed: data.aiAccessAllowed };
}

export function useAuthHydration() {
  const hydrate = useAuthStore((state) => state.hydrate);
  const hydrated = useAuthStore((state) => state.hydrated);
  return { hydrate, hydrated };
}

export function useLogin() {
  const setAuth = useAuthStore((state) => state.setAuth);
  return useMutation({
    mutationFn: (body: LoginRequest) => login(body).then((response) => response.data),
    onSuccess: (data) => {
      setAuth({
        user: mapUser(data),
        accessToken: data.accessToken,
      });
    },
  });
}

export function useRegister() {
  const setAuth = useAuthStore((state) => state.setAuth);
  return useMutation({
    mutationFn: (body: RegisterRequest) => register(body).then((response) => response.data),
    onSuccess: (data) => {
      setAuth({
        user: mapUser(data),
        accessToken: data.accessToken,
      });
    },
  });
}

export function useLogout() {
  const clear = useAuthStore((state) => state.logout);
  return useMutation({
    mutationFn: () => logout().then((response) => response.data),
    onSettled: () => clear(),
  });
}
