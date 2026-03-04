import api from "@/lib/api/axios";
import type { MarkNotificationsSeenRequest, MessageResponse, NotificationListResponse } from "@/types/api";

export const getNotifications = (params?: { days?: number; unseenOnly?: boolean }) =>
  api.get<NotificationListResponse>("/notifications", { params });

export const markNotificationsSeen = (body: MarkNotificationsSeenRequest) =>
  api.patch<MessageResponse>("/notifications/mark-seen", body);
