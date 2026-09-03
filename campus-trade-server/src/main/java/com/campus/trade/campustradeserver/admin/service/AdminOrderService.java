package com.campus.trade.campustradeserver.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.trade.campustradeserver.admin.dto.AdminOrderQuery;
import com.campus.trade.campustradeserver.common.api.PageResponse;
import com.campus.trade.campustradeserver.order.mapper.OrderMapper;
import com.campus.trade.campustradeserver.order.vo.OrderPageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AdminOrderService {
    private final OrderMapper orderMapper;

    public PageResponse<OrderPageResponse> listOrders(AdminOrderQuery query) {
        String orderNo = normalize(query.getOrderNo());
        String status = query.getStatus() == null
                ? null
                : query.getStatus().getCode();
        Page<OrderPageResponse> page = new Page<>(
                query.getPage(),
                query.getPageSize()
        );
        IPage<OrderPageResponse> result = orderMapper.selectAdminOrderPage(
                page,
                orderNo,
                status,
                query.getBuyerId(),
                query.getSellerId()
        );
        PageResponse<OrderPageResponse> response = new PageResponse<>();
        response.setPage(Math.toIntExact(result.getCurrent()));
        response.setPageSize(Math.toIntExact(result.getSize()));
        response.setTotal(result.getTotal());
        response.setRecords(result.getRecords());
        return response;

    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
