"use client";

import { FormEvent, useState } from "react";

interface InviteModalProps {
  open: boolean;
  onClose: () => void;
  onSubmit: (email: string) => void;
  isSubmitting?: boolean;
}

export function InviteModal({ open, onClose, onSubmit, isSubmitting = false }: InviteModalProps) {
  const [email, setEmail] = useState("");

  if (!open) return null;

  function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    onSubmit(email);
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/55 p-4">
      <section className="surface w-full max-w-md p-6">
        <h3 className="font-display text-3xl italic">Convidar parceiro(a)</h3>
        <form className="mt-5 space-y-4" onSubmit={submit}>
          <input
            type="email"
            required
            value={email}
            onChange={(event) => setEmail(event.target.value)}
            placeholder="parceiro@email.com"
            className="focusable w-full rounded-md border bg-[var(--bg-elevated)] px-3 py-2 outline-none"
          />
          <div className="flex gap-2">
            <button
              disabled={isSubmitting}
              className="focusable rounded-md bg-[var(--accent)] px-4 py-2 font-medium text-black disabled:cursor-not-allowed disabled:opacity-40"
            >
              {isSubmitting ? "Enviando..." : "Enviar convite"}
            </button>
            <button type="button" onClick={onClose} className="focusable rounded-md border px-4 py-2">
              Cancelar
            </button>
          </div>
        </form>
      </section>
    </div>
  );
}
