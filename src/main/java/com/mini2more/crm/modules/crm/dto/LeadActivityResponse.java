/** Copyright © 2026 Mini2More. All Rights Reserved. Product: Mini2More CRM **/
package com.mini2more.crm.modules.crm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadActivityResponse {
    private Long id;
    private String type;
    private String description;
    private String oldValue;
    private String newValue;
    private Long userId;
    private String userName;
    private LocalDateTime scheduledAt;
    private LocalDateTime createdAt;
}
