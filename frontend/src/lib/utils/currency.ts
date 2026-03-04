export const formatCurrency = (value: number, currency = "BRL", locale = "pt-BR") =>
  new Intl.NumberFormat(locale, {
    style: "currency",
    currency,
    maximumFractionDigits: 2,
  }).format(value);

export const normalizeCurrencyInput = (value: string) => value.replace(/\D/g, "");

export const parseCurrencyInputValue = (value: string) => Number(normalizeCurrencyInput(value) || "0") / 100;

export const formatCurrencyShort = (value: number): string => {
  const abs = Math.abs(value);
  if (abs >= 1_000_000) return `${(value / 1_000_000).toFixed(1)}M`;
  if (abs >= 1_000) return `${(value / 1_000).toFixed(1)}k`;
  return value.toFixed(0);
};

