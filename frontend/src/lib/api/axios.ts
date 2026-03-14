import axios, { AxiosError, InternalAxiosRequestConfig } from "axios";
import { useAuthStore } from "@/lib/store/auth.store";
import type { AuthResponse } from "@/types/api";

function resolveBaseURL() {
  if (typeof window !== "undefined") {
    return "/api/v1";
  }

  const rawBaseURL = process.env.API_URL ?? "";
  const normalizedBaseURL = rawBaseURL.replace(/\/+$/, "");

  if (!normalizedBaseURL) {
    return "/api/v1";
  }

  return normalizedBaseURL.endsWith("/api/v1")
    ? normalizedBaseURL
    : `${normalizedBaseURL}/api/v1`;
}

const api = axios.create({
  baseURL: resolveBaseURL(),
  timeout: 10000,
  withCredentials: true,
  headers: { "Content-Type": "application/json" },
});

interface RetryableConfig extends InternalAxiosRequestConfig {
  _retry?: boolean;
}

let isRefreshing = false;
let failedQueue: Array<{
  resolve: (value: string) => void;
  reject: (reason?: unknown) => void;
}> = [];

const processQueue = (error: unknown, token: string | null) => {
  failedQueue.forEach((promise) => {
    if (error || !token) {
      promise.reject(error);
      return;
    }
    promise.resolve(token);
  });

  failedQueue = [];
};

api.interceptors.request.use((config) => {
  const token = useAuthStore.getState().accessToken;
  if (token) {
    config.headers = config.headers ?? {};
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const originalRequest = error.config as RetryableConfig | undefined;
    if (!originalRequest) {
      return Promise.reject(error);
    }

    const isUnauthorized = error.response?.status === 401;
    const isRefreshCall = originalRequest.url?.includes("/auth/refresh");

    if (!isUnauthorized || isRefreshCall || originalRequest._retry) {
      return Promise.reject(error);
    }

    originalRequest._retry = true;

    if (isRefreshing) {
      return new Promise((resolve, reject) => {
        failedQueue.push({
          resolve: (token) => {
            originalRequest.headers = originalRequest.headers ?? {};
            originalRequest.headers.Authorization = `Bearer ${token}`;
            resolve(api(originalRequest));
          },
          reject,
        });
      });
    }

    isRefreshing = true;

    try {
      const response = await api.post<AuthResponse>("/auth/refresh");
      const next = response.data;

      useAuthStore.getState().setAuth({
        user: {
          id: next.userId,
          email: next.email,
          displayName: next.displayName,
          aiAccessAllowed: next.aiAccessAllowed,
        },
        accessToken: next.accessToken,
      });

      processQueue(null, next.accessToken);
      originalRequest.headers = originalRequest.headers ?? {};
      originalRequest.headers.Authorization = `Bearer ${next.accessToken}`;
      return api(originalRequest);
    } catch (refreshError) {
      processQueue(refreshError, null);
      useAuthStore.getState().logout();
      if (typeof window !== "undefined") {
        window.location.assign("/login");
      }
      return Promise.reject(refreshError);
    } finally {
      isRefreshing = false;
    }
  },
);

export default api;
