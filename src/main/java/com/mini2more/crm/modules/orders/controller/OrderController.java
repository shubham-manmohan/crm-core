/** Copyright © 2026 Mini2More. All Rights Reserved. Product: Mini2More CRM **/
package com.mini2more.crm.modules.orders.controller;
import com.mini2more.crm.modules.orders.service.OrderService;
import com.mini2more.crm.modules.orders.entity.Dispatch;
import com.mini2more.crm.modules.orders.entity.SalesOrder;

import com.mini2more.crm.common.dto.ApiResponse;
import com.mini2more.crm.common.dto.PageResponse;
import com.mini2more.crm.common.enums.DispatchStatus;
import com.mini2more.crm.common.enums.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES')")
    public ResponseEntity<ApiResponse<SalesOrder>> createOrder(@RequestBody Map<String, Object> payload) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Order created", orderService.createOrder(payload)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES', 'LOGISTICS')")
    public ResponseEntity<ApiResponse<PageResponse<SalesOrder>>> getOrders(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        return ResponseEntity.ok(ApiResponse.success(
                orderService.getOrders(search, status, page, size, sortBy, sortDir)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES', 'LOGISTICS')")
    public ResponseEntity<ApiResponse<SalesOrder>> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(orderService.getById(id)));
    }

    @PatchMapping("/{id}/confirm")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES')")
    public ResponseEntity<ApiResponse<SalesOrder>> confirmOrder(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Order confirmed",
                orderService.confirmOrder(id)));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES')")
    public ResponseEntity<ApiResponse<SalesOrder>> cancelOrder(
            @PathVariable Long id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(ApiResponse.success("Order cancelled",
                orderService.cancelOrder(id, body.getOrDefault("reason", ""))));
    }

    // ─── Dispatch endpoints ─────────────────────────────────

    @PostMapping("/{orderId}/dispatch")
    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS')")
    public ResponseEntity<ApiResponse<Dispatch>> createDispatch(
            @PathVariable Long orderId, @RequestBody Map<String, Object> payload) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Dispatch created",
                        orderService.createDispatch(orderId, payload)));
    }

    @GetMapping("/{orderId}/dispatches")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES', 'LOGISTICS')")
    public ResponseEntity<ApiResponse<List<Dispatch>>> getDispatchesForOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(ApiResponse.success(orderService.getDispatchesForOrder(orderId)));
    }

    @PatchMapping("/dispatch/{dispatchId}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS')")
    public ResponseEntity<ApiResponse<Dispatch>> updateDispatchStatus(
            @PathVariable Long dispatchId, @RequestBody Map<String, String> body) {
        DispatchStatus newStatus = DispatchStatus.valueOf(body.get("status"));
        return ResponseEntity.ok(ApiResponse.success("Dispatch updated",
                orderService.updateDispatchStatus(dispatchId, newStatus)));
    }
}
