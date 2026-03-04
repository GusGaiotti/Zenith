import { create } from "zustand";

type AuthUser = {
  id: number;
  email: string;
  displayName: string;
};

interface AuthState {
  user: AuthUser | null;
  accessToken: string | null;
  activeLedgerId: number | null;
  hydrated: boolean;
  authResolved: boolean;
  hydrate: () => void;
  setAuthResolved: (resolved: boolean) => void;
  setSession: (payload: {
    user: AuthUser;
    accessToken: string;
  }) => void;
  setAuth: (payload: {
    user: AuthUser;
    accessToken: string;
  }) => void;
  setActiveLedger: (id: number | null) => void;
  logout: () => void;
}

const STORAGE_KEY = "zen_auth_state_v2";
const LEGACY_LEDGER_KEY = "zen_active_ledger_id";
const LEGACY_STORAGE_KEY = "zen_auth_state_v1";

type PersistedAuthState = Pick<AuthState, "user" | "activeLedgerId">;

function getSessionStorage() {
  if (typeof window === "undefined") return null;
  return window.sessionStorage;
}

function persist(state: PersistedAuthState) {
  const storage = getSessionStorage();
  if (!storage) return;
  storage.setItem(STORAGE_KEY, JSON.stringify(state));
}

export const useAuthStore = create<AuthState>((set) => ({
  user: null,
  accessToken: null,
  activeLedgerId: null,
  hydrated: false,
  authResolved: false,
  hydrate: () => {
    if (typeof window === "undefined") return;
    const storage = getSessionStorage();
    const raw = storage?.getItem(STORAGE_KEY);
    const legacyRaw = window.localStorage.getItem(LEGACY_STORAGE_KEY);
    const source = raw ?? legacyRaw;

    let legacyLedgerId: number | null = null;
    const legacy = window.localStorage.getItem(LEGACY_LEDGER_KEY);
    if (legacy) {
      const parsed = Number(legacy);
      legacyLedgerId = Number.isFinite(parsed) ? parsed : null;
    }

    if (!source) {
      set({ hydrated: true, activeLedgerId: legacyLedgerId });
      return;
    }

    try {
      const parsed = JSON.parse(source) as PersistedAuthState;

      const hydratedState: PersistedAuthState = {
        user: parsed.user ?? null,
        activeLedgerId: parsed.activeLedgerId ?? legacyLedgerId ?? null,
      };

      set({
        ...hydratedState,
        accessToken: null,
        hydrated: true,
      });

      if (!raw && legacyRaw && storage) {
        storage.setItem(STORAGE_KEY, JSON.stringify(hydratedState));
        window.localStorage.removeItem(LEGACY_STORAGE_KEY);
      }
    } catch {
      set({ hydrated: true, activeLedgerId: legacyLedgerId });
    }
  },
  setAuthResolved: (resolved) => {
    set({ authResolved: resolved });
  },
  setSession: ({ user, accessToken }) => {
    set({ user, accessToken });
    persist({
      user,
      activeLedgerId: useAuthStore.getState().activeLedgerId,
    });
  },
  setAuth: ({ user, accessToken }) => {
    set({ user, accessToken, authResolved: true });
    persist({
      user,
      activeLedgerId: useAuthStore.getState().activeLedgerId,
    });
  },
  setActiveLedger: (id) => {
    set({ activeLedgerId: id });
    const state = useAuthStore.getState();
    persist({
      user: state.user,
      activeLedgerId: id,
    });
    if (typeof window !== "undefined") {
      if (id) {
        window.localStorage.setItem(LEGACY_LEDGER_KEY, String(id));
      } else {
        window.localStorage.removeItem(LEGACY_LEDGER_KEY);
      }
    }
  },
  logout: () => {
    set({
      user: null,
      accessToken: null,
      activeLedgerId: null,
      authResolved: true,
    });
    if (typeof window !== "undefined") {
      const storage = getSessionStorage();
      storage?.removeItem(STORAGE_KEY);
      window.localStorage.removeItem(LEGACY_STORAGE_KEY);
      window.localStorage.removeItem(LEGACY_LEDGER_KEY);
    }
  },
}));
