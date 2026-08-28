import axios from "axios";
import type { AxiosError } from "axios";
import router from "../router";
import pinia from "../stores/pinia";
import { useAuthStore } from "../stores/auth";

const http = axios.create({
  baseURL: "/api",
  timeout: 10_000,
});

http.interceptors.request.use((config) => {
  const authStore = useAuthStore(pinia);
  if (authStore.token) {
    config.headers.Authorization = `Bearer ${authStore.token}`;
  }
  return config;
});

http.interceptors.response.use(
  (response) => response,
  async (error: AxiosError<{ message?: string }>) => {
    if (error.response?.status === 401) {
      useAuthStore(pinia).clearSession();

      const requestUrl = error.config?.url ?? "";
      const isPublicAuthRequest = [
        "/auth/email-code",
        "/auth/register",
        "/auth/login",
      ].some((path) => requestUrl.includes(path));

      if (!isPublicAuthRequest && router.currentRoute.value.path !== "/login") {
        const currentRoute = router.currentRoute.value.fullPath;
        await router.replace({
          name: "login",
          query: { redirect: currentRoute },
        });
      }
    }
    return Promise.reject(error);
  },
);

export function getErrorMessage(
  error: unknown,
  fallback = "请求失败，请稍后重试",
) {
  if (axios.isAxiosError<{ message?: string }>(error)) {
    return error.response?.data?.message ?? fallback;
  }
  return fallback;
}

export default http;
