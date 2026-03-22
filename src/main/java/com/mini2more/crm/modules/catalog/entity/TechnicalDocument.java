/** Copyright © 2026 Mini2More. All Rights Reserved. Product: Mini2More CRM **/
package com.mini2more.crm.modules.catalog.entity;

import com.mini2more.crm.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "technical_documents")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class TechnicalDocument extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false) private String title;
    @Column(name = "document_type") private String documentType; // DATASHEET, MANUAL, CERTIFICATE, CATALOG
    @Column(name = "file_path", nullable = false) private String filePath;
    @Column(name = "file_size") private Long fileSize;
    @Column(name = "file_name") private String fileName;
    @Column(name = "product_code") private String productCode;
    @Column(name = "brand") private String brand;
    @Column(name = "category") private String category;
    @Column(name = "is_public") @Builder.Default private Boolean isPublic = true;
}
