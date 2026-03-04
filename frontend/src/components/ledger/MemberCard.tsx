interface MemberCardProps {
  name: string;
  email: string;
  joinedAt: string;
  statusLabel?: string;
  actionLabel?: string;
  onAction?: () => void;
  actionDisabled?: boolean;
}

export function MemberCard({
  name,
  email,
  joinedAt,
  statusLabel,
  actionLabel,
  onAction,
  actionDisabled = false,
}: MemberCardProps) {
  const initials = name
    .split(" ")
    .map((word) => word[0])
    .join("")
    .slice(0, 2)
    .toUpperCase();

  return (
    <article className="surface p-4">
      <div className="flex items-center gap-3">
        <div className="flex h-10 w-10 items-center justify-center rounded-full border border-[var(--accent)] text-sm font-semibold text-[var(--accent)]">{initials}</div>
        <div>
          <h3 className="font-semibold">{name}</h3>
          <p className="text-xs text-[var(--text-secondary)]">{email}</p>
          <p className="text-xs text-[var(--text-muted)]">{statusLabel ?? `Entrou ${joinedAt}`}</p>
        </div>
      </div>
      {actionLabel && onAction ? (
        <button
          type="button"
          disabled={actionDisabled}
          className="focusable mt-3 rounded-md border px-3 py-2 text-xs text-[var(--text-secondary)] transition-all duration-150 hover:bg-white/5 disabled:cursor-not-allowed disabled:opacity-40"
          onClick={onAction}
        >
          {actionLabel}
        </button>
      ) : null}
    </article>
  );
}
