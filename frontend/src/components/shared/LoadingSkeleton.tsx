interface LoadingSkeletonProps {
  variant?: "card" | "row" | "stat" | "chart";
}

export function LoadingSkeleton({ variant = "card" }: LoadingSkeletonProps) {
  if (variant === "row") {
    return (
      <div className="surface skeleton-shimmer rounded-xl p-4">
        <div className="h-3 w-1/3 rounded-full bg-white/8" />
        <div className="mt-3 h-3 w-2/3 rounded-full bg-white/6" />
      </div>
    );
  }

  if (variant === "stat") {
    return (
      <div className="surface skeleton-shimmer rounded-xl p-6">
        <div className="h-3 w-1/2 rounded-full bg-white/8" />
        <div className="mt-5 h-10 w-3/4 rounded-2xl bg-white/10" />
        <div className="mt-5 h-6 w-1/3 rounded-full bg-white/7" />
      </div>
    );
  }

  if (variant === "chart") {
    return (
      <div className="surface skeleton-shimmer rounded-xl p-6">
        <div className="h-3 w-1/3 rounded-full bg-white/8" />
        <div className="mt-5 h-48 rounded-2xl bg-white/6" />
        <div className="mt-5 h-3 w-2/3 rounded-full bg-white/7" />
      </div>
    );
  }

  return (
    <div className="surface skeleton-shimmer rounded-xl p-5">
      <div className="h-3 w-2/5 rounded-full bg-white/8" />
      <div className="mt-4 h-3 w-3/4 rounded-full bg-white/7" />
      <div className="mt-3 h-3 w-1/2 rounded-full bg-white/6" />
    </div>
  );
}
