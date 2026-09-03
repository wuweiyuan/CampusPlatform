import type { ApiResponse } from "./category";
import type { PageResponse, ProductStatus } from "./product";
import http from "./http";

export interface AdminProduct {
  id: number;
  title: string;
  price: number;
  imageBase64: string | null;
  status: ProductStatus;
  categoryId: number;
  categoryName: string;
  sellerId: number;
  sellerName: string;
  viewCount: number;
  createdAt: string;
  updatedAt: string;
}

export interface AdminProductQuery {
  page: number;
  pageSize: number;
  sellerId?: number;
  categoryId?: number;
  status?: ProductStatus;
  keyword?: string;
}

export function getAdminProducts(params: AdminProductQuery) {
  return http.get<ApiResponse<PageResponse<AdminProduct>>>("/admin/products", {
    params,
  });
}

export function offShelfAdminProduct(id: number) {
  return http.patch<ApiResponse<void>>(`/admin/products/${id}/off-shelf`);
}
