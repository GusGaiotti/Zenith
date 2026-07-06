import api from "@/lib/api/axios";
import type { MessageResponse, UpdateProfileRequest, UserProfileResponse } from "@/types/api";

export const getMyProfile = () => api.get<UserProfileResponse>("/users/me");
export const updateMyProfile = (body: UpdateProfileRequest) => api.patch<UserProfileResponse>("/users/me", body);
export const deleteMyAccount = () => api.delete<MessageResponse>("/users/me");
