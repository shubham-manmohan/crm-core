/** Copyright © 2026 Mini2More. All Rights Reserved. Product: Mini2More CRM **/
package com.mini2more.crm.modules.orders.controller;
import com.mini2more.crm.modules.orders.service.OrderService;
import com.mini2more.crm.modules.orders.entity.Dispatch;

import com.mini2more.crm.common.dto.ApiResponse;
import com.mini2more.crm.common.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dispatch")
@RequiredArgsConstructor
public class DispatchController {

    private final OrderService orderService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS')")
    public ResponseEntity<ApiResponse<PageResponse<Dispatch>>> getDispatches(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        return ResponseEntity.ok(ApiResponse.success(
                orderService.getDispatches(search, page, size, sortBy, sortDir)));
    }
}
