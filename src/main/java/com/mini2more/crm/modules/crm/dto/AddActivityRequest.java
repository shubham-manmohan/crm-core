/** Copyright © 2026 Mini2More. All Rights Reserved. Product: Mini2More CRM **/
package com.mini2more.crm.modules.crm.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AddActivityRequest {
    @NotBlank(message = "Activity type is required")
    private String type;

    @NotBlank(message = "Description is required")
    private String description;

    private LocalDateTime scheduledAt;
}
