/** Copyright © 2026 Mini2More. All Rights Reserved. Product: Mini2More CRM **/
package com.mini2more.crm.modules.catalog.service;
import com.mini2more.crm.modules.catalog.entity.EnquiryItem;
import com.mini2more.crm.modules.catalog.repository.ProductCategoryRepository;
import com.mini2more.crm.modules.catalog.entity.ProductCategory;
import com.mini2more.crm.modules.catalog.entity.TechnicalDocument;
import com.mini2more.crm.modules.catalog.repository.EnquiryRepository;
import com.mini2more.crm.modules.catalog.repository.TechnicalDocumentRepository;
import com.mini2more.crm.modules.catalog.entity.Enquiry;

import com.mini2more.crm.common.dto.PageResponse;
import com.mini2more.crm.common.enums.EnquiryStatus;
import com.mini2more.crm.common.exception.ResourceNotFoundException;
import com.mini2more.crm.config.AuditService;
import com.mini2more.crm.modules.inventory.repository.InventoryRepository;
import com.mini2more.crm.modules.inventory.entity.Product;
import com.mini2more.crm.modules.inventory.repository.ProductRepository;
import com.mini2more.crm.modules.core.entity.UserEntity;
import com.mini2more.crm.modules.core.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class CatalogService {

    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final ProductCategoryRepository categoryRepository;
    private final TechnicalDocumentRepository documentRepository;
    private final EnquiryRepository enquiryRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    // ─── PUBLIC CATALOG ───────────────────────────────────────────

    public PageResponse<Product> browseProducts(String search, String brand, String category,
                                                  int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Page<Product> p = productRepository.searchProducts(search, brand, category, PageRequest.of(page, size, sort));
        return PageResponse.<Product>builder()
                .content(p.getContent()).page(p.getNumber()).size(p.getSize())
                .totalElements(p.getTotalElements()).totalPages(p.getTotalPages())
                .last(p.isLast()).first(p.isFirst()).build();
    }

    public Product getProductDetail(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
    }

    /**
     * Returns a map of productId → stock urgency indicator:
     * "IN_STOCK", "LOW_STOCK", "OUT_OF_STOCK"
     */
    public Map<Long, String> getStockUrgencyForProducts(List<Long> productIds) {
        Map<Long, String> result = new HashMap<>();
        for (Long productId : productIds) {
            var items = inventoryRepository.findByProductId(productId);
            int totalAvailable = items.stream().mapToInt(i -> i.getAvailableQty() != null ? i.getAvailableQty() : 0).sum();
            Product product = productRepository.findById(productId).orElse(null);
            int reorderLevel = (product != null && product.getReorderLevel() != null) ? product.getReorderLevel() : 10;

            if (totalAvailable <= 0) result.put(productId, "OUT_OF_STOCK");
            else if (totalAvailable <= reorderLevel) result.put(productId, "LOW_STOCK");
            else result.put(productId, "IN_STOCK");
        }
        return result;
    }

    // ─── CATEGORIES ───────────────────────────────────────────────

    public List<ProductCategory> getRootCategories() {
        return categoryRepository.findByParentIsNullAndIsActiveTrueOrderByDisplayOrder();
    }

    public List<ProductCategory> getSubCategories(Long parentId) {
        return categoryRepository.findByParentIdAndIsActiveTrue(parentId);
    }

    // ─── TECHNICAL DOCS ───────────────────────────────────────────

    public PageResponse<TechnicalDocument> browseDocs(String search, int page, int size) {
        Page<TechnicalDocument> p = documentRepository.searchPublic(search, PageRequest.of(page, size, Sort.by("title")));
        return PageResponse.<TechnicalDocument>builder()
                .content(p.getContent()).page(p.getNumber()).size(p.getSize())
                .totalElements(p.getTotalElements()).totalPages(p.getTotalPages())
                .last(p.isLast()).first(p.isFirst()).build();
    }

    // ─── ENQUIRY SUBMISSION ───────────────────────────────────────

    @Transactional
    @SuppressWarnings("unchecked")
    public Enquiry submitEnquiry(Map<String, Object> payload, Long customerId) {
        Long maxId = enquiryRepository.findMaxId();
        long nextId = (maxId != null ? maxId : 0) + 1;

        Enquiry.EnquiryBuilder builder = Enquiry.builder()
                .enquiryNumber(String.format("ENQ-%06d", nextId))
                .status(EnquiryStatus.SUBMITTED)
                .contactName(str(payload, "contactName"))
                .contactEmail(str(payload, "contactEmail"))
                .contactPhone(str(payload, "contactPhone"))
                .companyName(str(payload, "companyName"))
                .enquiryType(str(payload, "enquiryType") != null ? str(payload, "enquiryType") : "STANDARD")
                .projectName(str(payload, "projectName"))
                .projectDescription(str(payload, "projectDescription"))
                .requirements(str(payload, "requirements"))
                .urgencyLevel(str(payload, "urgencyLevel") != null ? str(payload, "urgencyLevel") : "NORMAL")
                .boqFilePath(str(payload, "boqFilePath"))
                .items(new ArrayList<>());

        if (customerId != null) {
            UserEntity customer = userRepository.findById(customerId).orElse(null);
            builder.customer(customer);
        }

        Enquiry enquiry = builder.build();

        List<Map<String, Object>> itemsData = (List<Map<String, Object>>) payload.get("items");
        if (itemsData != null) {
            for (Map<String, Object> itemData : itemsData) {
                EnquiryItem.EnquiryItemBuilder ib = EnquiryItem.builder()
                        .enquiry(enquiry)
                        .productName(str(itemData, "productName"))
                        .productCode(str(itemData, "productCode"))
                        .brand(str(itemData, "brand"))
                        .quantity(itemData.get("quantity") != null ? Integer.parseInt(itemData.get("quantity").toString()) : 1)
                        .unit(itemData.getOrDefault("unit", "Nos").toString())
                        .specifications(str(itemData, "specifications"));

                if (itemData.get("productId") != null) {
                    Long productId = Long.valueOf(itemData.get("productId").toString());
                    Product product = productRepository.findById(productId).orElse(null);
                    ib.product(product);
                }

                enquiry.getItems().add(ib.build());
            }
        }

        Enquiry saved = enquiryRepository.save(enquiry);
        auditService.log("SUBMIT", "ENQUIRY", saved.getId(), "Enquiry submitted: " + saved.getEnquiryNumber());
        return saved;
    }

    // ─── ENQUIRY MANAGEMENT ───────────────────────────────────────

    public PageResponse<Enquiry> getEnquiries(String search, EnquiryStatus status,
                                                int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Page<Enquiry> p = enquiryRepository.search(search, status, PageRequest.of(page, size, sort));
        return PageResponse.<Enquiry>builder()
                .content(p.getContent()).page(p.getNumber()).size(p.getSize())
                .totalElements(p.getTotalElements()).totalPages(p.getTotalPages())
                .last(p.isLast()).first(p.isFirst()).build();
    }

    public Enquiry getEnquiryById(Long id) {
        return enquiryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Enquiry", id));
    }

    @Transactional
    public Enquiry updateEnquiryStatus(Long id, EnquiryStatus status) {
        Enquiry enquiry = getEnquiryById(id);
        enquiry.setStatus(status);
        auditService.log("STATUS", "ENQUIRY", id, "Status → " + status);
        return enquiryRepository.save(enquiry);
    }

    private String str(Map<String, Object> map, String key) {
        Object v = map.get(key);
        return v != null ? v.toString() : null;
    }
}
