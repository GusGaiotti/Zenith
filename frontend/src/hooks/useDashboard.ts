"use client";

import { useQuery } from "@tanstack/react-query";
import {
  getDashboardCategoriesBreakdownByMonth,
  getDashboardCoupleSplitByMonth,
  getDashboardOverviewByMonth,
  getDashboardPulseByMonth,
  getDashboardTrendsByMonth,
} from "@/lib/api/dashboard";
import { queryKeys } from "@/lib/api/query-keys";
import { useAuthStore } from "@/lib/store/auth.store";
import { requireLedgerId } from "@/lib/utils/require-ledger-id";

export function useDashboardOverview(yearMonth: string, createdByUserId?: number) {
  const ledgerId = useAuthStore((state) => state.activeLedgerId);
  return useQuery({
    queryKey: ledgerId
      ? queryKeys.dashboard(ledgerId, `overview-${yearMonth}-${createdByUserId ?? "all"}`)
      : ["dashboard", "none", "overview", yearMonth, createdByUserId ?? "all"],
    queryFn: () =>
      getDashboardOverviewByMonth(requireLedgerId(ledgerId), yearMonth, createdByUserId).then(
        (response) => response.data,
      ),
    enabled: Boolean(ledgerId),
    staleTime: 5 * 60 * 1000,
  });
}


export function useDashboardTrends(months = 6, endMonth: string, createdByUserId?: number) {
  const ledgerId = useAuthStore((state) => state.activeLedgerId);
  return useQuery({
    queryKey: ledgerId
      ? queryKeys.dashboard(ledgerId, `trends-${months}-${endMonth}-${createdByUserId ?? "all"}`)
      : ["dashboard", "none", `trends-${months}`, endMonth, createdByUserId ?? "all"],
    queryFn: () =>
      getDashboardTrendsByMonth(requireLedgerId(ledgerId), months, endMonth, createdByUserId).then(
        (response) => response.data,
      ),
    enabled: Boolean(ledgerId),
    staleTime: 5 * 60 * 1000,
  });
}

export function useDashboardCoupleSplit(yearMonth: string, createdByUserId?: number) {
  const ledgerId = useAuthStore((state) => state.activeLedgerId);
  return useQuery({
    queryKey: ledgerId
      ? queryKeys.dashboard(ledgerId, `couple-split-${yearMonth}-${createdByUserId ?? "all"}`)
      : ["dashboard", "none", "couple-split", yearMonth, createdByUserId ?? "all"],
    queryFn: () =>
      getDashboardCoupleSplitByMonth(requireLedgerId(ledgerId), yearMonth, createdByUserId).then(
        (response) => response.data,
      ),
    enabled: Boolean(ledgerId),
    staleTime: 5 * 60 * 1000,
  });
}

export function useDashboardCategoriesBreakdown(yearMonth: string, createdByUserId?: number) {
  const ledgerId = useAuthStore((state) => state.activeLedgerId);
  return useQuery({
    queryKey: ledgerId
      ? queryKeys.dashboard(ledgerId, `categories-breakdown-${yearMonth}-${createdByUserId ?? "all"}`)
      : ["dashboard", "none", "categories-breakdown", yearMonth, createdByUserId ?? "all"],
    queryFn: () =>
      getDashboardCategoriesBreakdownByMonth(requireLedgerId(ledgerId), yearMonth, createdByUserId).then(
        (response) => response.data,
      ),
    enabled: Boolean(ledgerId),
    staleTime: 5 * 60 * 1000,
  });
}

export function useDashboardPulse(yearMonth: string, createdByUserId?: number) {
  const ledgerId = useAuthStore((state) => state.activeLedgerId);
  return useQuery({
    queryKey: ledgerId
      ? queryKeys.dashboard(ledgerId, `pulse-${yearMonth}-${createdByUserId ?? "all"}`)
      : ["dashboard", "none", "pulse", yearMonth, createdByUserId ?? "all"],
    queryFn: () =>
      getDashboardPulseByMonth(requireLedgerId(ledgerId), yearMonth, createdByUserId).then(
        (response) => response.data,
      ),
    enabled: Boolean(ledgerId),
    staleTime: 5 * 60 * 1000,
  });
}
