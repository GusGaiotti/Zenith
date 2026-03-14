import type { SVGProps } from "react";

type IconName =
  | "dashboard"
  | "transactions"
  | "categories"
  | "ledger"
  | "ask-ai"
  | "collapse"
  | "expand";

type NavItem = {
  href: string;
  label: string;
  icon: IconName;
};

const iconClassName = "h-4 w-4";

export const APP_NAV_ITEMS: NavItem[] = [
  { href: "/dashboard", label: "Dashboard", icon: "dashboard" },
  { href: "/transactions", label: "Transacoes", icon: "transactions" },
  { href: "/categories", label: "Categorias", icon: "categories" },
  { href: "/ledger", label: "Fatura", icon: "ledger" },
];

function IconBase(props: SVGProps<SVGSVGElement>) {
  return (
    <svg
      aria-hidden
      fill="none"
      stroke="currentColor"
      strokeLinecap="round"
      strokeLinejoin="round"
      strokeWidth="1.8"
      viewBox="0 0 24 24"
      {...props}
    />
  );
}

export function NavigationIcon({ name, className = iconClassName }: { name: IconName; className?: string }) {
  switch (name) {
    case "dashboard":
      return (
        <IconBase className={className}>
          <path d="M4 5.5h7v5H4z" />
          <path d="M13 5.5h7v8h-7z" />
          <path d="M4 12.5h7V20H4z" />
          <path d="M13 15h7v5h-7z" />
        </IconBase>
      );
    case "transactions":
      return (
        <IconBase className={className}>
          <path d="M6 7h12" />
          <path d="m13 4 3 3-3 3" />
          <path d="M18 17H6" />
          <path d="m11 14-3 3 3 3" />
        </IconBase>
      );
    case "categories":
      return (
        <IconBase className={className}>
          <path d="M12 4.5 4.5 8.7 12 13l7.5-4.3L12 4.5Z" />
          <path d="M4.5 15.3 12 19.5l7.5-4.2" />
          <path d="M4.5 12 12 16.2l7.5-4.2" />
        </IconBase>
      );
    case "ledger":
      return (
        <IconBase className={className}>
          <rect x="4" y="5" width="16" height="14" rx="2.5" />
          <path d="M4 10.5h16" />
          <path d="M8 15h3" />
          <path d="M14 15h2" />
        </IconBase>
      );
    case "ask-ai":
      return (
        <IconBase className={className}>
          <path d="M12 3.5c4.97 0 9 3.36 9 7.5 0 2.35-1.3 4.44-3.33 5.82l.33 3.68-3.78-1.84c-.7.13-1.45.2-2.22.2-4.97 0-9-3.36-9-7.5s4.03-7.86 9-7.86Z" />
          <path d="M9.4 10.2a2.7 2.7 0 0 1 5.2 0c0 1.1-.62 1.73-1.47 2.3-.67.45-1.03.75-1.03 1.5" />
          <path d="M12 16.2h.01" />
        </IconBase>
      );
    case "collapse":
      return (
        <IconBase className={className}>
          <path d="M8 5v14" />
          <path d="m16 7-4 5 4 5" />
        </IconBase>
      );
    case "expand":
      return (
        <IconBase className={className}>
          <path d="M16 5v14" />
          <path d="m8 7 4 5-4 5" />
        </IconBase>
      );
  }
}
