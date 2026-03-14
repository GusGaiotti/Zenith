"use client";

import Link from "next/link";
import { useEffect, useMemo, useRef, useState } from "react";
import { useMarkNotificationsSeen, useNotifications } from "@/hooks/useNotifications";
import { formatDateTime } from "@/lib/utils/date";

function BellIcon({ className }: { className?: string }) {
  return (
    <svg
      aria-hidden
      className={className}
      fill="none"
      stroke="currentColor"
      strokeLinecap="round"
      strokeLinejoin="round"
      strokeWidth="1.8"
      viewBox="0 0 24 24"
    >
      <path d="M6.5 16.5h11" />
      <path d="M8 16.5V10a4 4 0 1 1 8 0v6.5" />
      <path d="M9.5 19a2.5 2.5 0 0 0 5 0" />
      <path d="M5 16.5h14" />
    </svg>
  );
}

export function NotificationBell() {
  const [open, setOpen] = useState(false);
  const [sessionSeenIds, setSessionSeenIds] = useState<number[]>([]);
  const wrapperRef = useRef<HTMLDivElement>(null);
  const notifications = useNotifications(7, false);
  const markSeen = useMarkNotificationsSeen();

  const unreadCount = useMemo(() => {
    const items = notifications.data?.items ?? [];
    return items.filter((item) => item.seenAt == null && !sessionSeenIds.includes(item.id)).length;
  }, [notifications.data?.items, sessionSeenIds]);

  const items = notifications.data?.items ?? [];

  useEffect(() => {
    if (!open) {
      return;
    }

    const handlePointerDown = (event: MouseEvent) => {
      if (!wrapperRef.current?.contains(event.target as Node)) {
        setOpen(false);
      }
    };

    const handleEscape = (event: KeyboardEvent) => {
      if (event.key === "Escape") {
        setOpen(false);
      }
    };

    document.addEventListener("mousedown", handlePointerDown);
    document.addEventListener("keydown", handleEscape);

    return () => {
      document.removeEventListener("mousedown", handlePointerDown);
      document.removeEventListener("keydown", handleEscape);
    };
  }, [open]);

  function handleToggle() {
    const nextOpen = !open;
    setOpen(nextOpen);

    if (!open) {
      const unseenIds = items
        .map((item) => item.id)
        .filter((id) => !sessionSeenIds.includes(id));

      if (unseenIds.length) {
        setSessionSeenIds((current) => [...current, ...unseenIds]);
        markSeen.mutate(unseenIds);
      }
    }
  }

  function handleMarkAllSeen() {
    const unseenIds = items
      .filter((item) => item.seenAt == null && !sessionSeenIds.includes(item.id))
      .map((item) => item.id);

    if (!unseenIds.length) {
      return;
    }

    setSessionSeenIds((current) => [...current, ...unseenIds]);
    markSeen.mutate(unseenIds);
  }

  return (
    <div ref={wrapperRef} className="relative">
      <button
        type="button"
        aria-label="Abrir notificacoes"
        aria-expanded={open}
        className="focusable relative grid h-12 w-12 place-items-center rounded-2xl border border-[var(--surface-edge)] bg-[var(--bg-elevated)] text-[var(--text-primary)] transition-colors duration-150 hover:border-[var(--accent)]"
        onClick={handleToggle}
      >
        <BellIcon className="h-5 w-5" />
        {unreadCount > 0 ? (
          <span className="absolute bottom-1 left-1 min-w-5 rounded-full bg-red-500 px-1.5 py-0.5 text-[10px] font-semibold text-white">
            {unreadCount > 9 ? "9+" : unreadCount}
          </span>
        ) : null}
      </button>

      {open ? (
        <div className="absolute right-0 top-[calc(100%+0.5rem)] z-40 w-[min(340px,calc(100vw-1.5rem))] rounded-2xl border border-[var(--surface-edge)] bg-[var(--menu-bg)] p-3 shadow-[0_20px_60px_rgba(0,0,0,0.18)] backdrop-blur">
          <div className="mb-3 flex items-center justify-between">
            <p className="text-sm font-medium text-[var(--text-primary)]">Notificacoes</p>
            <div className="flex items-center gap-3">
              <button
                type="button"
                className="text-[11px] font-medium uppercase tracking-[0.08em] text-[var(--accent-hover)] transition-opacity duration-150 hover:opacity-80 disabled:opacity-30"
                onClick={handleMarkAllSeen}
                disabled={unreadCount === 0 || markSeen.isPending}
              >
                Marcar tudo
              </button>
              <span className="text-[11px] uppercase tracking-[0.08em] text-[var(--text-muted)]">7 dias</span>
            </div>
          </div>

          {notifications.isLoading ? (
            <p className="text-sm text-[var(--text-secondary)]">Carregando...</p>
          ) : null}

          {notifications.isError ? (
            <p className="text-sm text-red-300">Nao foi possivel carregar as notificacoes.</p>
          ) : null}

          {!notifications.isLoading && !notifications.isError ? (
            <ul className="max-h-[320px] space-y-2 overflow-y-auto pr-1">
              {items.length ? (
                items.map((item) => (
                  <li
                    key={item.id}
                    className={`rounded-xl border p-3 text-sm ${
                      item.seenAt
                        ? "border-[var(--border)] bg-[var(--panel-bg)]"
                        : "border-[var(--accent)] bg-[var(--accent-muted)]"
                    }`}
                  >
                    <p className="font-medium text-[var(--text-primary)]">{item.title}</p>
                    <p className="mt-1 text-[13px] text-[var(--text-secondary)]">{item.body}</p>
                    <div className="mt-2 flex items-center justify-between gap-2">
                      <span className="text-[11px] text-[var(--text-muted)]">{formatDateTime(item.createdAt)}</span>
                      {item.type === "INVITATION_RECEIVED" && item.invitationToken ? (
                        <Link
                          href={`/ledger/join/${item.invitationToken}`}
                          className="text-[11px] font-medium text-[var(--accent-hover)]"
                          onClick={() => setOpen(false)}
                        >
                          Abrir convite
                        </Link>
                      ) : null}
                    </div>
                  </li>
                ))
              ) : (
                <li className="rounded-xl border border-[var(--surface-edge)] bg-[var(--bg-elevated)] p-3 text-sm text-[var(--text-secondary)]">
                  Nada novo por aqui.
                </li>
              )}
            </ul>
          ) : null}

          {markSeen.isError ? (
            <p className="mt-3 text-xs text-amber-300">As notificacoes foram abertas, mas nao foi possivel sincronizar o status.</p>
          ) : null}
        </div>
      ) : null}
    </div>
  );
}
