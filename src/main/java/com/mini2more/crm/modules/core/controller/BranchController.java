/** Copyright © 2026 Mini2More. All Rights Reserved. Product: Mini2More CRM **/
package com.mini2more.crm.modules.core.controller;
import com.mini2more.crm.modules.core.entity.Branch;
import com.mini2more.crm.modules.core.repository.BranchRepository;

import com.mini2more.crm.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/branches")
@RequiredArgsConstructor
public class BranchController {

    private final BranchRepository branchRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Branch>>> getAllBranches() {
        List<Branch> branches = branchRepository.findAll();
        return ResponseEntity.ok(ApiResponse.success(branches));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Branch>> getBranchById(@PathVariable Long id) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Branch not found"));
        return ResponseEntity.ok(ApiResponse.success(branch));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Branch>> createBranch(@RequestBody Branch branch) {
        Branch saved = branchRepository.save(branch);
        return ResponseEntity.ok(ApiResponse.success("Branch created", saved));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Branch>> updateBranch(@PathVariable Long id, @RequestBody Branch branch) {
        Branch existing = branchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Branch not found"));
        existing.setName(branch.getName());
        existing.setAddress(branch.getAddress());
        existing.setCity(branch.getCity());
        existing.setState(branch.getState());
        existing.setPincode(branch.getPincode());
        existing.setPhone(branch.getPhone());
        existing.setEmail(branch.getEmail());
        existing.setGstNumber(branch.getGstNumber());
        existing.setIsActive(branch.getIsActive());
        Branch saved = branchRepository.save(existing);
        return ResponseEntity.ok(ApiResponse.success("Branch updated", saved));
    }
}
