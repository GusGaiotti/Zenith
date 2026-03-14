"use client";

import { useEffect, useId, useRef, useState } from "react";
import { cn } from "@/lib/utils/cn";

export interface SelectMenuOption {
  value: string;
  label: string;
}

interface SelectMenuProps {
  value: string;
  options: SelectMenuOption[];
  onChange: (value: string) => void;
  label?: string;
  placeholder?: string;
  align?: "left" | "right";
  buttonClassName?: string;
}

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

export function SelectMenu({
  value,
  options,
  onChange,
  label,
  placeholder = "Selecionar",
  align = "left",
  buttonClassName,
}: SelectMenuProps) {
  const [open, setOpen] = useState(false);
  const [focusedIndex, setFocusedIndex] = useState(-1);
  const wrapperRef = useRef<HTMLDivElement>(null);
  const optionRefs = useRef<Array<HTMLButtonElement | null>>([]);
  const listboxId = useId();
  const selectedOption = options.find((option) => option.value === value);
  const selectedIndex = options.findIndex((option) => option.value === value);

  function focusOption(index: number) {
    setFocusedIndex(index);

    requestAnimationFrame(() => {
      optionRefs.current[index]?.focus();
    });
  }

  function openMenu(targetIndex = selectedIndex >= 0 ? selectedIndex : 0) {
    setOpen(true);
    focusOption(targetIndex);
  }

  function closeMenu() {
    setOpen(false);
    setFocusedIndex(-1);
  }

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

  return (
    <div ref={wrapperRef} className="relative space-y-2">
      {label ? <span className="block text-xs uppercase tracking-[0.08em] text-[var(--text-muted)]">{label}</span> : null}
      <button
        type="button"
        aria-expanded={open}
        aria-haspopup="listbox"
        aria-controls={listboxId}
        className={cn(
          "focusable flex w-full items-center justify-between gap-3 rounded-xl border border-[var(--surface-edge)] bg-[var(--bg-elevated)] px-4 py-3 text-left text-sm text-[var(--text-primary)] transition-colors duration-150 hover:border-[var(--accent)]",
          buttonClassName,
        )}
        onClick={() => {
          if (open) {
            closeMenu();
            return;
          }

          openMenu();
        }}
        onKeyDown={(event) => {
          if (event.key !== "ArrowDown" && event.key !== "ArrowUp") {
            return;
          }

          event.preventDefault();
          if (event.key === "ArrowDown") {
            openMenu(selectedIndex >= 0 ? selectedIndex : 0);
            return;
          }

          openMenu(selectedIndex >= 0 ? selectedIndex : options.length - 1);
        }}
      >
        <span className={cn("truncate", !selectedOption && "text-[var(--text-secondary)]")}>
          {selectedOption?.label ?? placeholder}
        </span>
        <span className="grid h-7 w-7 place-items-center rounded-md border border-[var(--surface-edge)] bg-[var(--bg-muted)] text-[var(--text-secondary)]">
          <Chevron open={open} />
        </span>
      </button>
      {open ? (
        <div
          id={listboxId}
          className={cn(
            "absolute top-[calc(100%+0.5rem)] z-40 min-w-full overflow-hidden rounded-2xl border border-[var(--surface-edge)] bg-[var(--menu-bg)] p-2 shadow-[0_20px_60px_rgba(0,0,0,0.18)] backdrop-blur",
            align === "right" ? "right-0" : "left-0",
          )}
          role="listbox"
          tabIndex={-1}
          aria-activedescendant={focusedIndex >= 0 ? `${listboxId}-${focusedIndex}` : undefined}
          onKeyDown={(event) => {
            if (event.key === "Escape") {
              event.preventDefault();
              closeMenu();
              return;
            }

            if (event.key === "ArrowDown") {
              event.preventDefault();
              const nextIndex = Math.min(options.length - 1, focusedIndex + 1);
              focusOption(nextIndex);
              return;
            }

            if (event.key === "ArrowUp") {
              event.preventDefault();
              const nextIndex = Math.max(0, focusedIndex - 1);
              focusOption(nextIndex);
              return;
            }

            if (event.key === "Home") {
              event.preventDefault();
              focusOption(0);
              return;
            }

            if (event.key === "End") {
              event.preventDefault();
              const nextIndex = options.length - 1;
              focusOption(nextIndex);
            }
          }}
        >
          {options.map((option, index) => {
            const active = option.value === value;

            return (
              <button
                id={`${listboxId}-${index}`}
                key={option.value}
                type="button"
                role="option"
                aria-selected={active}
                tabIndex={focusedIndex === index ? 0 : -1}
                ref={(element) => {
                  optionRefs.current[index] = element;
                }}
                className={cn(
                  "flex w-full items-center justify-between rounded-xl px-3 py-2 text-sm transition-colors duration-150",
                  active
                    ? "bg-[var(--accent-muted)] text-[var(--accent-hover)]"
                    : "text-[var(--text-secondary)] hover:bg-[var(--bg-elevated)] hover:text-[var(--text-primary)]",
                )}
                onClick={() => {
                  onChange(option.value);
                  closeMenu();
                }}
                onFocus={() => setFocusedIndex(index)}
              >
                <span>{option.label}</span>
                {active ? <span className="font-mono text-[11px]">OK</span> : null}
              </button>
            );
          })}
        </div>
      ) : null}
    </div>
  );
}
