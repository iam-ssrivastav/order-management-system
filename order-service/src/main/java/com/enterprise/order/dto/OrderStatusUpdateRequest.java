package com.enterprise.order.dto;

import com.enterprise.order.model.OrderStatus;

public class OrderStatusUpdateRequest {
    private OrderStatus status;

    public OrderStatusUpdateRequest() {
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }
}
