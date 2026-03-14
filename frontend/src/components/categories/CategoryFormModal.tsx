"use client";

import { FormEvent, useEffect, useId, useRef, useState } from "react";
import type { CreateCategoryRequest } from "@/types/api";

interface CategoryFormModalProps {
  open: boolean;
  onClose: () => void;
  onSubmit: (payload: CreateCategoryRequest) => void;
  isSubmitting?: boolean;
  title?: string;
  submitLabel?: string;
  initialName?: string;
  initialColor?: string;
}

interface CategoryFormContentProps {
  onClose: () => void;
  onSubmit: (payload: CreateCategoryRequest) => void;
  isSubmitting: boolean;
  title: string;
  submitLabel: string;
  initialName: string;
  initialColor: string;
}

const palette = ["#C8873A", "#F87171", "#4ADE80", "#94A3B8", "#60A5FA", "#F59E0B", "#E879F9", "#22D3EE"];

function CategoryFormContent({
  onClose,
  onSubmit,
  isSubmitting,
  title,
  submitLabel,
  initialName,
  initialColor,
}: CategoryFormContentProps) {
  const [name, setName] = useState(initialName);
  const [color, setColor] = useState(initialColor);
  const dialogRef = useRef<HTMLElement>(null);
  const titleId = useId();

  useEffect(() => {
    dialogRef.current?.focus();

    const handleEscape = (event: KeyboardEvent) => {
      if (event.key === "Escape") {
        onClose();
      }
    };

    document.addEventListener("keydown", handleEscape);

    return () => {
      document.removeEventListener("keydown", handleEscape);
    };
  }, [onClose]);

  function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    onSubmit({
      name: name.trim(),
      color,
    });
  }

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/55 p-4"
      onMouseDown={(event) => {
        if (event.target === event.currentTarget) {
          onClose();
        }
      }}
    >
      <section
        ref={dialogRef}
        role="dialog"
        aria-modal="true"
        aria-labelledby={titleId}
        tabIndex={-1}
        className="surface w-full max-w-md p-6"
      >
        <h2 id={titleId} className="font-display text-3xl italic">
          {title}
        </h2>
        <form className="mt-5 space-y-4" onSubmit={submit}>
          <input
            name="name"
            required
            value={name}
            onChange={(event) => setName(event.target.value)}
            placeholder="Nome da categoria"
            className="focusable w-full rounded-md border bg-[var(--bg-elevated)] px-3 py-2 text-[var(--text-primary)] outline-none placeholder:text-[var(--text-secondary)]"
          />
          <div className="space-y-3">
            <div>
              <p className="text-sm font-medium text-[var(--text-secondary)]">Cor da categoria</p>
              <p className="mt-1 text-xs text-[var(--text-muted)]">Escolha uma cor da paleta para manter o contraste e evitar combinações quebradas.</p>
            </div>
            <div className="grid grid-cols-4 gap-2 sm:grid-cols-8">
            {palette.map((item) => (
              <button
                type="button"
                key={item}
                onClick={() => setColor(item)}
                className={`h-10 rounded-2xl border border-[var(--surface-edge)] transition-transform duration-150 hover:scale-[1.04] ${color === item ? "ring-2 ring-[var(--accent)] ring-offset-2 ring-offset-[var(--bg-surface)]" : ""}`}
                style={{ backgroundColor: item }}
                aria-label={item}
              />
            ))}
            </div>
          </div>
          <div className="rounded-2xl border border-[var(--surface-edge)] bg-[var(--card-strong)] px-4 py-3 text-sm text-[var(--text-secondary)]">
            Cor selecionada: <span className="font-mono text-[var(--text-primary)]">{color}</span>
          </div>
          <div className="flex gap-2">
            <button
              disabled={isSubmitting || !name.trim()}
              className="focusable rounded-md bg-[var(--accent)] px-4 py-2 font-medium text-white disabled:cursor-not-allowed disabled:opacity-40"
            >
              {isSubmitting ? "Salvando..." : submitLabel}
            </button>
            <button
              type="button"
              onClick={onClose}
              className="focusable rounded-md border border-red-400/30 bg-red-500/10 px-4 py-2 text-red-200 transition-colors duration-150 hover:border-red-300 hover:bg-red-500/20"
            >
              Cancelar
            </button>
          </div>
        </form>
      </section>
    </div>
  );
}

export function CategoryFormModal({
  open,
  onClose,
  onSubmit,
  isSubmitting = false,
  title = "Nova categoria",
  submitLabel = "Criar",
  initialName = "",
  initialColor = palette[0],
}: CategoryFormModalProps) {
  if (!open) return null;

  return (
    <CategoryFormContent
      key={`${title}-${initialName}-${initialColor}`}
      onClose={onClose}
      onSubmit={onSubmit}
      isSubmitting={isSubmitting}
      title={title}
      submitLabel={submitLabel}
      initialName={initialName}
      initialColor={initialColor}
    />
  );
}

