"use client";

import { AxiosError } from "axios";
import { useState } from "react";
import { useParams, useRouter } from "next/navigation";
import { useAcceptInvitation, useDeclineInvitation } from "@/hooks/useLedger";

function extractInvitationErrorMessage(error: unknown) {
  if (error instanceof AxiosError) {
    const message = (error.response?.data as { message?: string } | undefined)?.message;

    if (message) {
      return message;
    }
  }

  return "Esse convite nao esta mais disponivel para aceite.";
}

export default function JoinLedgerPage() {
  const params = useParams<{ token: string }>();
  const router = useRouter();
  const [acceptErrorMessage, setAcceptErrorMessage] = useState<string | null>(null);
  const acceptMutation = useAcceptInvitation();
  const declineMutation = useDeclineInvitation();

  const token = params.token;
  const maskedToken = `${token.slice(0, 6)}...${token.slice(-4)}`;

  if (acceptErrorMessage) {
    return (
      <main className="mx-auto max-w-xl p-6">
        <section className="surface border border-red-500/30 bg-red-500/10 p-6">
          <p className="text-xs font-semibold uppercase tracking-[0.08em] text-red-200">Convite indisponivel</p>
          <h1 className="mt-3 font-display text-4xl italic text-[var(--text-primary)]">Nao foi possivel aceitar</h1>
          <p className="mt-3 text-sm text-red-100">{acceptErrorMessage}</p>
          <p className="mt-2 text-sm text-[var(--text-secondary)]">
            O convite pode ter sido cancelado, expirado ou ja utilizado.
          </p>
          <div className="mt-6 flex gap-2">
            <button
              className="focusable rounded-md bg-[var(--accent)] px-4 py-2 text-white"
              onClick={() => router.push("/dashboard")}
            >
              Ir para dashboard
            </button>
            <button className="focusable rounded-md border px-4 py-2" onClick={() => router.push("/ledger")}>
              Voltar para fatura
            </button>
          </div>
        </section>
      </main>
    );
  }

  return (
    <main className="mx-auto max-w-xl p-6">
      <section className="surface p-6">
        <h1 className="font-display text-4xl italic">Join invitation</h1>
        <p className="mt-3 text-sm text-[var(--text-secondary)]">
          Voce foi convidado para entrar em uma fatura. Referencia: {maskedToken}
        </p>
        <div className="mt-6 flex gap-2">
          <button
            disabled={acceptMutation.isPending}
            className="focusable rounded-md bg-[var(--accent)] px-4 py-2 text-white disabled:cursor-not-allowed disabled:opacity-40"
            onClick={() =>
              acceptMutation.mutate(token, {
                onSuccess: () => router.push("/dashboard"),
                onError: (error) => setAcceptErrorMessage(extractInvitationErrorMessage(error)),
              })
            }
          >
            {acceptMutation.isPending ? "Accepting..." : "Accept"}
          </button>
          <button
            disabled={declineMutation.isPending}
            className="focusable rounded-md border px-4 py-2 disabled:cursor-not-allowed disabled:opacity-40"
            onClick={() => declineMutation.mutate(token, { onSuccess: () => router.push("/ledger") })}
          >
            {declineMutation.isPending ? "Declining..." : "Decline"}
          </button>
        </div>
      </section>
    </main>
  );
}
