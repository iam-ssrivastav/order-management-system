package com.enterprise.order.dto;

import java.math.BigDecimal;

public class OrderRequest {
    private String productId;
    private Integer quantity;
    private BigDecimal price;
    private String customerId;

    public OrderRequest() {
    }

    public OrderRequest(String productId, Integer quantity, BigDecimal price, String customerId) {
        this.productId = productId;
        this.quantity = quantity;
        this.price = price;
        this.customerId = customerId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }
}
