/** Copyright © 2026 Mini2More. All Rights Reserved. Product: Mini2More CRM **/
package com.mini2more.crm.modules.core.controller;
import com.mini2more.crm.modules.core.entity.Company;
import com.mini2more.crm.modules.core.service.CompanyService;

import com.mini2more.crm.common.dto.ApiResponse;
import com.mini2more.crm.common.dto.PageResponse;
import com.mini2more.crm.common.enums.CompanyType;
import com.mini2more.crm.modules.core.dto.CompanyDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES', 'PURCHASER', 'ACCOUNTANT', 'LOGISTICS')")
    public ResponseEntity<ApiResponse<PageResponse<CompanyDto>>> getAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) CompanyType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        PageResponse<CompanyDto> response = companyService.getAllCompanies(search, type, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success("Companies fetched successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES', 'PURCHASER', 'ACCOUNTANT', 'LOGISTICS')")
    public ResponseEntity<ApiResponse<CompanyDto>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Company fetched", companyService.getCompanyById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES', 'PURCHASER')")
    public ResponseEntity<ApiResponse<CompanyDto>> create(@RequestBody CompanyDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Company created", companyService.createCompany(dto)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES', 'PURCHASER')")
    public ResponseEntity<ApiResponse<CompanyDto>> update(@PathVariable Long id, @RequestBody CompanyDto dto) {
        return ResponseEntity.ok(ApiResponse.success("Company updated", companyService.updateCompany(id, dto)));
    }
}
