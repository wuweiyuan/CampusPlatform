import type { ApiResponse } from "./category";
import type { OrderItem, OrderStatus } from "./order";
import type { PageResponse } from "./product";
import http from "./http";

export interface AdminOrderQuery {
  page: number;
  pageSize: number;
  orderNo?: string;
  status?: OrderStatus;
  buyerId?: number;
  sellerId?: number;
}

export function getAdminOrders(params: AdminOrderQuery) {
  return http.get<ApiResponse<PageResponse<OrderItem>>>("/admin/orders", {
    params,
  });
}
