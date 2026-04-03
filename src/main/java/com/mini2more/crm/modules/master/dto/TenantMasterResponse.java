/** Copyright © 2026 Mini2More. All Rights Reserved. Product: Mini2More CRM **/
package com.mini2more.crm.modules.master.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantMasterResponse {
    private Long id;
    private String type;
    private String code;
    private String name;
    private String description;
    private String parentCode;
    private Integer displayOrder;
    private Boolean isActive;
    private String metadata;
}
