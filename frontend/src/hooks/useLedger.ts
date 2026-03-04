"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { acceptInvitation, cancelInvitation, createLedger, declineInvitation, getLedger, inviteToLedger } from "@/lib/api/ledger";
import { queryKeys } from "@/lib/api/query-keys";
import { useAuthStore } from "@/lib/store/auth.store";
import { requireLedgerId } from "@/lib/utils/require-ledger-id";

export function useLedger() {
  const ledgerId = useAuthStore((state) => state.activeLedgerId);

  return useQuery({
    queryKey: ledgerId ? queryKeys.ledger(ledgerId) : ["ledger", "none"],
    queryFn: () => getLedger(requireLedgerId(ledgerId)).then((response) => response.data),
    enabled: Boolean(ledgerId),
  });
}

export function useCreateLedger() {
  const setActiveLedger = useAuthStore((state) => state.setActiveLedger);

  return useMutation({
    mutationFn: (name: string) => createLedger({ name }).then((response) => response.data),
    onSuccess: (data) => {
      setActiveLedger(data.id);
    },
  });
}

export function useInviteMember() {
  const ledgerId = useAuthStore((state) => state.activeLedgerId);
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (email: string) =>
      inviteToLedger(requireLedgerId(ledgerId), { email }).then((response) => response.data),
    onSuccess: () => {
      if (ledgerId) {
        queryClient.invalidateQueries({ queryKey: queryKeys.ledger(ledgerId) });
      }
    },
  });
}

export function useAcceptInvitation() {
  const setActiveLedger = useAuthStore((state) => state.setActiveLedger);

  return useMutation({
    mutationFn: (token: string) => acceptInvitation(token).then((response) => response.data),
    onSuccess: (data) => {
      setActiveLedger(data.id);
    },
  });
}

export function useDeclineInvitation() {
  return useMutation({
    mutationFn: (token: string) => declineInvitation(token).then((response) => response.data),
  });
}

export function useCancelInvitation() {
  const ledgerId = useAuthStore((state) => state.activeLedgerId);
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (token: string) => cancelInvitation(token).then((response) => response.data),
    onSuccess: () => {
      if (ledgerId) {
        queryClient.invalidateQueries({ queryKey: queryKeys.ledger(ledgerId) });
      }
    },
  });
}
