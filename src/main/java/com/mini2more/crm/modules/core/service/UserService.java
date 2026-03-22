/** Copyright © 2026 Mini2More. All Rights Reserved. Product: Mini2More CRM **/
package com.mini2more.crm.modules.core.service;
import com.mini2more.crm.modules.core.entity.Company;
import com.mini2more.crm.modules.core.entity.Branch;
import com.mini2more.crm.modules.core.repository.UserRepository;
import com.mini2more.crm.modules.core.entity.UserEntity;
import com.mini2more.crm.modules.core.repository.CompanyRepository;
import com.mini2more.crm.modules.core.repository.BranchRepository;

import com.mini2more.crm.common.dto.PageResponse;
import com.mini2more.crm.common.enums.UserRole;
import com.mini2more.crm.common.exception.BusinessException;
import com.mini2more.crm.common.exception.DuplicateResourceException;
import com.mini2more.crm.common.exception.ResourceNotFoundException;
import com.mini2more.crm.config.AuditService;
import com.mini2more.crm.security.JwtProvider;
import com.mini2more.crm.modules.core.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final BranchRepository branchRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final AuditService auditService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered: " + request.getEmail());
        }
        if (request.getGstNumber() != null && userRepository.existsByGstNumber(request.getGstNumber())) {
            throw new DuplicateResourceException("GST number already registered: " + request.getGstNumber());
        }

        Company company = null;
        if (request.getCompanyName() != null && !request.getCompanyName().isBlank()) {
            company = companyRepository.findByNameIgnoreCase(request.getCompanyName()).orElse(null);
            if (company == null) {
                company = Company.builder()
                    .name(request.getCompanyName())
                    .companyType(com.mini2more.crm.common.enums.CompanyType.CLIENT)
                    .build();
                company = companyRepository.save(company);
            }
        }

        UserEntity user = UserEntity.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .company(company)
                .gstNumber(request.getGstNumber())
                .phone(request.getPhone())
                .role(UserRole.CLIENT)
                .isActive(true)
                .address(request.getAddress())
                .city(request.getCity())
                .state(request.getState())
                .pincode(request.getPincode())
                .build();

        user = userRepository.save(user);
        auditService.log("REGISTER", "USER", user.getId(), "Client self-registration");

        return buildAuthResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        UserEntity user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException("Invalid email or password");
        }

        if (!user.getIsActive()) {
            throw new BusinessException("Account is deactivated. Please contact admin.");
        }

        auditService.log("LOGIN", "USER", user.getId(), "User login");
        return buildAuthResponse(user);
    }

    public AuthResponse refreshToken(RefreshTokenRequest request) {
        if (!jwtProvider.validateToken(request.getRefreshToken())) {
            throw new BusinessException("Invalid or expired refresh token");
        }

        String email = jwtProvider.getEmailFromToken(request.getRefreshToken());
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        return buildAuthResponse(user);
    }

    public UserResponse getCurrentUser(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        return mapToResponse(user);
    }

    public PageResponse<UserResponse> getAllUsers(String search, UserRole role, Boolean isActive,
            int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<UserEntity> userPage = userRepository.findAllWithFilters(search, role, isActive, pageable);

        return PageResponse.<UserResponse>builder()
                .content(userPage.getContent().stream().map(this::mapToResponse).toList())
                .page(userPage.getNumber())
                .size(userPage.getSize())
                .totalElements(userPage.getTotalElements())
                .totalPages(userPage.getTotalPages())
                .last(userPage.isLast())
                .first(userPage.isFirst())
                .build();
    }

    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        if (request.getFullName() != null)
            user.setFullName(request.getFullName());
        // Handling company assignment will eventually require a companyId in the request, 
        // string companyName here is ignored temporarily until API is upgraded.
        if (request.getGstNumber() != null)
            user.setGstNumber(request.getGstNumber());
        if (request.getPhone() != null)
            user.setPhone(request.getPhone());
        if (request.getAddress() != null)
            user.setAddress(request.getAddress());
        if (request.getCity() != null)
            user.setCity(request.getCity());
        if (request.getState() != null)
            user.setState(request.getState());
        if (request.getPincode() != null)
            user.setPincode(request.getPincode());
        if (request.getRole() != null)
            user.setRole(request.getRole());
        if (request.getIsActive() != null)
            user.setIsActive(request.getIsActive());
        if (request.getBranchId() != null) {
            Branch branch = branchRepository.findById(request.getBranchId())
                    .orElseThrow(() -> new ResourceNotFoundException("Branch", request.getBranchId()));
            user.setBranch(branch);
        }

        user = userRepository.save(user);
        auditService.log("UPDATE", "USER", user.getId(), "User profile updated");
        return mapToResponse(user);
    }

    @Transactional
    public UserResponse createInternalUser(RegisterRequest request, UserRole role) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered: " + request.getEmail());
        }

        Company company = null;
        if (request.getCompanyName() != null && !request.getCompanyName().isBlank()) {
            company = companyRepository.findByNameIgnoreCase(request.getCompanyName()).orElse(null);
            if (company == null) {
                company = Company.builder()
                    .name(request.getCompanyName())
                    .companyType(role == UserRole.CLIENT ? com.mini2more.crm.common.enums.CompanyType.CLIENT : com.mini2more.crm.common.enums.CompanyType.BOTH)
                    .build();
                company = companyRepository.save(company);
            }
        }

        UserEntity user = UserEntity.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .company(company)
                .gstNumber(request.getGstNumber())
                .phone(request.getPhone())
                .role(role)
                .isActive(true)
                .address(request.getAddress())
                .city(request.getCity())
                .state(request.getState())
                .pincode(request.getPincode())
                .build();

        user = userRepository.save(user);
        auditService.log("CREATE_USER", "USER", user.getId(), "Internal user created with role: " + role);
        return mapToResponse(user);
    }

    private AuthResponse buildAuthResponse(UserEntity user) {
        String accessToken = jwtProvider.generateAccessToken(user.getId(), user.getEmail(), user.getRole().name());
        String refreshToken = jwtProvider.generateRefreshToken(user.getEmail());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .user(mapToResponse(user))
                .build();
    }

    private UserResponse mapToResponse(UserEntity user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .companyName(user.getCompany() != null ? user.getCompany().getName() : null)
                .gstNumber(user.getGstNumber())
                .phone(user.getPhone())
                .role(user.getRole())
                .branchId(user.getBranch() != null ? user.getBranch().getId() : null)
                .branchName(user.getBranch() != null ? user.getBranch().getName() : null)
                .isActive(user.getIsActive())
                .address(user.getAddress())
                .city(user.getCity())
                .state(user.getState())
                .pincode(user.getPincode())
                .profileImage(user.getProfileImage())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
