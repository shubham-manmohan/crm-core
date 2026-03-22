/** Copyright © 2026 Mini2More. All Rights Reserved. Product: Mini2More CRM **/
package com.mini2more.crm.modules.inventory.service;
import com.mini2more.crm.modules.inventory.repository.GoodsReceiptRepository;
import com.mini2more.crm.modules.inventory.repository.ProductRepository;
import com.mini2more.crm.modules.inventory.entity.GoodsReceipt;
import com.mini2more.crm.modules.inventory.repository.PurchaseOrderRepository;
import com.mini2more.crm.modules.inventory.entity.PurchaseOrderItem;
import com.mini2more.crm.modules.inventory.entity.PurchaseOrder;

import com.mini2more.crm.common.dto.PageResponse;
import com.mini2more.crm.common.enums.PurchaseOrderStatus;
import com.mini2more.crm.common.exception.BusinessException;
import com.mini2more.crm.common.exception.ResourceNotFoundException;
import com.mini2more.crm.config.AuditService;
import com.mini2more.crm.modules.quotation.entity.Vendor;
import com.mini2more.crm.modules.quotation.repository.VendorRepository;
import com.mini2more.crm.modules.core.entity.Branch;
import com.mini2more.crm.modules.core.repository.BranchRepository;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PurchaseOrderService {

    private final PurchaseOrderRepository poRepository;
    private final GoodsReceiptRepository grnRepository;
    private final VendorRepository vendorRepository;
    private final BranchRepository branchRepository;
    private final ProductRepository productRepository;
    private final InventoryService inventoryService;
    private final AuditService auditService;

    // ─── CREATE PO ────────────────────────────────────────────────

    @Transactional
    public PurchaseOrder createPurchaseOrder(Long vendorId, Long branchId,
            String paymentTerms, String notes,
            List<PurchaseOrderItem> items) {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor", vendorId));

        Branch branch = branchId != null
                ? branchRepository.findById(branchId)
                        .orElseThrow(() -> new ResourceNotFoundException("Branch", branchId))
                : null;

        Long maxId = poRepository.findMaxId();
        long nextId = (maxId != null ? maxId : 0) + 1;

        PurchaseOrder po = PurchaseOrder.builder()
                .poNumber(String.format("PO-%06d", nextId))
                .vendor(vendor)
                .branch(branch)
                .status(PurchaseOrderStatus.DRAFT)
                .paymentTerms(paymentTerms)
                .notes(notes)
                .items(new ArrayList<>())
                .build();

        // Calculate totals
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal totalGst = BigDecimal.ZERO;

        for (PurchaseOrderItem item : items) {
            item.setPurchaseOrder(po);
            if (item.getReceivedQty() == null)
                item.setReceivedQty(0);

            BigDecimal lineTotal = item.getUnitPrice()
                    .multiply(BigDecimal.valueOf(item.getOrderedQty()));
            item.setTotalPrice(lineTotal);

            BigDecimal gst = lineTotal.multiply(item.getGstPercent())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            totalGst = totalGst.add(gst);
            totalAmount = totalAmount.add(lineTotal);

            po.getItems().add(item);
        }

        po.setTotalAmount(totalAmount);
        po.setGstAmount(totalGst);
        po.setGrandTotal(totalAmount.add(totalGst));

        PurchaseOrder saved = poRepository.save(po);
        auditService.log("CREATE", "PURCHASE_ORDER", saved.getId(),
                "PO created: " + saved.getPoNumber());
        return saved;
    }

    // ─── LIFECYCLE ────────────────────────────────────────────────

    @Transactional
    public PurchaseOrder approvePO(Long id) {
        PurchaseOrder po = poRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseOrder", id));
        if (po.getStatus() != PurchaseOrderStatus.DRAFT) {
            throw new BusinessException("Only DRAFT POs can be approved");
        }
        po.setStatus(PurchaseOrderStatus.APPROVED);
        auditService.log("APPROVE", "PURCHASE_ORDER", po.getId(), "PO approved");
        return poRepository.save(po);
    }

    @Transactional
    public PurchaseOrder sendToVendor(Long id) {
        PurchaseOrder po = poRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseOrder", id));
        if (po.getStatus() != PurchaseOrderStatus.APPROVED) {
            throw new BusinessException("Only APPROVED POs can be sent");
        }
        po.setStatus(PurchaseOrderStatus.SENT_TO_VENDOR);
        auditService.log("SEND", "PURCHASE_ORDER", po.getId(), "PO sent to vendor");
        return poRepository.save(po);
    }

    // ─── GOODS RECEIPT ────────────────────────────────────────────

    @Transactional
    public GoodsReceipt receiveGoods(Long poId, Long poItemId,
            int receivedQty, int rejectedQty,
            String rejectionReason, String invoiceNumber,
            String notes) {
        PurchaseOrder po = poRepository.findById(poId)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseOrder", poId));

        PurchaseOrderItem poItem = po.getItems().stream()
                .filter(i -> i.getId().equals(poItemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("PO Item", poItemId));

        int acceptedQty = receivedQty - rejectedQty;
        if (acceptedQty < 0)
            throw new BusinessException("Rejected qty cannot exceed received qty");

        int totalReceived = poItem.getReceivedQty() + acceptedQty;
        if (totalReceived > poItem.getOrderedQty()) {
            throw new BusinessException("Total received (" + totalReceived
                    + ") exceeds ordered quantity (" + poItem.getOrderedQty() + ")");
        }

        // Update PO item
        poItem.setReceivedQty(totalReceived);

        // Generate GRN number
        Long maxGrnId = grnRepository.findMaxId();
        long nextGrnId = (maxGrnId != null ? maxGrnId : 0) + 1;

        GoodsReceipt grn = GoodsReceipt.builder()
                .grnNumber(String.format("GRN-%06d", nextGrnId))
                .purchaseOrder(po)
                .poItem(poItem)
                .product(poItem.getProduct())
                .receivedQty(receivedQty)
                .acceptedQty(acceptedQty)
                .rejectedQty(rejectedQty)
                .rejectionReason(rejectionReason)
                .invoiceNumber(invoiceNumber)
                .notes(notes)
                .receivedAt(LocalDateTime.now())
                .build();

        GoodsReceipt saved = grnRepository.save(grn);

        // Update inventory: add accepted qty to branch stock
        if (acceptedQty > 0 && po.getBranch() != null && poItem.getProduct() != null) {
            inventoryService.adjustStock(poItem.getProduct().getId(),
                    po.getBranch().getId(), acceptedQty,
                    "Goods Receipt: " + saved.getGrnNumber());
        }

        // Update PO status
        boolean allFullyReceived = po.getItems().stream()
                .allMatch(i -> i.getReceivedQty() >= i.getOrderedQty());
        boolean anyReceived = po.getItems().stream()
                .anyMatch(i -> i.getReceivedQty() > 0);

        if (allFullyReceived) {
            po.setStatus(PurchaseOrderStatus.FULLY_RECEIVED);
        } else if (anyReceived) {
            po.setStatus(PurchaseOrderStatus.PARTIALLY_RECEIVED);
        }
        poRepository.save(po);

        auditService.log("GOODS_RECEIPT", "PURCHASE_ORDER", po.getId(),
                "GRN " + saved.getGrnNumber() + ": received " + acceptedQty
                        + " units of " + poItem.getProductName());

        return saved;
    }

    // ─── READ ─────────────────────────────────────────────────────

    public PurchaseOrder getPurchaseOrderById(Long id) {
        return poRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseOrder", id));
    }

    public PageResponse<PurchaseOrder> getPurchaseOrders(String search,
            PurchaseOrderStatus status,
            int page, int size,
            String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<PurchaseOrder> poPage = poRepository.search(search, status, pageable);

        return PageResponse.<PurchaseOrder>builder()
                .content(poPage.getContent())
                .page(poPage.getNumber())
                .size(poPage.getSize())
                .totalElements(poPage.getTotalElements())
                .totalPages(poPage.getTotalPages())
                .last(poPage.isLast())
                .first(poPage.isFirst())
                .build();
    }
}
