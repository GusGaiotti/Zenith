"use client";

import { AxiosError } from "axios";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";
import { useLogin } from "@/hooks/useAuth";
import { getMyLedger } from "@/lib/api/ledger";
import { useAuthStore } from "@/lib/store/auth.store";
import { loginSchema, type LoginSchema } from "@/lib/validators/auth.schemas";

export default function LoginPage() {
  const router = useRouter();
  const loginMutation = useLogin();
  const setActiveLedger = useAuthStore((state) => state.setActiveLedger);
  const [cooldownSeconds, setCooldownSeconds] = useState(0);

  useEffect(() => {
    if (cooldownSeconds <= 0) return;
    const timer = setInterval(() => {
      setCooldownSeconds((value) => Math.max(0, value - 1));
    }, 1000);
    return () => clearInterval(timer);
  }, [cooldownSeconds]);

  const form = useForm<LoginSchema>({
    resolver: zodResolver(loginSchema),
    defaultValues: { email: "", password: "" },
  });

  const onSubmit = form.handleSubmit((values) => {
    loginMutation.mutate(values, {
      onSuccess: async () => {
        setCooldownSeconds(0);
        try {
          const response = await getMyLedger();
          setActiveLedger(response.data.id);
          router.push("/dashboard");
        } catch (error) {
          const axiosError = error as AxiosError;
          if (axiosError.response?.status === 404) {
            setActiveLedger(null);
            router.push("/onboarding");
            return;
          }
          router.push("/dashboard");
        }
      },
      onError: () => {
        setCooldownSeconds(5);
      },
    });
  });

  return (
    <main className="flex min-h-screen items-center justify-center p-4">
      <section className="surface w-full max-w-md p-8">
        <h1 className="font-display text-4xl italic">Bem-vindo de volta</h1>
        <p className="mt-2 text-sm text-[var(--text-secondary)]">Entre para continuar na sua fatura compartilhada.</p>
        <form className="mt-6 space-y-4" onSubmit={onSubmit}>
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
            {form.formState.errors.password ? (
              <span className="mt-1 block text-xs text-red-300">{form.formState.errors.password.message}</span>
            ) : null}
          </label>
          {loginMutation.isError ? (
            <p className="rounded-md bg-red-500/10 px-3 py-2 text-sm text-red-300">
              Credenciais invalidas ou servidor indisponivel.
            </p>
          ) : null}
          <button
            disabled={loginMutation.isPending || cooldownSeconds > 0}
            className="focusable w-full rounded-md bg-[var(--accent)] px-4 py-2 font-medium text-black transition-all duration-150 hover:bg-[var(--accent-hover)] disabled:cursor-not-allowed disabled:opacity-40"
          >
            {loginMutation.isPending
              ? "Entrando..."
              : cooldownSeconds > 0
                ? `Tente novamente em ${cooldownSeconds}s`
                : "Entrar"}
          </button>
        </form>
        <p className="mt-4 text-sm text-[var(--text-secondary)]">
          Nao tem conta? <Link className="text-[var(--accent)]" href="/register">Cadastre-se</Link>
        </p>
      </section>
    </main>
  );
}
