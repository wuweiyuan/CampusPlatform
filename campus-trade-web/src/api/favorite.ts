import http from "./http";
import type { ApiResponse } from "./category";
import type { PageResponse, ProductStatus } from "./product";

export interface FavoriteProduct {
  id: number;
  productId: number;
  title: string;
  price: number;
  imageBase64: string | null;
  status: ProductStatus;
  categoryId: number;
  categoryName: string;
  sellerId: number;
  sellerName: string;
  favoriteCreatedAt: string;
}

export interface FavoriteListQuery {
  page: number;
  pageSize: number;
}

export function addFavorite(productId: number) {
  return http.post<ApiResponse<null>>(`/products/${productId}/favorite`);
}

export function removeFavorite(productId: number) {
  return http.delete<ApiResponse<null>>(`/products/${productId}/favorite`);
}

export function getFavorites(params: FavoriteListQuery) {
  return http.get<ApiResponse<PageResponse<FavoriteProduct>>>("/favorites", {
    params,
  });
}
