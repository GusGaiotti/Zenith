"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import { zodResolver } from "@hookform/resolvers/zod";
import { useForm, useWatch } from "react-hook-form";
import { BrandWordmark } from "@/components/brand/BrandWordmark";
import { ThemeToggle } from "@/components/theme/ThemeToggle";
import { useRegister } from "@/hooks/useAuth";
import { registerSchema, type RegisterSchema } from "@/lib/validators/auth.schemas";

export default function RegisterPage() {
  const router = useRouter();
  const registerMutation = useRegister();
  const [cooldownSeconds, setCooldownSeconds] = useState(0);

  useEffect(() => {
    if (cooldownSeconds <= 0) return;
    const timer = setInterval(() => {
      setCooldownSeconds((value) => Math.max(0, value - 1));
    }, 1000);
    return () => clearInterval(timer);
  }, [cooldownSeconds]);

  const form = useForm<RegisterSchema>({
    resolver: zodResolver(registerSchema),
    defaultValues: { displayName: "", email: "", password: "" },
  });

  const onSubmit = form.handleSubmit((values) => {
    registerMutation.mutate(values, {
      onSuccess: () => {
        setCooldownSeconds(0);
        router.push("/onboarding");
      },
      onError: () => {
        setCooldownSeconds(5);
      },
    });
  });

  const password = useWatch({ control: form.control, name: "password", defaultValue: "" });
  const strength = password.length > 10 ? "Forte" : password.length > 6 ? "Media" : "Fraca";

  return (
    <main className="relative flex min-h-screen items-center justify-center p-4">
      <div className="absolute right-4 top-4 z-10">
        <ThemeToggle compact />
      </div>
      <section className="surface w-full max-w-md p-8">
        <BrandWordmark animate className="mb-6" labelClassName="text-4xl text-[var(--text-primary)]" />
        <h1 className="font-display text-4xl">Criar conta</h1>
        <p className="mt-2 text-sm text-[var(--text-secondary)]">Configure o Zenith em menos de um minuto.</p>
        <form className="mt-6 space-y-4" onSubmit={onSubmit}>
          <label className="block text-sm">
            <span className="mb-1 block text-[var(--text-secondary)]">Nome</span>
            <input
              className="focusable w-full rounded-md border bg-[var(--bg-elevated)] px-3 py-2 outline-none"
              {...form.register("displayName")}
            />
            {form.formState.errors.displayName ? (
              <span className="mt-1 block text-xs text-red-300">{form.formState.errors.displayName.message}</span>
            ) : null}
          </label>
          <label className="block text-sm">
            <span className="mb-1 block text-[var(--text-secondary)]">Email</span>
            <input
              type="email"
              className="focusable w-full rounded-md border bg-[var(--bg-elevated)] px-3 py-2 outline-none"
              {...form.register("email")}
            />
            {form.formState.errors.email ? (
              <span className="mt-1 block text-xs text-red-300">{form.formState.errors.email.message}</span>
            ) : null}
          </label>
          <label className="block text-sm">
            <span className="mb-1 block text-[var(--text-secondary)]">Senha</span>
            <input
              type="password"
              className="focusable w-full rounded-md border bg-[var(--bg-elevated)] px-3 py-2 outline-none"
              {...form.register("password")}
            />
            <span className="mt-1 block text-xs text-[var(--text-secondary)]">Forca: {strength}</span>
            {form.formState.errors.password ? (
              <span className="mt-1 block text-xs text-red-300">{form.formState.errors.password.message}</span>
            ) : null}
          </label>
          {registerMutation.isError ? (
            <p className="rounded-md bg-red-500/10 px-3 py-2 text-sm text-red-300">
              Nao foi possivel cadastrar. Tente novamente.
            </p>
          ) : null}
          <button
            disabled={registerMutation.isPending || cooldownSeconds > 0}
            className="focusable w-full rounded-md bg-[var(--accent)] px-4 py-2 font-medium text-white transition-all duration-150 hover:bg-[var(--accent-hover)] disabled:cursor-not-allowed disabled:opacity-40"
          >
            {registerMutation.isPending
              ? "Cadastrando..."
              : cooldownSeconds > 0
                ? `Tente novamente em ${cooldownSeconds}s`
                : "Cadastrar"}
          </button>
        </form>
        <p className="mt-4 text-sm text-[var(--text-secondary)]">
          Ja tem conta? <Link className="text-[var(--accent)]" href="/login">Entrar</Link>
        </p>
      </section>
    </main>
  );
}

