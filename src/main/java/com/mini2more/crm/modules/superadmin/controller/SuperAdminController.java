package com.mini2more.crm.modules.superadmin.controller;

import com.mini2more.crm.modules.core.entity.Company;
import com.mini2more.crm.modules.superadmin.service.SuperAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/superadmin")
@RequiredArgsConstructor
public class SuperAdminController {

    private final SuperAdminService superAdminService;

    @GetMapping("/companies")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')") // Enforcing explicit RBAC permission matching the JwtProvider upgrade
    public ResponseEntity<List<Company>> getAllCompanies() {
        return ResponseEntity.ok(superAdminService.getAllCompaniesGodView());
    }
}
