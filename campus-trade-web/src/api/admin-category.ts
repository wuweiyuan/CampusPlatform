import type { ApiResponse } from "./category";
import http from "./http";

export type CategoryStatus = "ENABLED" | "DISABLED";

export interface AdminCategory {
  id: number;
  name: string;
  sort: number;
  status: CategoryStatus;
  createdAt: string;
  updatedAt: string;
}

export interface CategoryPayload {
  name: string;
  sort: number;
}

export function getAdminCategories() {
  return http.get<ApiResponse<AdminCategory[]>>("/admin/categories");
}

export function createAdminCategory(payload: CategoryPayload) {
  return http.post<ApiResponse<AdminCategory>>("/admin/categories", payload);
}

export function updateAdminCategory(id: number, payload: CategoryPayload) {
  return http.patch<ApiResponse<AdminCategory>>(
    `/admin/categories/${id}`,
    payload,
  );
}

export function updateAdminCategoryStatus(
  id: number,
  status: CategoryStatus,
) {
  return http.patch<ApiResponse<AdminCategory>>(`/admin/categories/${id}/status`, {
    status,
  });
}
