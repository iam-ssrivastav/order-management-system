package com.enterprise.inventory.controller;

import com.enterprise.inventory.entity.Inventory;
import com.enterprise.inventory.service.InventoryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/{productId}")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('USER', 'WAREHOUSE', 'MANAGER', 'ADMIN', 'SUPPORT')")
    public Inventory getInventory(@PathVariable String productId) {
        return inventoryService.getInventory(productId);
    }

    @org.springframework.web.bind.annotation.PostMapping
    @org.springframework.web.bind.annotation.ResponseStatus(org.springframework.http.HttpStatus.OK)
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('WAREHOUSE', 'MANAGER', 'ADMIN')")
    public Inventory addStock(
            @org.springframework.web.bind.annotation.RequestBody com.enterprise.inventory.dto.StockUpdateRequest request) {
        return inventoryService.addStock(request.getProductId(), request.getQuantity());
    }

    @org.springframework.web.bind.annotation.PostMapping("/deduct")
    @org.springframework.web.bind.annotation.ResponseStatus(org.springframework.http.HttpStatus.OK)
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public void deductStock(
            @org.springframework.web.bind.annotation.RequestBody com.enterprise.inventory.dto.StockUpdateRequest request) {
        inventoryService.deductStock(request.getProductId(), request.getQuantity());
    }
}
