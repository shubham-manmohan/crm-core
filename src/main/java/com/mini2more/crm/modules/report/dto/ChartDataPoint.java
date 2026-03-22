/** Copyright © 2026 Mini2More. All Rights Reserved. Product: Mini2More CRM **/
package com.mini2more.crm.modules.report.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChartDataPoint {
    private String label;      // e.g. "Siemens", "ABB", "Jan", "Feb"
    private Number value;      // Value (could be amount or count)
    private String color;      // Optional color code for frontend
}
