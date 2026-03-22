/** Copyright © 2026 Mini2More. All Rights Reserved. Product: Mini2More CRM **/
package com.mini2more.crm.modules.inventory.controller;
import com.mini2more.crm.modules.inventory.service.PurchaseOrderService;
import com.mini2more.crm.modules.inventory.repository.ProductRepository;
import com.mini2more.crm.modules.inventory.entity.GoodsReceipt;
import com.mini2more.crm.modules.inventory.entity.PurchaseOrderItem;
import com.mini2more.crm.modules.inventory.entity.PurchaseOrder;

import com.mini2more.crm.common.dto.ApiResponse;
import com.mini2more.crm.common.dto.PageResponse;
import com.mini2more.crm.common.enums.PurchaseOrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/purchase-orders")
@RequiredArgsConstructor
public class PurchaseOrderController {

    private final PurchaseOrderService poService;
    private final ProductRepository productRepository;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PURCHASER')")
    @SuppressWarnings("unchecked")
    public ResponseEntity<ApiResponse<PurchaseOrder>> createPO(@RequestBody Map<String, Object> body) {
        Long vendorId = Long.valueOf(body.get("vendorId").toString());
        Long branchId = body.get("branchId") != null ? Long.valueOf(body.get("branchId").toString()) : null;
        String paymentTerms = body.getOrDefault("paymentTerms", "").toString();
        String notes = body.getOrDefault("notes", "").toString();

        List<Map<String, Object>> itemsData = (List<Map<String, Object>>) body.get("items");
        List<PurchaseOrderItem> items = new ArrayList<>();

        if (itemsData != null) {
            for (Map<String, Object> itemData : itemsData) {
                PurchaseOrderItem.PurchaseOrderItemBuilder builder = PurchaseOrderItem.builder()
                        .productName(itemData.getOrDefault("productName", "").toString())
                        .productCode(
                                itemData.get("productCode") != null ? itemData.get("productCode").toString() : null)
                        .unit(itemData.getOrDefault("unit", "Nos").toString())
                        .orderedQty(Integer.parseInt(itemData.getOrDefault("orderedQty", "1").toString()))
                        .unitPrice(new BigDecimal(itemData.getOrDefault("unitPrice", "0").toString()))
                        .gstPercent(new BigDecimal(itemData.getOrDefault("gstPercent", "18").toString()))
                        .hsnCode(itemData.get("hsnCode") != null ? itemData.get("hsnCode").toString() : null);

                if (itemData.get("productId") != null) {
                    Long productId = Long.valueOf(itemData.get("productId").toString());
                    productRepository.findById(productId).ifPresent(builder::product);
                }

                items.add(builder.build());
            }
        }

        PurchaseOrder po = poService.createPurchaseOrder(vendorId, branchId, paymentTerms, notes, items);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Purchase Order created", po));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PURCHASER')")
    public ResponseEntity<ApiResponse<PageResponse<PurchaseOrder>>> getPOs(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) PurchaseOrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        return ResponseEntity.ok(ApiResponse.success(
                poService.getPurchaseOrders(search, status, page, size, sortBy, sortDir)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PURCHASER')")
    public ResponseEntity<ApiResponse<PurchaseOrder>> getPOById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(poService.getPurchaseOrderById(id)));
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PurchaseOrder>> approvePO(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("PO approved", poService.approvePO(id)));
    }

    @PatchMapping("/{id}/send")
    @PreAuthorize("hasAnyRole('ADMIN', 'PURCHASER')")
    public ResponseEntity<ApiResponse<PurchaseOrder>> sendToVendor(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("PO sent to vendor", poService.sendToVendor(id)));
    }

    @PostMapping("/{poId}/items/{poItemId}/receive")
    @PreAuthorize("hasAnyRole('ADMIN', 'PURCHASER', 'LOGISTICS')")
    public ResponseEntity<ApiResponse<GoodsReceipt>> receiveGoods(
            @PathVariable Long poId,
            @PathVariable Long poItemId,
            @RequestBody Map<String, Object> body) {
        int receivedQty = Integer.parseInt(body.get("receivedQty").toString());
        int rejectedQty = Integer.parseInt(body.getOrDefault("rejectedQty", "0").toString());
        String rejectionReason = body.getOrDefault("rejectionReason", "").toString();
        String invoiceNumber = body.getOrDefault("invoiceNumber", "").toString();
        String notes = body.getOrDefault("notes", "").toString();

        GoodsReceipt grn = poService.receiveGoods(poId, poItemId, receivedQty,
                rejectedQty, rejectionReason, invoiceNumber, notes);
        return ResponseEntity.ok(ApiResponse.success("Goods received", grn));
    }
}
