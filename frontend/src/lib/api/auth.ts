import api from "@/lib/api/axios";
import type { AuthResponse, LoginRequest, MessageResponse, RegisterRequest } from "@/types/api";

export const login = (body: LoginRequest) => api.post<AuthResponse>("/auth/login", body);
export const register = (body: RegisterRequest) => api.post<AuthResponse>("/auth/register", body);
export const logout = () => api.post<MessageResponse>("/auth/logout");
export const forgotPassword = (email: string) => api.post<MessageResponse>("/auth/forgot-password", { email });
export const resetPassword = (token: string, newPassword: string) =>
  api.post<MessageResponse>("/auth/reset-password", { token, newPassword });
