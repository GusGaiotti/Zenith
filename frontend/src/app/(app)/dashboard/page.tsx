"use client";

import { Suspense, useMemo, useState } from "react";
import { AxiosError } from "axios";
import { useRouter } from "next/navigation";
import { useMutation } from "@tanstack/react-query";
import { CategoryBreakdownChart } from "@/components/dashboard/CategoryBreakdownChart";
import { CoupleSplitPanel } from "@/components/dashboard/CoupleSplitPanel";
import { ExpenseTrendChart } from "@/components/dashboard/ExpenseTrendChart";
import { NotificationBell } from "@/components/dashboard/NotificationBell";
import { OverviewCards } from "@/components/dashboard/OverviewCards";
import { PulseSparkline } from "@/components/dashboard/PulseSparkline";
import { RecentTransactions } from "@/components/dashboard/RecentTransactions";
import { EmptyState } from "@/components/shared/EmptyState";
import { LoadingSkeleton } from "@/components/shared/LoadingSkeleton";
import { MonthPicker } from "@/components/shared/MonthPicker";
import { SelectMenu } from "@/components/shared/SelectMenu";
import { useDashboardCategoriesBreakdown, useDashboardCoupleSplit, useDashboardOverview, useDashboardPulse, useDashboardTrends } from "@/hooks/useDashboard";
import { useLedger } from "@/hooks/useLedger";
import { exportTransactionsExcel } from "@/lib/api/transactions";
import { useTransactions } from "@/hooks/useTransactions";
import { useAuthStore } from "@/lib/store/auth.store";

const filterClassName = "elevated h-12 w-full px-4 text-sm text-[var(--text-primary)] sm:min-w-[176px] sm:w-auto";

function parseFilename(header?: string) {
  if (!header) return null;
  const match = /filename=\"?([^\";]+)\"?/i.exec(header);
  return match?.[1] ?? null;
}

function DownloadIcon({ className }: { className?: string }) {
  return (
    <svg
      aria-hidden
      className={className}
      fill="none"
      stroke="currentColor"
      strokeLinecap="round"
      strokeLinejoin="round"
      strokeWidth="1.8"
      viewBox="0 0 24 24"
    >
      <path d="M12 4v10" />
      <path d="m8 10 4 4 4-4" />
      <path d="M5 18h14" />
    </svg>
  );
}

