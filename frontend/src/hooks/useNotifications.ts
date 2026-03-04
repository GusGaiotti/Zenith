"use client";

import { useMutation, useQuery } from "@tanstack/react-query";
import { getNotifications, markNotificationsSeen } from "@/lib/api/notifications";
import { queryKeys } from "@/lib/api/query-keys";

export function useNotifications(days = 7, unseenOnly = true) {
  return useQuery({
    queryKey: queryKeys.notifications(`days-${days}-unseen-${String(unseenOnly)}`),
    queryFn: () => getNotifications({ days, unseenOnly }).then((response) => response.data),
  });
}

export function useMarkNotificationsSeen() {
  return useMutation({
    mutationFn: (ids: number[]) => markNotificationsSeen({ ids }).then((response) => response.data),
  });
}
