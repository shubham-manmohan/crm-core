/** Copyright © 2026 Mini2More. All Rights Reserved. Product: Mini2More CRM **/
package com.mini2more.crm.modules.quotation.service;
import com.mini2more.crm.modules.quotation.entity.Vendor;
import com.mini2more.crm.modules.quotation.repository.VendorRepository;

import com.mini2more.crm.common.dto.PageResponse;
import com.mini2more.crm.common.exception.DuplicateResourceException;
import com.mini2more.crm.common.exception.ResourceNotFoundException;
import com.mini2more.crm.config.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VendorService {

    private final VendorRepository vendorRepository;
    private final AuditService auditService;

    @Transactional
    public Vendor createVendor(Vendor vendor) {
        if (vendor.getGstNumber() != null && vendorRepository.existsByGstNumber(vendor.getGstNumber())) {
            throw new DuplicateResourceException("Vendor with GST " + vendor.getGstNumber() + " already exists");
        }
        vendor.setIsActive(true);
        Vendor saved = vendorRepository.save(vendor);
        auditService.log("CREATE", "VENDOR", saved.getId(), "Vendor created: " + saved.getName());
        return saved;
    }

    @Transactional
    public Vendor updateVendor(Long id, Vendor updated) {
        Vendor vendor = vendorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor", id));

        if (updated.getName() != null)
            vendor.setName(updated.getName());
        if (updated.getContactPerson() != null)
            vendor.setContactPerson(updated.getContactPerson());
        if (updated.getEmail() != null)
            vendor.setEmail(updated.getEmail());
        if (updated.getPhone() != null)
            vendor.setPhone(updated.getPhone());
        if (updated.getAlternatePhone() != null)
            vendor.setAlternatePhone(updated.getAlternatePhone());
        if (updated.getGstNumber() != null)
            vendor.setGstNumber(updated.getGstNumber());
        if (updated.getAddress() != null)
            vendor.setAddress(updated.getAddress());
        if (updated.getCity() != null)
            vendor.setCity(updated.getCity());
        if (updated.getState() != null)
            vendor.setState(updated.getState());
        if (updated.getPincode() != null)
            vendor.setPincode(updated.getPincode());
        if (updated.getPanNumber() != null)
            vendor.setPanNumber(updated.getPanNumber());
        if (updated.getBankName() != null)
            vendor.setBankName(updated.getBankName());
        if (updated.getBankAccount() != null)
            vendor.setBankAccount(updated.getBankAccount());
        if (updated.getBankIfsc() != null)
            vendor.setBankIfsc(updated.getBankIfsc());
        if (updated.getBrandsSupplied() != null)
            vendor.setBrandsSupplied(updated.getBrandsSupplied());
        if (updated.getProductCategories() != null)
            vendor.setProductCategories(updated.getProductCategories());
        if (updated.getPaymentTerms() != null)
            vendor.setPaymentTerms(updated.getPaymentTerms());
        if (updated.getLeadTimeDays() != null)
            vendor.setLeadTimeDays(updated.getLeadTimeDays());
        if (updated.getRating() != null)
            vendor.setRating(updated.getRating());
        if (updated.getNotes() != null)
            vendor.setNotes(updated.getNotes());
        if (updated.getIsActive() != null)
            vendor.setIsActive(updated.getIsActive());

        Vendor saved = vendorRepository.save(vendor);
        auditService.log("UPDATE", "VENDOR", saved.getId(), "Vendor updated: " + saved.getName());
        return saved;
    }

    public Vendor getVendorById(Long id) {
        return vendorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor", id));
    }

    public PageResponse<Vendor> getVendors(String search, int page, int size,
            String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Vendor> vendorPage = vendorRepository.searchVendors(search, pageable);

        return PageResponse.<Vendor>builder()
                .content(vendorPage.getContent())
                .page(vendorPage.getNumber())
                .size(vendorPage.getSize())
                .totalElements(vendorPage.getTotalElements())
                .totalPages(vendorPage.getTotalPages())
                .last(vendorPage.isLast())
                .first(vendorPage.isFirst())
                .build();
    }
}
