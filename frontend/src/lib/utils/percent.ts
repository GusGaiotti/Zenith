export function normalizePercentage(value: number | string | null | undefined): number {
  const numeric = Number(value ?? 0);

  if (!Number.isFinite(numeric)) {
    return 0;
  }

  if (numeric !== 0 && Math.abs(numeric) <= 1) {
    return numeric * 100;
  }

  return numeric;
}

export function clampPercentage(value: number | string | null | undefined): number {
  return Math.min(100, Math.max(0, normalizePercentage(value)));
}

export function formatPercentage(
  value: number | string | null | undefined,
  decimals = 1,
): string {
  return `${normalizePercentage(value).toFixed(decimals)}%`;
}
