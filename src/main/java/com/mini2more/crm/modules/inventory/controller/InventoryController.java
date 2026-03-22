/** Copyright © 2026 Mini2More. All Rights Reserved. Product: Mini2More CRM **/
package com.mini2more.crm.modules.inventory.controller;
import com.mini2more.crm.modules.inventory.entity.StockTransfer;
import com.mini2more.crm.modules.inventory.service.InventoryService;
import com.mini2more.crm.modules.inventory.entity.InventoryItem;

import com.mini2more.crm.common.dto.ApiResponse;
import com.mini2more.crm.common.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES', 'PURCHASER', 'LOGISTICS')")
    public ResponseEntity<ApiResponse<PageResponse<InventoryItem>>> getInventory(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long branchId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "product.name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        return ResponseEntity.ok(ApiResponse.success(
                inventoryService.getInventory(search, branchId, page, size, sortBy, sortDir)));
    }

    @PostMapping("/adjust")
    @PreAuthorize("hasAnyRole('ADMIN', 'PURCHASER', 'LOGISTICS')")
    public ResponseEntity<ApiResponse<InventoryItem>> adjustStock(@RequestBody Map<String, Object> body) {
        Long productId = Long.valueOf(body.get("productId").toString());
        Long branchId = Long.valueOf(body.get("branchId").toString());
        int quantity = Integer.parseInt(body.get("quantity").toString());
        String reason = body.getOrDefault("reason", "Manual adjustment").toString();
        return ResponseEntity.ok(ApiResponse.success("Stock adjusted",
                inventoryService.adjustStock(productId, branchId, quantity, reason)));
    }

    @PostMapping("/reserve")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES')")
    public ResponseEntity<ApiResponse<InventoryItem>> reserveStock(@RequestBody Map<String, Object> body) {
        Long productId = Long.valueOf(body.get("productId").toString());
        Long branchId = Long.valueOf(body.get("branchId").toString());
        int quantity = Integer.parseInt(body.get("quantity").toString());
        return ResponseEntity.ok(ApiResponse.success("Stock reserved",
                inventoryService.reserveStock(productId, branchId, quantity)));
    }

    @PostMapping("/release")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES')")
    public ResponseEntity<ApiResponse<InventoryItem>> releaseReservation(@RequestBody Map<String, Object> body) {
        Long productId = Long.valueOf(body.get("productId").toString());
        Long branchId = Long.valueOf(body.get("branchId").toString());
        int quantity = Integer.parseInt(body.get("quantity").toString());
        return ResponseEntity.ok(ApiResponse.success("Reservation released",
                inventoryService.releaseReservation(productId, branchId, quantity)));
    }

    @GetMapping("/low-stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'PURCHASER', 'LOGISTICS')")
    public ResponseEntity<ApiResponse<List<InventoryItem>>> getLowStock(
            @RequestParam(required = false) Long branchId) {
        return ResponseEntity.ok(ApiResponse.success(inventoryService.getLowStockItems(branchId)));
    }

    @GetMapping("/product/{productId}/branches")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES', 'PURCHASER', 'LOGISTICS')")
    public ResponseEntity<ApiResponse<List<InventoryItem>>> getProductStockAcrossBranches(
            @PathVariable Long productId) {
        return ResponseEntity.ok(ApiResponse.success(
                inventoryService.getProductStockAcrossBranches(productId)));
    }

    // ─── Stock Transfers ─────────────────────────────────────────

    @PostMapping("/transfers")
    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS')")
    public ResponseEntity<ApiResponse<StockTransfer>> createTransfer(@RequestBody Map<String, Object> body) {
        Long productId = Long.valueOf(body.get("productId").toString());
        Long fromBranchId = Long.valueOf(body.get("fromBranchId").toString());
        Long toBranchId = Long.valueOf(body.get("toBranchId").toString());
        int quantity = Integer.parseInt(body.get("quantity").toString());
        String notes = body.getOrDefault("notes", "").toString();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Transfer created",
                        inventoryService.createTransfer(productId, fromBranchId, toBranchId, quantity, notes)));
    }

    @GetMapping("/transfers")
    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS')")
    public ResponseEntity<ApiResponse<PageResponse<StockTransfer>>> getTransfers(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        return ResponseEntity.ok(ApiResponse.success(
                inventoryService.getTransfers(search, page, size, sortBy, sortDir)));
    }

    @PatchMapping("/transfers/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<StockTransfer>> approveTransfer(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Transfer approved",
                inventoryService.approveTransfer(id)));
    }

    @PatchMapping("/transfers/{id}/receive")
    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS')")
    public ResponseEntity<ApiResponse<StockTransfer>> receiveTransfer(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Transfer received",
                inventoryService.receiveTransfer(id)));
    }
}
