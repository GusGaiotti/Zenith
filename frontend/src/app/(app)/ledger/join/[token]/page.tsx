"use client";

import { useState } from "react";
import { useParams, useRouter } from "next/navigation";
import { useAcceptInvitation, useDeclineInvitation } from "@/hooks/useLedger";
import { getApiErrorMessage } from "@/lib/utils/api-error";

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
          <p className="text-xs font-semibold tracking-[0.08em] text-red-200 uppercase">
            Convite indisponível
          </p>
          <h1 className="mt-3 font-display text-4xl text-[var(--text-primary)] italic">
            Não foi possível aceitar
          </h1>
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
            <button
              className="focusable rounded-md border px-4 py-2"
              onClick={() => router.push("/ledger")}
            >
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
          Você foi convidado para entrar em uma fatura. Referência: {maskedToken}
        </p>
        <div className="mt-6 flex gap-2">
          <button
            disabled={acceptMutation.isPending}
            className="focusable rounded-md bg-[var(--accent)] px-4 py-2 text-white disabled:cursor-not-allowed disabled:opacity-40"
            onClick={() =>
              acceptMutation.mutate(token, {
                onSuccess: () => router.push("/dashboard"),
                onError: (error) =>
                  setAcceptErrorMessage(
                    getApiErrorMessage(error, "Esse convite não está mais disponível para aceite."),
                  ),
              })
            }
          >
            {acceptMutation.isPending ? "Accepting..." : "Accept"}
          </button>
          <button
            disabled={declineMutation.isPending}
            className="focusable rounded-md border px-4 py-2 disabled:cursor-not-allowed disabled:opacity-40"
            onClick={() =>
              declineMutation.mutate(token, { onSuccess: () => router.push("/ledger") })
            }
          >
            {declineMutation.isPending ? "Declining..." : "Decline"}
          </button>
        </div>
      </section>
    </main>
  );
}
