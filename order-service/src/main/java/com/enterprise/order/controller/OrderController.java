package com.enterprise.order.controller;

import com.enterprise.order.dto.OrderRequest;
import com.enterprise.order.dto.OrderResponse;
import com.enterprise.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Orders", description = "Order management APIs with Role-Based Access Control")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new order", description = "Creates a new order and triggers vendor processing workflow. Requires USER or ADMIN role.")
    public OrderResponse placeOrder(@RequestBody OrderRequest orderRequest) {
        return orderService.placeOrder(orderRequest);
    }

    @GetMapping("/{orderId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get order by ID", description = "Retrieves order details by order ID. Requires USER or ADMIN role.")
    @Parameter(name = "orderId", description = "Order ID", example = "1")
    public OrderResponse getOrder(@PathVariable Long orderId) {
        return orderService.getOrder(orderId);
    }

    @PutMapping("/{orderId}/status")
    @ResponseStatus(HttpStatus.OK)
    @io.swagger.v3.oas.annotations.Operation(summary = "Update order status", description = "Changes the lifecycle status of an existing order")
    public com.enterprise.order.dto.OrderResponse updateOrderStatus(
            @PathVariable Long orderId,
            @RequestBody com.enterprise.order.dto.OrderStatusUpdateRequest request) {
        return orderService.updateStatus(orderId, request.getStatus());
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get customer orders", description = "Retrieves all orders for a specific customer. Requires USER or ADMIN role.")
    @Parameter(name = "customerId", description = "Customer ID", example = "testuser")
    public ResponseEntity<List<OrderResponse>> getOrdersByCustomer(@PathVariable String customerId) {
        return ResponseEntity.ok(orderService.getOrdersByCustomer(customerId));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cancel an order (ADMIN only)", description = "⚠️ **ADMIN ONLY** - Cancels an existing order and triggers compensating transactions (payment refund + inventory restoration). Returns 403 Forbidden if user does not have ADMIN role.")
    @Parameter(name = "id", description = "Order ID to cancel", example = "1")
    @Parameter(name = "reason", description = "Reason for cancellation (optional)", example = "Customer requested")
    public ResponseEntity<OrderResponse> cancelOrder(
            @PathVariable Long id,
            @RequestParam(required = false) String reason) {
        try {
            OrderResponse cancelledOrder = orderService.cancelOrder(id, reason);
            return ResponseEntity.ok(cancelledOrder);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
