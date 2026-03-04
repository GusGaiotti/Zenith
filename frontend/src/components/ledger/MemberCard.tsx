interface MemberCardProps {
  name: string;
  email: string;
  joinedAt: string;
}

export function MemberCard({ name, email, joinedAt }: MemberCardProps) {
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
          <p className="text-xs text-[var(--text-muted)]">Entrou {joinedAt}</p>
        </div>
      </div>
    </article>
  );
}