export default function DashboardPage() {
  const router = useRouter();
  const [yearMonth, setYearMonth] = useState(() => {
    const now = new Date();
    return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, "0")}`;
  });
  const [memberFilter, setMemberFilter] = useState<string>("all");
  const [exportError, setExportError] = useState<string | null>(null);
  const createdByUserId = memberFilter === "all" ? undefined : Number(memberFilter);

  const activeLedgerId = useAuthStore((state) => state.activeLedgerId);
  const ledger = useLedger();
  const overview = useDashboardOverview(yearMonth, createdByUserId);
  const trends = useDashboardTrends(6, yearMonth, createdByUserId);
  const split = useDashboardCoupleSplit(yearMonth, createdByUserId);
  const categories = useDashboardCategoriesBreakdown(yearMonth, createdByUserId);
  const pulse = useDashboardPulse(yearMonth, createdByUserId);

  const { startDate, endDate } = useMemo(() => {
    const [year, month] = yearMonth.split("-").map(Number);
    const start = `${yearMonth}-01`;
    const end = `${yearMonth}-${String(new Date(year, month, 0).getDate()).padStart(2, "0")}`;
    return { startDate: start, endDate: end };
  }, [yearMonth]);

  const transactions = useTransactions({ size: 5, startDate, endDate, createdBy: createdByUserId });
  const exportMutation = useMutation({
    mutationFn: () =>
      exportTransactionsExcel(activeLedgerId as number, {
        startDate,
        endDate,
        createdBy: createdByUserId,
      }),
    onSuccess: (response) => {
      setExportError(null);
      const filename = parseFilename(response.headers["content-disposition"]) ?? `zenith-${yearMonth}.xlsx`;
      const blobUrl = URL.createObjectURL(response.data);
      const link = document.createElement("a");
      link.href = blobUrl;
      link.download = filename;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      URL.revokeObjectURL(blobUrl);
    },
    onError: (error) => {
      if (error instanceof AxiosError) {
        setExportError((error.response?.data as { message?: string } | undefined)?.message ?? "Falha ao exportar arquivo.");
        return;
      }
      setExportError("Falha ao exportar arquivo.");
    },
  });

  const recent = transactions.data?.pages.flatMap((page) => page.content) ?? [];
  const loading = overview.isLoading || trends.isLoading || split.isLoading || categories.isLoading || pulse.isLoading;
  const hasError = overview.isError || trends.isError || split.isError || categories.isError || pulse.isError;
  const memberOptions = [
    { value: "all", label: "Todos" },
    ...(ledger.data?.members ?? []).map((member) => ({
      value: String(member.userId),
      label: member.displayName,
    })),
  ];
  const showSplitPanel = memberFilter === "all" && (ledger.data?.members?.length ?? 0) > 1;

  if (!activeLedgerId) {
    return (
      <div className="space-y-5">
        <div className="flex justify-end">
          <div className="flex flex-col gap-2">
            <span className="block text-xs uppercase tracking-[0.08em] text-[var(--text-muted)]">Alertas</span>
            <NotificationBell />
          </div>
        </div>
        <EmptyState
          title="Fatura nao configurada"
          description="Voce ainda nao criou uma fatura. Continue navegando ou crie uma em /onboarding para liberar o dashboard completo."
          action={{ label: "Criar fatura", onClick: () => router.push("/onboarding") }}
        />
      </div>
    );
  }

  if (loading) {
    return (
      <div className="space-y-5">
        <div className="grid gap-3 md:grid-cols-2 xl:grid-cols-4">
          {Array.from({ length: 4 }).map((_, index) => (
            <LoadingSkeleton key={index} variant="stat" />
          ))}
        </div>
        <div className="grid gap-5 xl:grid-cols-12">
          <div className="xl:col-span-8">
            <LoadingSkeleton variant="chart" />
          </div>
          <div className="xl:col-span-4">
            <LoadingSkeleton variant="chart" />
          </div>
          <div className="xl:col-span-4">
            <LoadingSkeleton variant="chart" />
          </div>
          <div className="xl:col-span-4">
            <LoadingSkeleton variant="chart" />
          </div>
          <div className="xl:col-span-4">
            <LoadingSkeleton variant="chart" />
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-5">
      <div className="flex flex-col gap-3 sm:flex-row sm:flex-wrap sm:items-end sm:justify-between">
        <div>
          <p className="font-display text-3xl italic text-[var(--text-primary)]">Dashboard</p>
          <p className="mt-1 text-sm text-[var(--text-secondary)]">Resumo mensal da fatura compartilhada.</p>
        </div>
        <div className="flex w-full flex-col items-stretch gap-3 sm:w-auto sm:flex-row sm:flex-wrap sm:items-end">
          <div className="flex flex-col gap-2">
            <span className="block text-xs uppercase tracking-[0.08em] text-[var(--text-muted)]">Alertas</span>
            <NotificationBell />
          </div>
          <div className="flex flex-col gap-2">
            <span className="block text-xs uppercase tracking-[0.08em] text-[var(--text-muted)]">Exportar</span>
            <button
              type="button"
              aria-label="Exportar Excel"
              disabled={!activeLedgerId || exportMutation.isPending}
              className="focusable elevated grid h-12 w-full place-items-center rounded-xl px-4 text-sm text-[var(--text-primary)] disabled:cursor-not-allowed disabled:opacity-60 sm:w-auto"
              onClick={() => exportMutation.mutate()}
            >
              <DownloadIcon className={`h-5 w-5 ${exportMutation.isPending ? "animate-pulse" : ""}`} />
            </button>
          </div>
          <MonthPicker
            label="Mes"
            value={yearMonth}
            onChange={setYearMonth}
            align="right"
            buttonClassName={filterClassName}
          />
          <SelectMenu
            label="Pessoa"
            value={memberFilter}
            options={memberOptions}
            onChange={setMemberFilter}
            align="right"
            buttonClassName={filterClassName}
          />
        </div>
      </div>

      {exportError ? (
        <div className="rounded-md border border-red-500/30 bg-red-500/10 px-4 py-3 text-sm text-red-200">{exportError}</div>
      ) : null}

      {hasError ? (
        <div className="rounded-md border border-amber-500/30 bg-amber-500/10 px-4 py-3 text-sm text-amber-200">
          Algumas secoes nao carregaram. Verifique permissao da fatura e disponibilidade da API.
        </div>
      ) : null}

      <OverviewCards overview={overview.data} />
      <div className="grid gap-5 xl:grid-cols-12">
        <div className="space-y-5 xl:col-span-8">
          <Suspense fallback={<LoadingSkeleton variant="chart" />}>
            <ExpenseTrendChart trends={trends.data} />
          </Suspense>
          {showSplitPanel ? (
            <div className="grid gap-5 lg:grid-cols-2">
              <Suspense fallback={<LoadingSkeleton variant="chart" />}>
                <CategoryBreakdownChart data={categories.data} />
              </Suspense>
              <Suspense fallback={<LoadingSkeleton variant="chart" />}>
                <PulseSparkline data={pulse.data} />
              </Suspense>
            </div>
          ) : null}
        </div>
        <div className="space-y-5 xl:col-span-4">
          {showSplitPanel ? (
            <Suspense fallback={<LoadingSkeleton variant="chart" />}>
              <CoupleSplitPanel data={split.data} />
            </Suspense>
          ) : null}
          {!showSplitPanel ? (
            <Suspense fallback={<LoadingSkeleton variant="chart" />}>
              <CategoryBreakdownChart data={categories.data} />
            </Suspense>
          ) : null}
          {!showSplitPanel ? (
            <Suspense fallback={<LoadingSkeleton variant="chart" />}>
              <PulseSparkline data={pulse.data} />
            </Suspense>
          ) : null}
          <RecentTransactions items={recent} />
        </div>
      </div>
    </div>
  );
}
