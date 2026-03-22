/** Copyright © 2026 Mini2More. All Rights Reserved. Product: Mini2More CRM **/
package com.mini2more.crm.modules.core.dto;

import com.mini2more.crm.common.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String email;
    private String fullName;
    private String companyName;
    private String gstNumber;
    private String phone;
    private UserRole role;
    private Long branchId;
    private String branchName;
    private Boolean isActive;
    private String address;
    private String city;
    private String state;
    private String pincode;
    private String profileImage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
