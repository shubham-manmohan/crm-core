/** Copyright © 2026 Mini2More. All Rights Reserved. Product: Mini2More CRM **/
package com.mini2more.crm.modules.core.service;
import com.mini2more.crm.modules.core.entity.Company;
import com.mini2more.crm.modules.core.repository.CompanyRepository;

import com.mini2more.crm.common.dto.PageResponse;
import com.mini2more.crm.common.enums.CompanyType;
import com.mini2more.crm.common.exception.BusinessException;
import com.mini2more.crm.common.exception.ResourceNotFoundException;
import com.mini2more.crm.config.AuditService;
import com.mini2more.crm.modules.core.dto.CompanyDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final AuditService auditService;

    public PageResponse<CompanyDto> getAllCompanies(String search, CompanyType type, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Company> companyPage = companyRepository.search(search, type, pageable);
        
        return PageResponse.<CompanyDto>builder()
                .content(companyPage.getContent().stream().map(this::mapToDto).toList())
                .page(companyPage.getNumber())
                .size(companyPage.getSize())
                .totalElements(companyPage.getTotalElements())
                .totalPages(companyPage.getTotalPages())
                .last(companyPage.isLast())
                .first(companyPage.isFirst())
                .build();
    }

    public CompanyDto getCompanyById(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company", id));
        return mapToDto(company);
    }

    @Transactional
    public CompanyDto createCompany(CompanyDto dto) {
        if (companyRepository.existsByNameIgnoreCase(dto.name())) {
            throw new BusinessException("Company with name '" + dto.name() + "' already exists");
        }
        
        Company company = Company.builder()
                .name(dto.name())
                .companyType(dto.companyType())
                .email(dto.email())
                .phone(dto.phone())
                .gstNumber(dto.gstNumber())
                .address(dto.address())
                .city(dto.city())
                .state(dto.state())
                .pincode(dto.pincode())
                .website(dto.website())
                .notes(dto.notes())
                .build();
                
        company = companyRepository.save(company);
        auditService.log("CREATE", "COMPANY", company.getId(), "Created " + company.getCompanyType() + " company");
        return mapToDto(company);
    }

    @Transactional
    public CompanyDto updateCompany(Long id, CompanyDto dto) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company", id));
                
        if (!company.getName().equalsIgnoreCase(dto.name()) && companyRepository.existsByNameIgnoreCase(dto.name())) {
            throw new BusinessException("Company with name '" + dto.name() + "' already exists");
        }
        
        company.setName(dto.name());
        company.setCompanyType(dto.companyType());
        company.setEmail(dto.email());
        company.setPhone(dto.phone());
        company.setGstNumber(dto.gstNumber());
        company.setAddress(dto.address());
        company.setCity(dto.city());
        company.setState(dto.state());
        company.setPincode(dto.pincode());
        company.setWebsite(dto.website());
        company.setNotes(dto.notes());
        
        company = companyRepository.save(company);
        auditService.log("UPDATE", "COMPANY", company.getId(), "Updated company details");
        return mapToDto(company);
    }

    private CompanyDto mapToDto(Company c) {
        return new CompanyDto(
                c.getId(), c.getName(), c.getCompanyType(), c.getEmail(), c.getPhone(),
                c.getGstNumber(), c.getAddress(), c.getCity(), c.getState(), c.getPincode(),
                c.getWebsite(), c.getNotes()
        );
    }
}
