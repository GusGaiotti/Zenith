import api from "@/lib/api/axios";
import type { AuthResponse, LoginRequest, MessageResponse, RegisterRequest } from "@/types/api";

export const login = (body: LoginRequest) => api.post<AuthResponse>("/auth/login", body);
export const register = (body: RegisterRequest) => api.post<AuthResponse>("/auth/register", body);
export const logout = () => api.post<MessageResponse>("/auth/logout");
