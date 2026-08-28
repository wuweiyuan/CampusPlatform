import http from "./http";

export interface Category {
  id: number;
  name: string;
  sort: number;
}

export interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
}

export function getCategories() {
  return http.get<ApiResponse<Category[]>>("/categories");
}
