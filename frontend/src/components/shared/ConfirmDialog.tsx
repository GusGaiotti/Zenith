"use client";

interface ConfirmDialogProps {
  title: string;
  description: string;
  confirmLabel: string;
  onConfirm: () => void;
  destructive?: boolean;
}

export function ConfirmDialog({
  title,
  description,
  confirmLabel,
  onConfirm,
  destructive = false,
}: ConfirmDialogProps) {
  return (
    <div className="surface p-4">
      <h4 className="text-sm font-semibold">{title}</h4>
      <p className="mt-2 text-sm text-[var(--text-secondary)]">{description}</p>
      <button
        onClick={onConfirm}
        className={`focusable mt-4 rounded-md px-3 py-2 text-sm font-medium transition-all duration-150 ${
          destructive
            ? "bg-red-500/15 text-red-300 hover:bg-red-500/25"
            : "bg-[var(--accent)] text-black hover:bg-[var(--accent-hover)]"
        }`}
      >
        {confirmLabel}
      </button>
    </div>
  );
}

