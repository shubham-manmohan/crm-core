/** Copyright © 2026 Mini2More. All Rights Reserved. Product: Mini2More CRM **/
package com.mini2more.crm.modules.catalog.controller;
import com.mini2more.crm.modules.catalog.entity.ProductCategory;
import com.mini2more.crm.modules.catalog.entity.TechnicalDocument;
import com.mini2more.crm.modules.catalog.service.CatalogService;
import com.mini2more.crm.modules.catalog.entity.Enquiry;

import com.mini2more.crm.common.dto.ApiResponse;
import com.mini2more.crm.common.dto.PageResponse;
import com.mini2more.crm.common.enums.EnquiryStatus;
import com.mini2more.crm.modules.inventory.entity.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Public API for customer-facing catalog and enquiry submission.
 * No authentication required for browsing; enquiry may optionally include customer ID.
 */
@RestController
@RequestMapping("/api/public/catalog")
@RequiredArgsConstructor
public class PublicCatalogController {

    private final CatalogService catalogService;

    @GetMapping("/products")
    public ResponseEntity<ApiResponse<PageResponse<Product>>> browseProducts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        return ResponseEntity.ok(ApiResponse.success(
                catalogService.browseProducts(search, brand, category, page, size, sortBy, sortDir)));
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<ApiResponse<Product>> getProductDetail(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(catalogService.getProductDetail(id)));
    }

    @PostMapping("/products/stock-urgency")
    public ResponseEntity<ApiResponse<Map<Long, String>>> getStockUrgency(@RequestBody List<Long> productIds) {
        return ResponseEntity.ok(ApiResponse.success(catalogService.getStockUrgencyForProducts(productIds)));
    }

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<ProductCategory>>> getRootCategories() {
        return ResponseEntity.ok(ApiResponse.success(catalogService.getRootCategories()));
    }

    @GetMapping("/categories/{parentId}/children")
    public ResponseEntity<ApiResponse<List<ProductCategory>>> getSubCategories(@PathVariable Long parentId) {
        return ResponseEntity.ok(ApiResponse.success(catalogService.getSubCategories(parentId)));
    }

    @GetMapping("/documents")
    public ResponseEntity<ApiResponse<PageResponse<TechnicalDocument>>> browseDocs(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(catalogService.browseDocs(search, page, size)));
    }

    @PostMapping("/enquiry")
    public ResponseEntity<ApiResponse<Enquiry>> submitEnquiry(
            @RequestBody Map<String, Object> payload,
            @RequestParam(required = false) Long customerId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Enquiry submitted", catalogService.submitEnquiry(payload, customerId)));
    }
}
