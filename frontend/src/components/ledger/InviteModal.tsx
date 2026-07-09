"use client";

import { FormEvent, useState } from "react";
import { Button, Input } from "@/components/ui";

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
          <Input
            type="email"
            required
            value={email}
            onChange={(event) => setEmail(event.target.value)}
            placeholder="parceiro@email.com"
          />
          <div className="flex gap-2">
            <Button type="submit" disabled={isSubmitting}>
              {isSubmitting ? "Enviando..." : "Enviar convite"}
            </Button>
            <button
              type="button"
              onClick={onClose}
              className="focusable rounded-md border border-[var(--danger-border)] bg-[var(--danger-bg)] px-4 py-2 text-[var(--danger-text)] transition-colors duration-150 hover:border-[var(--danger-border)] hover:bg-[var(--danger-bg)]"
            >
              Cancelar
            </button>
          </div>
        </form>
      </section>
    </div>
  );
}
