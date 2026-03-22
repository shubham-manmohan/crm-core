/** Copyright © 2026 Mini2More. All Rights Reserved. Product: Mini2More CRM **/
package com.mini2more.crm.modules.quotation.service;
import com.mini2more.crm.modules.quotation.repository.RfqRepository;
import com.mini2more.crm.modules.quotation.entity.Rfq;
import com.mini2more.crm.modules.quotation.repository.QuotationRepository;
import com.mini2more.crm.modules.quotation.entity.QuotationItem;
import com.mini2more.crm.modules.quotation.entity.Quotation;

import com.mini2more.crm.common.dto.PageResponse;
import com.mini2more.crm.common.enums.QuotationStatus;
import com.mini2more.crm.common.exception.BusinessException;
import com.mini2more.crm.common.exception.ResourceNotFoundException;
import com.mini2more.crm.config.AuditService;
import com.mini2more.crm.modules.crm.entity.LeadEntity;
import com.mini2more.crm.modules.crm.repository.LeadRepository;
import com.mini2more.crm.modules.quotation.dto.CreateQuotationRequest;
import com.mini2more.crm.modules.quotation.dto.QuotationItemDto;
import com.mini2more.crm.modules.inventory.entity.Product;
import com.mini2more.crm.modules.inventory.repository.ProductRepository;
import com.mini2more.crm.security.SecurityUtils;
import com.mini2more.crm.modules.core.entity.Branch;
import com.mini2more.crm.modules.core.repository.BranchRepository;
import com.mini2more.crm.modules.core.entity.UserEntity;
import com.mini2more.crm.modules.core.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuotationService {

    private final QuotationRepository quotationRepository;
    private final LeadRepository leadRepository;
    private final BranchRepository branchRepository;
    private final UserRepository userRepository;
    private final RfqRepository rfqRepository;
    private final ProductRepository productRepository;
    private final SecurityUtils securityUtils;
    private final AuditService auditService;

    // ─── CREATE ────────────────────────────────────────────────────

    @Transactional
    public Quotation createQuotation(CreateQuotationRequest request) {
        Quotation quotation = Quotation.builder()
                .quotationNumber(generateQuotationNumber())
                .version(1)
                .clientName(request.getClientName())
                .clientEmail(request.getClientEmail())
                .clientPhone(request.getClientPhone())
                .clientCompany(request.getClientCompany())
                .clientGst(request.getClientGst())
                .clientAddress(request.getClientAddress())
                .status(QuotationStatus.DRAFT)
                .paymentTerms(request.getPaymentTerms())
                .deliveryTerms(request.getDeliveryTerms())
                .validityDays(request.getValidityDays() != null ? request.getValidityDays() : 30)
                .warrantyTerms(request.getWarrantyTerms())
                .termsAndConditions(request.getTermsAndConditions())
                .notes(request.getNotes())
                .discountPercent(request.getDiscountPercent() != null
                        ? request.getDiscountPercent()
                        : BigDecimal.ZERO)
                .build();

        // Set lead
        if (request.getLeadId() != null) {
            LeadEntity lead = leadRepository.findById(request.getLeadId())
                    .orElseThrow(() -> new ResourceNotFoundException("Lead", request.getLeadId()));
            quotation.setLead(lead);
            // Auto-fill client info from lead if not provided
            if (quotation.getClientName() == null)
                quotation.setClientName(lead.getClientName());
            if (quotation.getClientEmail() == null)
                quotation.setClientEmail(lead.getClientEmail());
            if (quotation.getClientPhone() == null)
                quotation.setClientPhone(lead.getClientPhone());
            if (quotation.getClientCompany() == null)
                quotation.setClientCompany(lead.getClientCompany());
        }

        // Set RFQ
        if (request.getRfqId() != null) {
            Rfq rfq = rfqRepository.findById(request.getRfqId())
                    .orElseThrow(() -> new ResourceNotFoundException("RFQ", request.getRfqId()));
            quotation.setRfq(rfq);
        }

        // Set branch
        if (request.getBranchId() != null) {
            Branch branch = branchRepository.findById(request.getBranchId())
                    .orElseThrow(() -> new ResourceNotFoundException("Branch", request.getBranchId()));
            quotation.setBranch(branch);
        }

        // Set prepared by
        Long currentUserId = securityUtils.getCurrentUserId();
        if (currentUserId != null) {
            UserEntity preparer = userRepository.findById(currentUserId).orElse(null);
            quotation.setPreparedBy(preparer);
        }

        quotation.setCreatedBy(currentUserId);

        // Add items & calculate totals
        List<QuotationItem> items = new ArrayList<>();
        if (request.getItems() != null) {
            int order = 1;
            for (QuotationItemDto itemDto : request.getItems()) {
                QuotationItem item = mapItemFromDto(itemDto, quotation, order++);
                items.add(item);
            }
        }
        quotation.setItems(items);

        calculateTotals(quotation);
        quotation.setValidUntil(LocalDate.now().plusDays(quotation.getValidityDays()));

        quotation = quotationRepository.save(quotation);
        auditService.log("CREATE", "QUOTATION", quotation.getId(),
                "Quotation created: " + quotation.getQuotationNumber());

        return quotation;
    }

    // ─── REVISION (VERSIONING) ─────────────────────────────────────

    @Transactional
    public Quotation createRevision(Long quotationId, CreateQuotationRequest request) {
        Quotation original = quotationRepository.findById(quotationId)
                .orElseThrow(() -> new ResourceNotFoundException("Quotation", quotationId));

        // Find the latest version
        Long parentId = original.getParentQuotationId() != null
                ? original.getParentQuotationId()
                : original.getId();
        List<Quotation> versions = quotationRepository
                .findByParentQuotationIdOrderByVersionDesc(parentId);
        int nextVersion = versions.isEmpty() ? original.getVersion() + 1
                : versions.get(0).getVersion() + 1;

        // Mark current as revision requested
        if (original.getStatus() != QuotationStatus.REVISION_REQUESTED) {
            original.setStatus(QuotationStatus.REVISION_REQUESTED);
            original.setRevisionNotes(request.getNotes());
            quotationRepository.save(original);
        }

        // Create new version
        request.setLeadId(original.getLead() != null ? original.getLead().getId() : null);
        Quotation newVersion = createQuotation(request);
        newVersion.setVersion(nextVersion);
        newVersion.setParentQuotationId(parentId);
        newVersion.setQuotationNumber(original.getQuotationNumber().split("-R")[0]
                + "-R" + nextVersion);

        newVersion = quotationRepository.save(newVersion);
        auditService.log("REVISION", "QUOTATION", newVersion.getId(),
                "Revision v" + nextVersion + " of " + original.getQuotationNumber());

        return newVersion;
    }

    // ─── APPROVAL WORKFLOW ────────────────────────────────────────

    @Transactional
    public Quotation submitForApproval(Long id) {
        Quotation q = quotationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quotation", id));

        if (q.getStatus() != QuotationStatus.DRAFT) {
            throw new BusinessException("Only DRAFT quotations can be submitted for approval");
        }

        // Margin governance: if margin < 10%, require admin approval
        if (q.getMarginPercent().compareTo(new BigDecimal("10")) < 0) {
            log.warn("Low margin quotation {} ({}%). Requires admin approval.",
                    q.getQuotationNumber(), q.getMarginPercent());
        }

        q.setStatus(QuotationStatus.PENDING_APPROVAL);
        auditService.log("SUBMIT_APPROVAL", "QUOTATION", q.getId(),
                "Submitted for approval: " + q.getQuotationNumber());
        return quotationRepository.save(q);
    }

    @Transactional
    public Quotation approveQuotation(Long id) {
        Quotation q = quotationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quotation", id));

        if (q.getStatus() != QuotationStatus.PENDING_APPROVAL) {
            throw new BusinessException("Only PENDING_APPROVAL quotations can be approved");
        }

        Long currentUserId = securityUtils.getCurrentUserId();
        if (currentUserId != null) {
            UserEntity approver = userRepository.findById(currentUserId).orElse(null);
            q.setApprovedBy(approver);
        }

        q.setStatus(QuotationStatus.APPROVED);
        auditService.log("APPROVE", "QUOTATION", q.getId(),
                "Quotation approved: " + q.getQuotationNumber());
        return quotationRepository.save(q);
    }

    @Transactional
    public Quotation rejectQuotation(Long id, String reason) {
        Quotation q = quotationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quotation", id));

        if (q.getStatus() != QuotationStatus.PENDING_APPROVAL) {
            throw new BusinessException("Only PENDING_APPROVAL quotations can be rejected");
        }

        q.setStatus(QuotationStatus.REJECTED);
        q.setRejectionReason(reason);
        auditService.log("REJECT", "QUOTATION", q.getId(),
                "Quotation rejected: " + q.getQuotationNumber() + " - " + reason);
        return quotationRepository.save(q);
    }

    @Transactional
    public Quotation markAsSent(Long id) {
        Quotation q = quotationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quotation", id));

        if (q.getStatus() != QuotationStatus.APPROVED) {
            throw new BusinessException("Only APPROVED quotations can be sent to client");
        }

        q.setStatus(QuotationStatus.SENT_TO_CLIENT);
        auditService.log("SEND", "QUOTATION", q.getId(),
                "Quotation sent to client: " + q.getQuotationNumber());
        return quotationRepository.save(q);
    }

    @Transactional
    public Quotation markAccepted(Long id) {
        Quotation q = quotationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quotation", id));
        q.setStatus(QuotationStatus.ACCEPTED);
        auditService.log("ACCEPT", "QUOTATION", q.getId(),
                "Quotation accepted by client: " + q.getQuotationNumber());
        return quotationRepository.save(q);
    }

    // ─── READ ──────────────────────────────────────────────────────

    public Quotation getQuotationById(Long id) {
        return quotationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quotation", id));
    }

    public PageResponse<Quotation> getQuotations(String search, QuotationStatus status,
            int page, int size,
            String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Quotation> qPage = quotationRepository.searchQuotations(search, status, pageable);

        return PageResponse.<Quotation>builder()
                .content(qPage.getContent())
                .page(qPage.getNumber())
                .size(qPage.getSize())
                .totalElements(qPage.getTotalElements())
                .totalPages(qPage.getTotalPages())
                .last(qPage.isLast())
                .first(qPage.isFirst())
                .build();
    }

    public List<Quotation> getQuotationsByLead(Long leadId) {
        return quotationRepository.findByLeadIdOrderByVersionDesc(leadId);
    }

    // ─── CALCULATION ──────────────────────────────────────────────

    private void calculateTotals(Quotation quotation) {
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;
        BigDecimal totalGst = BigDecimal.ZERO;

        for (QuotationItem item : quotation.getItems()) {
            // Item total = (unitPrice * quantity) - discount
            BigDecimal lineTotal = item.getUnitPrice()
                    .multiply(BigDecimal.valueOf(item.getQuantity()));

            if (item.getDiscountPercent() != null
                    && item.getDiscountPercent().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal discount = lineTotal.multiply(item.getDiscountPercent())
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                lineTotal = lineTotal.subtract(discount);
            }

            item.setTotalPrice(lineTotal);

            // GST
            BigDecimal gstRate = item.getGstPercent() != null
                    ? item.getGstPercent()
                    : new BigDecimal("18");
            BigDecimal itemGst = lineTotal.multiply(gstRate)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            totalGst = totalGst.add(itemGst);

            // Margin per item
            BigDecimal itemCost = item.getCostPrice()
                    .multiply(BigDecimal.valueOf(item.getQuantity()));
            item.setMarginAmount(lineTotal.subtract(itemCost));
            if (lineTotal.compareTo(BigDecimal.ZERO) > 0) {
                item.setMarginPercent(item.getMarginAmount()
                        .multiply(BigDecimal.valueOf(100))
                        .divide(lineTotal, 2, RoundingMode.HALF_UP));
            }

            subtotal = subtotal.add(lineTotal);
            totalCost = totalCost.add(itemCost);
        }

        // Overall discount
        BigDecimal discountAmount = BigDecimal.ZERO;
        if (quotation.getDiscountPercent() != null
                && quotation.getDiscountPercent().compareTo(BigDecimal.ZERO) > 0) {
            discountAmount = subtotal.multiply(quotation.getDiscountPercent())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }

        BigDecimal totalAmount = subtotal.subtract(discountAmount).add(totalGst);

        quotation.setSubtotal(subtotal);
        quotation.setDiscountAmount(discountAmount);
        quotation.setGstAmount(totalGst);
        quotation.setTotalAmount(totalAmount);
        quotation.setTotalCostPrice(totalCost);

        // Overall margin
        BigDecimal marginAmount = subtotal.subtract(discountAmount).subtract(totalCost);
        quotation.setMarginAmount(marginAmount);
        if (subtotal.subtract(discountAmount).compareTo(BigDecimal.ZERO) > 0) {
            quotation.setMarginPercent(marginAmount.multiply(BigDecimal.valueOf(100))
                    .divide(subtotal.subtract(discountAmount), 2, RoundingMode.HALF_UP));
        }
    }

    // ─── HELPERS ──────────────────────────────────────────────────

    private QuotationItem mapItemFromDto(QuotationItemDto dto, Quotation quotation, int order) {
        Product product = null;
        if (dto.getProductId() != null) {
            product = productRepository.findById(dto.getProductId()).orElse(null);
        }

        return QuotationItem.builder()
                .quotation(quotation)
                .itemOrder(order)
                .product(product)
                .productName(product != null ? product.getName() : dto.getProductName())
                .productCode(product != null ? product.getProductCode() : dto.getProductCode())
                .brand(product != null ? product.getBrand() : dto.getBrand())
                .category(product != null ? product.getCategory() : dto.getCategory())
                .description(dto.getDescription())
                .unit(dto.getUnit())
                .quantity(dto.getQuantity() != null ? dto.getQuantity() : 1)
                .costPrice(dto.getCostPrice() != null ? dto.getCostPrice() : BigDecimal.ZERO)
                .unitPrice(dto.getUnitPrice() != null ? dto.getUnitPrice() : BigDecimal.ZERO)
                .discountPercent(dto.getDiscountPercent() != null
                        ? dto.getDiscountPercent()
                        : BigDecimal.ZERO)
                .gstPercent(dto.getGstPercent() != null
                        ? dto.getGstPercent()
                        : new BigDecimal("18.00"))
                .hsnCode(dto.getHsnCode())
                .deliveryDays(dto.getDeliveryDays())
                .vendorId(dto.getVendorId())
                .vendorName(dto.getVendorName())
                .build();
    }

    private String generateQuotationNumber() {
        Long maxId = quotationRepository.findMaxId();
        long nextId = (maxId != null ? maxId : 0) + 1;
        return String.format("QT-%06d", nextId);
    }
}
