import type { ApiResponse } from "./category";
import http from "./http";
import type { PageResponse } from "./product";

export type OrderStatus =
  | "PENDING_PAYMENT"
  | "CANCELLED"
  | "PAID"
  | "COMPLETED";

export interface OrderItem {
  id: number;
  orderNo: string;
  buyerId: number;
  buyerName: string;
  sellerId: number;
  sellerName: string;
  productId: number;
  productTitle: string;
  productImageBase64: string | null;
  amount: number;
  status: OrderStatus;
  createdAt: string;
  paidAt: string | null;
  completedAt: string | null;
  updatedAt: string;
}

export interface OrderListQuery {
  page: number;
  pageSize: number;
}

export function createOrder(productId: number) {
  return http.post<ApiResponse<OrderItem>>("/orders", { productId });
}

export function getBuyingOrders(params: OrderListQuery) {
  return http.get<ApiResponse<PageResponse<OrderItem>>>("/orders/buying", {
    params,
  });
}

export function getSellingOrders(params: OrderListQuery) {
  return http.get<ApiResponse<PageResponse<OrderItem>>>("/orders/selling", {
    params,
  });
}

export function cancelOrder(orderId: number) {
  return http.post<ApiResponse<null>>(`/orders/${orderId}/cancel`);
}

export function payOrder(orderId: number) {
  return http.post<ApiResponse<null>>(`/orders/${orderId}/pay`);
}

export function completeOrder(orderId: number) {
  return http.post<ApiResponse<null>>(`/orders/${orderId}/complete`);
}
