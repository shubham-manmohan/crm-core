/** Copyright © 2026 Mini2More. All Rights Reserved. Product: Mini2More CRM **/
package com.mini2more.crm.modules.master.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TenantMasterRequest {
    @NotBlank(message = "Type is required")
    private String type;

    @NotBlank(message = "Code is required")
    private String code;

    @NotBlank(message = "Name is required")
    private String name;

    private String description;
    private String parentCode;
    private Integer displayOrder;
    private Boolean isActive;
    private String metadata;
}
