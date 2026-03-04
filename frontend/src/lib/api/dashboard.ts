import api from "@/lib/api/axios";
import type {
  DashboardCategoriesBreakdownResponse,
  DashboardCoupleSplitResponse,
  DashboardOverviewResponse,
  DashboardPulseResponse,
  DashboardTrendsResponse,
} from "@/types/api";

export const getDashboardOverview = (ledgerId: number) =>
  api.get<DashboardOverviewResponse>(`/ledgers/${ledgerId}/dashboard/overview`);

export const getDashboardOverviewByMonth = (
  ledgerId: number,
  yearMonth: string,
  createdByUserId?: number,
) =>
  api.get<DashboardOverviewResponse>(`/ledgers/${ledgerId}/dashboard/overview`, {
    params: { yearMonth, createdByUserId },
  });


export const getDashboardTrends = (ledgerId: number, months = 6) =>
  api.get<DashboardTrendsResponse>(`/ledgers/${ledgerId}/dashboard/trends`, { params: { months } });

export const getDashboardTrendsByMonth = (
  ledgerId: number,
  months: number,
  endMonth: string,
  createdByUserId?: number,
) =>
  api.get<DashboardTrendsResponse>(`/ledgers/${ledgerId}/dashboard/trends`, {
    params: { months, endMonth, createdByUserId },
  });

export const getDashboardCoupleSplit = (ledgerId: number) =>
  api.get<DashboardCoupleSplitResponse>(`/ledgers/${ledgerId}/dashboard/couple-split`);

export const getDashboardCoupleSplitByMonth = (
  ledgerId: number,
  yearMonth: string,
  createdByUserId?: number,
) =>
  api.get<DashboardCoupleSplitResponse>(`/ledgers/${ledgerId}/dashboard/couple-split`, {
    params: { yearMonth, createdByUserId },
  });

export const getDashboardCategoriesBreakdown = (ledgerId: number) =>
  api.get<DashboardCategoriesBreakdownResponse>(`/ledgers/${ledgerId}/dashboard/categories/breakdown`);

export const getDashboardCategoriesBreakdownByMonth = (
  ledgerId: number,
  yearMonth: string,
  createdByUserId?: number,
) =>
  api.get<DashboardCategoriesBreakdownResponse>(`/ledgers/${ledgerId}/dashboard/categories/breakdown`, {
    params: { yearMonth, createdByUserId },
  });

export const getDashboardPulse = (ledgerId: number) =>
  api.get<DashboardPulseResponse>(`/ledgers/${ledgerId}/dashboard/pulse`);

export const getDashboardPulseByMonth = (
  ledgerId: number,
  yearMonth: string,
  createdByUserId?: number,
) =>
  api.get<DashboardPulseResponse>(`/ledgers/${ledgerId}/dashboard/pulse`, {
    params: { yearMonth, createdByUserId },
  });
