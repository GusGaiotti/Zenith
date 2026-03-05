"use client";

import { useEffect, useMemo, useRef, useState } from "react";
import { cn } from "@/lib/utils/cn";

interface MonthPickerProps {
  value: string;
  onChange: (value: string) => void;
  label?: string;
  align?: "left" | "right";
  buttonClassName?: string;
}

const monthOptions = [
  { value: "01", label: "Jan" },
  { value: "02", label: "Fev" },
  { value: "03", label: "Mar" },
  { value: "04", label: "Abr" },
  { value: "05", label: "Mai" },
  { value: "06", label: "Jun" },
  { value: "07", label: "Jul" },
  { value: "08", label: "Ago" },
  { value: "09", label: "Set" },
  { value: "10", label: "Out" },
  { value: "11", label: "Nov" },
  { value: "12", label: "Dez" },
] as const;

function Chevron({ open }: { open: boolean }) {
  return (
    <svg
      aria-hidden
      className={cn("h-4 w-4 transition-transform duration-150", open && "rotate-180")}
      fill="none"
      stroke="currentColor"
      strokeLinecap="round"
      strokeLinejoin="round"
      strokeWidth="1.8"
      viewBox="0 0 24 24"
    >
      <path d="m7 10 5 5 5-5" />
    </svg>
  );
}

export function MonthPicker({
  value,
  onChange,
  label,
  align = "left",
  buttonClassName,
}: MonthPickerProps) {
  const [open, setOpen] = useState(false);
  const wrapperRef = useRef<HTMLDivElement>(null);
  const [selectedYear, selectedMonth] = value.split("-");
  const [visibleYear, setVisibleYear] = useState(Number(selectedYear));

  useEffect(() => {
    setVisibleYear(Number(selectedYear));
  }, [selectedYear]);

  useEffect(() => {
    if (!open) {
      return;
    }

    const handlePointerDown = (event: MouseEvent) => {
      if (!wrapperRef.current?.contains(event.target as Node)) {
        setOpen(false);
      }
    };

    const handleEscape = (event: KeyboardEvent) => {
      if (event.key === "Escape") {
        setOpen(false);
      }
    };

    document.addEventListener("mousedown", handlePointerDown);
    document.addEventListener("keydown", handleEscape);

    return () => {
      document.removeEventListener("mousedown", handlePointerDown);
      document.removeEventListener("keydown", handleEscape);
    };
  }, [open]);

  const selectedLabel = useMemo(() => {
    const month = monthOptions.find((option) => option.value === selectedMonth);
    return month ? `${month.label} ${selectedYear}` : value;
  }, [selectedMonth, selectedYear, value]);

  return (
    <div ref={wrapperRef} className="relative space-y-2">
      {label ? <span className="block text-xs uppercase tracking-[0.08em] text-[var(--text-muted)]">{label}</span> : null}
      <button
        type="button"
        aria-expanded={open}
        aria-haspopup="dialog"
        className={cn(
          "focusable flex w-full items-center justify-between gap-3 rounded-xl border border-[var(--surface-edge)] bg-[rgba(35,33,25,0.94)] px-4 py-3 text-left text-sm text-[var(--text-primary)] transition-colors duration-150 hover:border-[var(--accent)]",
          buttonClassName,
        )}
        onClick={() => setOpen((currentValue) => !currentValue)}
      >
        <span className="font-mono">{selectedLabel}</span>
        <span className="grid h-7 w-7 place-items-center rounded-md border border-[var(--surface-edge)] bg-white/5 text-[var(--text-secondary)]">
          <Chevron open={open} />
        </span>
      </button>
      {open ? (
        <div
          className={cn(
            "absolute top-[calc(100%+0.5rem)] z-40 w-[min(280px,calc(100vw-1.5rem))] rounded-2xl border border-[var(--surface-edge)] bg-[rgba(24,22,19,0.98)] p-3 shadow-[0_20px_60px_rgba(0,0,0,0.42)] backdrop-blur",
            align === "right" ? "right-0" : "left-0",
          )}
          role="dialog"
          aria-label="Selecionar mes"
        >
          <div className="mb-3 flex items-center justify-between">
            <button
              type="button"
              className="focusable rounded-lg border border-[var(--surface-edge)] px-2 py-1 text-xs text-[var(--text-secondary)] transition-colors duration-150 hover:border-[var(--accent)] hover:text-[var(--accent)]"
              onClick={() => setVisibleYear((currentValue) => currentValue - 1)}
            >
              Ano -
            </button>
            <span className="font-mono text-sm text-[var(--text-primary)]">{visibleYear}</span>
            <button
              type="button"
              className="focusable rounded-lg border border-[var(--surface-edge)] px-2 py-1 text-xs text-[var(--text-secondary)] transition-colors duration-150 hover:border-[var(--accent)] hover:text-[var(--accent)]"
              onClick={() => setVisibleYear((currentValue) => currentValue + 1)}
            >
              Ano +
            </button>
          </div>
          <div className="grid grid-cols-3 gap-2">
            {monthOptions.map((month) => {
              const optionValue = `${visibleYear}-${month.value}`;
              const active = optionValue === value;

              return (
                <button
                  key={optionValue}
                  type="button"
                  className={cn(
                    "focusable rounded-xl border px-3 py-2 text-sm transition-colors duration-150",
                    active
                      ? "border-[var(--accent)] bg-[var(--accent-muted)] text-[var(--accent)]"
                      : "border-[var(--surface-edge)] bg-white/5 text-[var(--text-secondary)] hover:border-[var(--accent)] hover:text-[var(--text-primary)]",
                  )}
                  onClick={() => {
                    onChange(optionValue);
                    setOpen(false);
                  }}
                >
                  {month.label}
                </button>
              );
            })}
          </div>
        </div>
      ) : null}
    </div>
  );
}
