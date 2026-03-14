"use client";

import { AxiosError } from "axios";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";
import { BrandWordmark } from "@/components/brand/BrandWordmark";
import { ThemeToggle } from "@/components/theme/ThemeToggle";
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
    <main className="relative flex min-h-screen items-center justify-center overflow-hidden p-4 sm:p-6">
      <div className="absolute right-4 top-4 z-10">
        <ThemeToggle compact />
      </div>
      <section className="surface relative w-full max-w-5xl overflow-hidden p-0">
        <div className="grid min-h-[620px] lg:grid-cols-[1.1fr_0.9fr]">
          <div className="relative hidden flex-col justify-between border-r border-[var(--border)] bg-[linear-gradient(145deg,color-mix(in_srgb,var(--accent)_18%,transparent),transparent_52%),linear-gradient(180deg,var(--surface-gradient-start),var(--elevated-gradient-end))] p-10 lg:flex">
            <div>
              <BrandWordmark animate labelClassName="text-5xl text-[var(--text-primary)]" />
              <h1 className="mt-6 max-w-sm text-4xl font-semibold leading-tight text-[var(--text-primary)]">
                Controle financeiro compartilhado com clareza, ritmo e tranquilidade.
              </h1>
            </div>
            <ul className="space-y-3 text-sm text-[var(--text-secondary)]">
              <li className="rounded-xl border border-[var(--border)] bg-[var(--panel-bg)] px-4 py-3">Saldo consolidado do casal em tempo real.</li>
              <li className="rounded-xl border border-[var(--border)] bg-[var(--panel-bg)] px-4 py-3">Transacoes organizadas por pessoa e categoria.</li>
              <li className="rounded-xl border border-[var(--border)] bg-[var(--panel-bg)] px-4 py-3">Visoes rapidas para o fechamento do mes.</li>
            </ul>
          </div>

          <div className="p-6 sm:p-10">
            <div className="mx-auto w-full max-w-md">
              <p className="text-xs font-semibold uppercase tracking-[0.15em] text-[var(--text-muted)]">Acesso seguro</p>
              <h2 className="mt-3 font-display text-3xl leading-tight text-[var(--text-primary)]">Entrar na sua conta</h2>
              <p className="mt-2 text-sm text-[var(--text-secondary)]">Use seu email e senha para continuar.</p>

              <form className="mt-8 space-y-5" onSubmit={onSubmit}>
                <label className="block text-sm">
                  <span className="mb-1.5 block font-medium text-[var(--text-secondary)]">Email</span>
                  <input
                    type="email"
                    className="focusable h-11 w-full rounded-xl border border-[var(--border)] bg-[var(--bg-elevated)] px-3.5 outline-none transition-colors duration-150"
                    {...form.register("email")}
                  />
                  {form.formState.errors.email ? (
                    <span className="mt-1.5 block text-xs text-red-300">{form.formState.errors.email.message}</span>
                  ) : null}
                </label>
                <label className="block text-sm">
                  <span className="mb-1.5 block font-medium text-[var(--text-secondary)]">Senha</span>
                  <input
                    type="password"
                    className="focusable h-11 w-full rounded-xl border border-[var(--border)] bg-[var(--bg-elevated)] px-3.5 outline-none transition-colors duration-150"
                    {...form.register("password")}
                  />
                  {form.formState.errors.password ? (
                    <span className="mt-1.5 block text-xs text-red-300">{form.formState.errors.password.message}</span>
                  ) : null}
                </label>
                {loginMutation.isError ? (
                  <p className="rounded-xl border border-red-500/25 bg-red-500/10 px-3 py-2.5 text-sm text-red-300">
                    Credenciais invalidas ou servidor indisponivel.
                  </p>
                ) : null}
                <button
                  disabled={loginMutation.isPending || cooldownSeconds > 0}
                  className="focusable h-11 w-full rounded-xl bg-[var(--accent)] px-4 font-semibold text-white shadow-[0_8px_24px_rgba(79,124,255,0.35)] transition-all duration-150 hover:bg-[var(--accent-hover)] disabled:cursor-not-allowed disabled:opacity-45"
                >
                  {loginMutation.isPending
                    ? "Entrando..."
                    : cooldownSeconds > 0
                      ? `Tente novamente em ${cooldownSeconds}s`
                      : "Entrar"}
                </button>
              </form>
              <p className="mt-5 text-sm text-[var(--text-secondary)]">
                Nao tem conta?{" "}
                <Link className="font-semibold text-[var(--accent-hover)]" href="/register">
                  Cadastre-se
                </Link>
              </p>
            </div>
          </div>
        </div>
      </section>
    </main>
  );
}
