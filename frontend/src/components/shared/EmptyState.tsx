interface EmptyStateProps {
  title: string;
  description: string;
  action?: { label: string; onClick?: () => void };
}

export function EmptyState({ title, description, action }: EmptyStateProps) {
  return (
    <div className="surface flex min-h-56 flex-col items-center justify-center p-6 text-center">
      <h3 className="font-display text-2xl">{title}</h3>
      <p className="mt-2 max-w-md text-sm text-[var(--text-secondary)]">{description}</p>
      {action ? (
        <button className="focusable mt-4 rounded-md bg-[var(--accent)] px-4 py-2 text-sm font-medium text-white transition-all duration-150 hover:bg-[var(--accent-hover)]" onClick={action.onClick}>
          {action.label}
        </button>
      ) : null}
    </div>
  );
}


