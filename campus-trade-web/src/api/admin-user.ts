import type { ApiResponse } from "./category";
import type { PageResponse } from "./product";
import http from "./http";

export type UserRole = "USER" | "ADMIN";
export type UserStatus = 0 | 1;

export interface AdminUser {
  id: number;
  username: string;
  email: string;
  role: UserRole;
  status: UserStatus;
  createdAt: string;
  updatedAt: string;
}

export interface AdminUserQuery {
  page: number;
  pageSize: number;
  username?: string;
  email?: string;
  role?: UserRole;
  status?: UserStatus;
}

export function getAdminUsers(params: AdminUserQuery) {
  return http.get<ApiResponse<PageResponse<AdminUser>>>("/admin/users", {
    params,
  });
}

export function updateAdminUserStatus(id: number, status: UserStatus) {
  return http.patch<ApiResponse<void>>(`/admin/users/${id}/status`, { status });
}
