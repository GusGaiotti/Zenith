"use client";

interface ConfirmDialogProps {
  open: boolean;
  title: string;
  description: string;
  confirmLabel: string;
  onConfirm: () => void;
  onCancel: () => void;
  variant?: "danger" | "default";
  isPending?: boolean;
}

export function ConfirmDialog({
  open,
  title,
  description,
  confirmLabel,
  onConfirm,
  onCancel,
  variant = "default",
  isPending = false,
}: ConfirmDialogProps) {
  if (!open) return null;

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center p-4"
      role="dialog"
      aria-modal="true"
      aria-labelledby="confirm-dialog-title"
    >
      <div className="absolute inset-0 bg-black/50 backdrop-blur-sm" onClick={onCancel} />
      <div className="surface relative w-full max-w-md p-6 shadow-2xl">
        <h4 id="confirm-dialog-title" className="text-base font-semibold text-[var(--text-primary)]">
          {title}
        </h4>
        <p className="mt-2 text-sm text-[var(--text-secondary)]">{description}</p>
        <div className="mt-6 flex justify-end gap-3">
          <button
            onClick={onCancel}
            disabled={isPending}
            className="focusable h-10 rounded-xl border border-[var(--border)] px-4 text-sm text-[var(--text-secondary)] transition-colors duration-150 hover:border-[var(--accent)] hover:text-[var(--text-primary)] disabled:opacity-45"
          >
            Cancelar
          </button>
          <button
            onClick={onConfirm}
            disabled={isPending}
            className={`focusable h-10 rounded-xl px-4 text-sm font-semibold transition-all duration-150 disabled:cursor-not-allowed disabled:opacity-45 ${
              variant === "danger"
                ? "danger-chip hover:border-[var(--expense)] hover:bg-[color-mix(in_srgb,var(--expense)_20%,transparent)]"
                : "bg-[var(--accent)] text-white hover:bg-[var(--accent-hover)]"
            }`}
          >
            {isPending ? "Aguarde..." : confirmLabel}
          </button>
        </div>
      </div>
    </div>
  );
}
