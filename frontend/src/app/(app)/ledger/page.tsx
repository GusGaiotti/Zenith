"use client";

import { useState } from "react";
import { InviteModal } from "@/components/ledger/InviteModal";
import { MemberCard } from "@/components/ledger/MemberCard";
import { LoadingSkeleton } from "@/components/shared/LoadingSkeleton";
import { PageHeader } from "@/components/shared/PageHeader";
import { useInviteMember, useLedger } from "@/hooks/useLedger";
import { formatDateTime } from "@/lib/utils/date";

export default function LedgerPage() {
  const [name, setName] = useState("Fatura da Casa");
  const [editing, setEditing] = useState(false);
  const [open, setOpen] = useState(false);

  const ledger = useLedger();
  const inviteMember = useInviteMember();
  const members = ledger.data?.members ?? [];
  const canInvite = members.length < 2;

  if (ledger.isLoading) {
    return <LoadingSkeleton variant="chart" />;
  }

  return (
    <div className="space-y-6 pb-20 md:pb-6">
      <PageHeader title="Fatura compartilhada" subtitle="Gerencie sua fatura compartilhada e membros." />

      <section className="surface p-6">
        <h2 className="text-xs uppercase tracking-[0.08em] text-[var(--text-muted)]">Informacoes da fatura</h2>
        {editing ? (
          <input
            value={name}
            onChange={(event) => setName(event.target.value)}
            onBlur={() => setEditing(false)}
            className="focusable mt-3 w-full rounded-md border bg-[var(--bg-elevated)] px-3 py-2 text-lg text-[var(--text-primary)] outline-none"
          />
        ) : (
          <button className="mt-3 text-left text-2xl font-semibold" onClick={() => setEditing(true)}>
            {ledger.data?.name ?? name}
          </button>
        )}
        <p className="mt-2 text-sm text-[var(--text-secondary)]">
          Criado em {ledger.data?.createdAt ? formatDateTime(ledger.data.createdAt) : "-"}
        </p>
      </section>

      <section>
        <div className="mb-3 flex items-center justify-between">
          <h2 className="font-display text-3xl italic">Membros</h2>
          {canInvite ? (
            <button
              className="focusable rounded-md bg-[var(--accent)] px-3 py-2 text-sm font-medium text-black"
              onClick={() => setOpen(true)}
            >
              Convidar parceiro(a)
            </button>
          ) : null}
        </div>
        <div className="grid gap-4 md:grid-cols-2">
          {members.map((member) => (
            <MemberCard
              key={member.userId}
              name={member.displayName}
              email={member.email}
              joinedAt={new Date(member.joinedAt).toLocaleDateString("pt-BR")}
            />
          ))}
          {members.length < 2 ? (
            <article className="surface flex min-h-32 items-center justify-center border-dashed p-4 text-sm text-[var(--text-secondary)]">
              Aguardando aceite do convite.
            </article>
          ) : null}
        </div>
      </section>

      <InviteModal
        open={open && canInvite}
        isSubmitting={inviteMember.isPending}
        onClose={() => setOpen(false)}
        onSubmit={(email) => {
          if (!canInvite) {
            setOpen(false);
            return;
          }

          inviteMember.mutate(email, {
            onSuccess: () => setOpen(false),
          });
        }}
      />
    </div>
  );
}
