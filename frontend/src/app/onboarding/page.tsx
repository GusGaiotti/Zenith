"use client";

import { AxiosError } from "axios";
import { useRouter } from "next/navigation";
import { FormEvent, useEffect, useState } from "react";
import { useCreateLedger, useInviteMember } from "@/hooks/useLedger";
import { getMyLedger } from "@/lib/api/ledger";
import { useAuthStore } from "@/lib/store/auth.store";

export default function OnboardingPage() {
  const router = useRouter();
  const [step, setStep] = useState(1);
  const [email, setEmail] = useState("");
  const hydrated = useAuthStore((state) => state.hydrated);
  const authResolved = useAuthStore((state) => state.authResolved);
  const accessToken = useAuthStore((state) => state.accessToken);
  const setActiveLedger = useAuthStore((state) => state.setActiveLedger);
  const createLedgerMutation = useCreateLedger();
  const inviteMutation = useInviteMember();

  useEffect(() => {
    if (!hydrated || !authResolved) return;
    if (!accessToken) {
      router.replace("/login");
      return;
    }

    getMyLedger()
      .then((response) => {
        setActiveLedger(response.data.id);
        router.replace("/dashboard");
      })
      .catch((error: AxiosError) => {
        if (error.response?.status === 404) {
          setActiveLedger(null);
        }
      });
  }, [accessToken, authResolved, hydrated, router, setActiveLedger]);

  function createLedger(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    const name = String(form.get("name") ?? "");

    createLedgerMutation.mutate(name, {
      onSuccess: () => setStep(2),
    });
  }

  function sendInvite() {
    if (!email.trim()) {
      router.push("/dashboard");
      return;
    }

    inviteMutation.mutate(email, {
      onSettled: () => router.push("/dashboard"),
    });
  }

  return (
    <main className="flex min-h-screen items-center justify-center p-4">
      <section className="surface w-full max-w-xl p-8">
        {step === 1 ? (
          <form onSubmit={createLedger} className="space-y-4">
            <h1 className="font-display text-4xl italic">Nomeie sua fatura</h1>
            <input
              name="name"
              required
              placeholder="Fatura da Casa"
              className="focusable w-full rounded-md border bg-[var(--bg-elevated)] px-3 py-2 outline-none"
            />
            <button
              disabled={createLedgerMutation.isPending}
              className="focusable rounded-md bg-[var(--accent)] px-4 py-2 font-medium text-black transition-all duration-150 hover:bg-[var(--accent-hover)] disabled:cursor-not-allowed disabled:opacity-40"
            >
              {createLedgerMutation.isPending ? "Criando..." : "Criar fatura"}
            </button>
            {createLedgerMutation.isError ? (
              <p className="text-sm text-red-300">Nao foi possivel criar a fatura.</p>
            ) : null}
            <button
              type="button"
              className="focusable rounded-md border px-4 py-2 text-sm"
              onClick={() => router.push("/dashboard")}
            >
              Pular por agora
            </button>
          </form>
        ) : (
          <div className="space-y-4">
            <h1 className="font-display text-4xl italic">Convide seu parceiro(a)</h1>
            <input
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              placeholder="parceiro@email.com"
              className="focusable w-full rounded-md border bg-[var(--bg-elevated)] px-3 py-2 outline-none"
            />
            <div className="flex gap-2">
              <button
                className="focusable rounded-md bg-[var(--accent)] px-4 py-2 font-medium text-black transition-all duration-150 hover:bg-[var(--accent-hover)]"
                onClick={sendInvite}
              >
                Enviar convite
              </button>
              <button className="focusable rounded-md border px-4 py-2 text-sm" onClick={() => router.push("/dashboard")}>
                Fazer depois
              </button>
            </div>
          </div>
        )}
      </section>
    </main>
  );
}
