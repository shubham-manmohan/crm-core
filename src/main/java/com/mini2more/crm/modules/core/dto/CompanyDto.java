/** Copyright © 2026 Mini2More. All Rights Reserved. Product: Mini2More CRM **/
package com.mini2more.crm.modules.core.dto;

import com.mini2more.crm.common.enums.CompanyType;

public record CompanyDto(
        Long id,
        String name,
        CompanyType companyType,
        String email,
        String phone,
        String gstNumber,
        String address,
        String city,
        String state,
        String pincode,
        String website,
        String notes
) {
}
