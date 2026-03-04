"use client";

import { useParams, useRouter } from "next/navigation";
import { useAcceptInvitation, useDeclineInvitation } from "@/hooks/useLedger";

export default function JoinLedgerPage() {
  const params = useParams<{ token: string }>();
  const router = useRouter();
  const acceptMutation = useAcceptInvitation();
  const declineMutation = useDeclineInvitation();

  const token = params.token;
  const maskedToken = `${token.slice(0, 6)}...${token.slice(-4)}`;

  return (
    <main className="mx-auto max-w-xl p-6">
      <section className="surface p-6">
        <h1 className="font-display text-4xl italic">Join invitation</h1>
        <p className="mt-3 text-sm text-[var(--text-secondary)]">
          Voce foi convidado para entrar em uma fatura. Referencia: {maskedToken}
        </p>
        <div className="mt-6 flex gap-2">
          <button
            className="focusable rounded-md bg-[var(--accent)] px-4 py-2 text-black"
            onClick={() => acceptMutation.mutate(token, { onSuccess: () => router.push("/dashboard") })}
          >
            Accept
          </button>
          <button
            className="focusable rounded-md border px-4 py-2"
            onClick={() => declineMutation.mutate(token, { onSuccess: () => router.push("/ledger") })}
          >
            Decline
          </button>
        </div>
      </section>
    </main>
  );
}
