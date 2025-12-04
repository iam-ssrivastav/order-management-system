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

    // ============ MANAGER ENDPOINTS ============

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN', 'AUDITOR')")
    @Operation(summary = "View all orders (MANAGER/AUDITOR)", description = "📊 **MANAGER/AUDITOR** - Retrieves all orders in the system. Regular users can only view their own orders.")
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    // ============ SUPPORT ENDPOINTS ============

    @PostMapping("/{id}/refund-request")
    @PreAuthorize("hasAnyRole('SUPPORT', 'MANAGER', 'ADMIN')")
    @Operation(summary = "Request refund (SUPPORT)", description = "💬 **SUPPORT** - Initiates a refund request for an order. Finance team will process the actual refund.")
    @Parameter(name = "id", description = "Order ID", example = "1")
    @Parameter(name = "reason", description = "Reason for refund request", example = "Customer complaint")
    public ResponseEntity<OrderResponse> requestRefund(
            @PathVariable Long id,
            @RequestParam String reason) {
        OrderResponse order = orderService.requestRefund(id, reason);
        return ResponseEntity.ok(order);
    }

    // ============ FINANCE ENDPOINTS ============

    @PostMapping("/{id}/refund")
    @PreAuthorize("hasAnyRole('FINANCE', 'ADMIN')")
    @Operation(summary = "Process refund (FINANCE)", description = "💰 **FINANCE ONLY** - Processes the actual refund for an order. Triggers payment service to refund customer.")
    @Parameter(name = "id", description = "Order ID to refund", example = "1")
    public ResponseEntity<OrderResponse> processRefund(@PathVariable Long id) {
        OrderResponse order = orderService.processRefund(id);
        return ResponseEntity.ok(order);
    }

    // ============ WAREHOUSE ENDPOINTS ============

    @PostMapping("/{id}/ship")
    @PreAuthorize("hasAnyRole('WAREHOUSE', 'MANAGER', 'ADMIN')")
    @Operation(summary = "Ship order (WAREHOUSE)", description = "📦 **WAREHOUSE** - Marks order as shipped and adds tracking number.")
    @Parameter(name = "id", description = "Order ID to ship", example = "1")
    @Parameter(name = "trackingNumber", description = "Shipping tracking number", example = "TRACK123456")
    public ResponseEntity<OrderResponse> shipOrder(
            @PathVariable Long id,
            @RequestParam String trackingNumber) {
        OrderResponse order = orderService.shipOrder(id, trackingNumber);
        return ResponseEntity.ok(order);
    }

    @PostMapping("/{id}/deliver")
    @PreAuthorize("hasAnyRole('WAREHOUSE', 'MANAGER', 'ADMIN')")
    @Operation(summary = "Mark as delivered (WAREHOUSE)", description = "✅ **WAREHOUSE** - Marks order as delivered to customer.")
    @Parameter(name = "id", description = "Order ID to mark as delivered", example = "1")
    public ResponseEntity<OrderResponse> markDelivered(@PathVariable Long id) {
        OrderResponse order = orderService.markDelivered(id);
        return ResponseEntity.ok(order);
    }

    // ============ AUDITOR ENDPOINTS ============

    @GetMapping("/export")
    @PreAuthorize("hasAnyRole('AUDITOR', 'ADMIN')")
    @Operation(summary = "Export orders (AUDITOR)", description = "📄 **AUDITOR** - Exports all orders as CSV for audit purposes. Read-only operation.")
    public ResponseEntity<String> exportOrders() {
        String csv = orderService.exportOrdersAsCsv();
        return ResponseEntity.ok()
                .header("Content-Type", "text/csv")
                .header("Content-Disposition", "attachment; filename=orders.csv")
                .body(csv);
    }
}
