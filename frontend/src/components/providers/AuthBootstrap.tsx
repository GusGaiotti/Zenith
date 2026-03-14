"use client";

import { AxiosError } from "axios";
import { useEffect, useRef } from "react";
import { useAuthHydration } from "@/hooks/useAuth";
import api from "@/lib/api/axios";
import { getMyLedger } from "@/lib/api/ledger";
import { useAuthStore } from "@/lib/store/auth.store";
import type { AuthResponse } from "@/types/api";

export function AuthBootstrap() {
  const { hydrate } = useAuthHydration();
  const hydrated = useAuthStore((state) => state.hydrated);
  const authResolved = useAuthStore((state) => state.authResolved);
  const setSession = useAuthStore((state) => state.setSession);
  const setAuthResolved = useAuthStore((state) => state.setAuthResolved);
  const setActiveLedger = useAuthStore((state) => state.setActiveLedger);
  const bootstrapInFlight = useRef(false);

  useEffect(() => {
    hydrate();
  }, [hydrate]);

  useEffect(() => {
    if (!hydrated || authResolved || bootstrapInFlight.current) {
      return;
    }
    bootstrapInFlight.current = true;

    const loadLedger = async () => {
      try {
        const response = await getMyLedger();
        setActiveLedger(response.data.id);
      } catch (error) {
        if (error instanceof AxiosError && error.response?.status === 404) {
          setActiveLedger(null);
          return;
        }

        setActiveLedger(null);
      }
    };

    const bootstrap = async () => {
      try {
        const accessToken = useAuthStore.getState().accessToken;

        if (!accessToken) {
          const response = await api.post<AuthResponse>("/auth/refresh");
          const data = response.data;
          setSession({
            user: {
              id: data.userId,
              email: data.email,
              displayName: data.displayName,
              aiAccessAllowed: data.aiAccessAllowed,
            },
            accessToken: data.accessToken,
          });
        }

        await loadLedger();
      } catch {
        setActiveLedger(null);
      } finally {
        setAuthResolved(true);
        bootstrapInFlight.current = false;
      }
    };

    void bootstrap();
  }, [authResolved, hydrated, setActiveLedger, setAuthResolved, setSession]);

  return null;
}
