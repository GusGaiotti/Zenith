"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { InviteModal } from "@/components/ledger/InviteModal";
import { MemberCard } from "@/components/ledger/MemberCard";
import { EmptyState } from "@/components/shared/EmptyState";
import { LoadingSkeleton } from "@/components/shared/LoadingSkeleton";
import { PageHeader } from "@/components/shared/PageHeader";
import { useCancelInvitation, useInviteMember, useLedger, useUpdateLedgerName } from "@/hooks/useLedger";
import { formatDateTime } from "@/lib/utils/date";
import { useAuthStore } from "@/lib/store/auth.store";

export default function LedgerPage() {
  const router = useRouter();
  const [draftName, setDraftName] = useState("");
  const [editing, setEditing] = useState(false);
  const [nameError, setNameError] = useState<string | null>(null);
  const [open, setOpen] = useState(false);

  const activeLedgerId = useAuthStore((state) => state.activeLedgerId);
  const ledger = useLedger();
  const inviteMember = useInviteMember();
  const cancelInvitation = useCancelInvitation();
  const updateLedgerName = useUpdateLedgerName();
  const members = ledger.data?.members ?? [];
  const pendingInvitations = ledger.data?.pendingInvitations ?? [];
  const canInvite = members.length < 2 && pendingInvitations.length === 0;

  const currentName = ledger.data?.name ?? "Fatura da Casa";

  function handleStartEdit() {
    setDraftName(currentName);
    setNameError(null);
    setEditing(true);
  }

  function handleCancelEdit() {
    setDraftName(currentName);
    setNameError(null);
    setEditing(false);
  }

  function handleSaveName() {
    setNameError(null);
    updateLedgerName.mutate(draftName, {
      onSuccess: () => {
        setEditing(false);
      },
      onError: (error) => {
        const response = (error as { response?: { data?: { message?: string } } }).response;
        setNameError(response?.data?.message ?? "Nao foi possivel atualizar o nome da fatura.");
      },
    });
  }

  if (!activeLedgerId) {
    return (
      <div className="space-y-6 pb-20 md:pb-6">
        <PageHeader title="Fatura compartilhada" subtitle="Crie uma fatura quando quiser ou aguarde um convite." />
        <EmptyState
          title="Nenhuma fatura ativa"
          description="Voce ainda nao faz parte de uma fatura. Quando quiser, crie uma nova e convide alguem."
          action={{ label: "Criar fatura", onClick: () => router.push("/onboarding") }}
        />
      </div>
    );
  }

  if (ledger.isLoading) {
    return <LoadingSkeleton variant="chart" />;
  }

  return (
    <div className="space-y-6 pb-20 md:pb-6">
      <PageHeader title="Fatura compartilhada" subtitle="Gerencie sua fatura compartilhada e membros." />

      <section className="surface p-6">
        <h2 className="text-xs uppercase tracking-[0.08em] text-[var(--text-muted)]">Informacoes da fatura</h2>
        {editing ? (
          <div className="mt-3 space-y-3">
            <input
              value={draftName}
              onChange={(event) => setDraftName(event.target.value)}
              maxLength={120}
              className="focusable w-full rounded-md border bg-[var(--bg-elevated)] px-3 py-2 text-lg text-[var(--text-primary)] outline-none"
            />
            <div className="flex flex-wrap gap-2">
              <button
                type="button"
                disabled={updateLedgerName.isPending}
                className="focusable rounded-md bg-[var(--accent)] px-3 py-2 text-sm font-medium text-white disabled:cursor-not-allowed disabled:opacity-60"
                onClick={handleSaveName}
              >
                {updateLedgerName.isPending ? "Salvando..." : "Salvar"}
              </button>
              <button
                type="button"
                disabled={updateLedgerName.isPending}
                className="focusable rounded-md border px-3 py-2 text-sm text-[var(--text-secondary)] disabled:cursor-not-allowed disabled:opacity-60"
                onClick={handleCancelEdit}
              >
                Cancelar
              </button>
            </div>
            {nameError ? <p className="text-sm text-red-300">{nameError}</p> : null}
          </div>
        ) : (
          <div className="mt-3 flex flex-wrap items-center gap-2">
            <p className="text-2xl font-semibold">{currentName}</p>
            <button
              type="button"
              className="focusable rounded-md border px-2.5 py-1 text-xs text-[var(--text-secondary)] hover:text-[var(--text-primary)]"
              onClick={handleStartEdit}
            >
              Editar
            </button>
          </div>
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
              className="focusable rounded-md bg-[var(--accent)] px-3 py-2 text-sm font-medium text-white"
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
          {pendingInvitations.map((invitation) => (
            <MemberCard
              key={invitation.token}
              name={invitation.invitedUserDisplayName ?? "Convite pendente"}
              email={invitation.invitedEmail}
              joinedAt="-"
              statusLabel="Convite pendente"
              actionLabel={cancelInvitation.isPending ? "Cancelando..." : "Cancelar convite"}
              actionDisabled={cancelInvitation.isPending}
              onAction={() => cancelInvitation.mutate(invitation.token)}
            />
          ))}
          {members.length < 2 && pendingInvitations.length === 0 ? (
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

