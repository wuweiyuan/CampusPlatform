import http from "./http";
import type { ApiResponse } from "./category";

export type ProductStatus = "ON_SALE" | "LOCKED" | "SOLD" | "OFF_SHELF";

export interface PageResponse<T> {
  page: number;
  pageSize: number;
  total: number;
  records: T[];
}

export interface ProductCard {
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
}

export interface ProductDetail extends ProductCard {
  description: string;
  updatedAt: string;
}

export interface ProductPayload {
  categoryId: number;
  title: string;
  description: string;
  price: number;
  imageBase64: string | null;
}

export interface ProductListQuery {
  page: number;
  pageSize: number;
  categoryId?: number;
  keyword?: string;
}

export interface MyProductQuery {
  page: number;
  pageSize: number;
  keyword?: string;
  status?: ProductStatus;
}

export function getProducts(params: ProductListQuery) {
  return http.get<ApiResponse<PageResponse<ProductCard>>>("/products", {
    params,
  });
}

export function getProduct(id: number) {
  return http.get<ApiResponse<ProductDetail>>(`/products/${id}`);
}

export function createProduct(payload: ProductPayload) {
  return http.post<ApiResponse<ProductDetail>>("/products", payload);
}

export function updateProduct(id: number, payload: ProductPayload) {
  return http.put<ApiResponse<ProductDetail>>(`/products/${id}`, payload);
}

export function offShelfProduct(id: number) {
  return http.post<ApiResponse<null>>(`/products/${id}/off-shelf`);
}

export function getMyProducts(params: MyProductQuery) {
  return http.get<ApiResponse<PageResponse<ProductCard>>>("/products/mine", {
    params,
  });
}
