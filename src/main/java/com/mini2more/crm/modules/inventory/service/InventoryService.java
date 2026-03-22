/** Copyright © 2026 Mini2More. All Rights Reserved. Product: Mini2More CRM **/
package com.mini2more.crm.modules.inventory.service;
import com.mini2more.crm.modules.inventory.entity.StockTransfer;
import com.mini2more.crm.modules.inventory.repository.ProductRepository;
import com.mini2more.crm.modules.inventory.repository.StockTransferRepository;
import com.mini2more.crm.modules.inventory.entity.Product;
import com.mini2more.crm.modules.inventory.repository.InventoryRepository;
import com.mini2more.crm.modules.inventory.entity.InventoryItem;

import com.mini2more.crm.common.dto.PageResponse;
import com.mini2more.crm.common.enums.TransferStatus;
import com.mini2more.crm.common.exception.BusinessException;
import com.mini2more.crm.common.exception.DuplicateResourceException;
import com.mini2more.crm.common.exception.ResourceNotFoundException;
import com.mini2more.crm.config.AuditService;
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

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final StockTransferRepository transferRepository;
    private final BranchRepository branchRepository;
    private final UserRepository userRepository;
    private final SecurityUtils securityUtils;
    private final AuditService auditService;

    // ─── PRODUCT CRUD ─────────────────────────────────────────────

    @Transactional
    public Product createProduct(Product product) {
        if (productRepository.existsByProductCode(product.getProductCode())) {
            throw new DuplicateResourceException("Product code already exists: " + product.getProductCode());
        }
        product.setIsActive(true);
        Product saved = productRepository.save(product);
        auditService.log("CREATE", "PRODUCT", saved.getId(), "Product created: " + saved.getProductCode());
        return saved;
    }

    @Transactional
    public Product updateProduct(Long id, Product updated) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));

        if (updated.getName() != null)
            product.setName(updated.getName());
        if (updated.getDescription() != null)
            product.setDescription(updated.getDescription());
        if (updated.getBrand() != null)
            product.setBrand(updated.getBrand());
        if (updated.getCategory() != null)
            product.setCategory(updated.getCategory());
        if (updated.getSubCategory() != null)
            product.setSubCategory(updated.getSubCategory());
        if (updated.getUnit() != null)
            product.setUnit(updated.getUnit());
        if (updated.getHsnCode() != null)
            product.setHsnCode(updated.getHsnCode());
        if (updated.getGstPercent() != null)
            product.setGstPercent(updated.getGstPercent());
        if (updated.getMrp() != null)
            product.setMrp(updated.getMrp());
        if (updated.getSellingPrice() != null)
            product.setSellingPrice(updated.getSellingPrice());
        if (updated.getMinOrderQty() != null)
            product.setMinOrderQty(updated.getMinOrderQty());
        if (updated.getReorderLevel() != null)
            product.setReorderLevel(updated.getReorderLevel());
        if (updated.getSpecifications() != null)
            product.setSpecifications(updated.getSpecifications());
        if (updated.getIsActive() != null)
            product.setIsActive(updated.getIsActive());
        if (updated.getTags() != null)
            product.setTags(updated.getTags());

        Product saved = productRepository.save(product);
        auditService.log("UPDATE", "PRODUCT", saved.getId(), "Product updated: " + saved.getProductCode());
        return saved;
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
    }

    public PageResponse<Product> getProducts(String search, String brand, String category,
            int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Product> productPage = productRepository.searchProducts(search, brand, category, pageable);

        return PageResponse.<Product>builder()
                .content(productPage.getContent())
                .page(productPage.getNumber())
                .size(productPage.getSize())
                .totalElements(productPage.getTotalElements())
                .totalPages(productPage.getTotalPages())
                .last(productPage.isLast())
                .first(productPage.isFirst())
                .build();
    }

    // ─── INVENTORY MANAGEMENT ──────────────────────────────────────

    public PageResponse<InventoryItem> getInventory(String search, Long branchId,
            int page, int size,
            String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<InventoryItem> invPage = inventoryRepository.searchInventory(search, branchId, pageable);

        return PageResponse.<InventoryItem>builder()
                .content(invPage.getContent())
                .page(invPage.getNumber())
                .size(invPage.getSize())
                .totalElements(invPage.getTotalElements())
                .totalPages(invPage.getTotalPages())
                .last(invPage.isLast())
                .first(invPage.isFirst())
                .build();
    }

    @Transactional
    public InventoryItem adjustStock(Long productId, Long branchId, int quantityChange,
            String reason) {
        InventoryItem item = inventoryRepository.findByProductIdAndBranchId(productId, branchId)
                .orElseGet(() -> {
                    Product product = productRepository.findById(productId)
                            .orElseThrow(() -> new ResourceNotFoundException("Product", productId));
                    Branch branch = branchRepository.findById(branchId)
                            .orElseThrow(() -> new ResourceNotFoundException("Branch", branchId));
                    return InventoryItem.builder()
                            .product(product).branch(branch)
                            .availableQty(0).reservedQty(0).inTransitQty(0)
                            .build();
                });

        int newQty = item.getAvailableQty() + quantityChange;
        if (newQty < 0) {
            throw new BusinessException("Insufficient stock. Available: "
                    + item.getAvailableQty() + ", requested adjustment: " + quantityChange);
        }
        item.setAvailableQty(newQty);
        if (quantityChange > 0)
            item.setLastRestockedAt(LocalDateTime.now());

        InventoryItem saved = inventoryRepository.save(item);
        auditService.log("STOCK_ADJUST", "INVENTORY", saved.getId(),
                "Stock adjusted by " + quantityChange + " for product " + productId
                        + " at branch " + branchId + ". Reason: " + reason);
        return saved;
    }

    @Transactional
    public InventoryItem reserveStock(Long productId, Long branchId, int quantity) {
        InventoryItem item = inventoryRepository.findByProductIdAndBranchId(productId, branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for product "
                        + productId + " at branch " + branchId));

        if (item.getAvailableQty() < quantity) {
            throw new BusinessException("Insufficient available stock to reserve. Available: "
                    + item.getAvailableQty() + ", requested: " + quantity);
        }

        item.setAvailableQty(item.getAvailableQty() - quantity);
        item.setReservedQty(item.getReservedQty() + quantity);
        return inventoryRepository.save(item);
    }

    @Transactional
    public InventoryItem releaseReservation(Long productId, Long branchId, int quantity) {
        InventoryItem item = inventoryRepository.findByProductIdAndBranchId(productId, branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found"));

        int releaseQty = Math.min(quantity, item.getReservedQty());
        item.setReservedQty(item.getReservedQty() - releaseQty);
        item.setAvailableQty(item.getAvailableQty() + releaseQty);
        return inventoryRepository.save(item);
    }

    public List<InventoryItem> getLowStockItems(Long branchId) {
        return inventoryRepository.findLowStockItems(branchId);
    }

    public List<InventoryItem> getProductStockAcrossBranches(Long productId) {
        return inventoryRepository.findByProductId(productId);
    }

    // ─── STOCK TRANSFERS ──────────────────────────────────────────

    @Transactional
    public StockTransfer createTransfer(Long productId, Long fromBranchId,
            Long toBranchId, int quantity, String notes) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));
        Branch fromBranch = branchRepository.findById(fromBranchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch", fromBranchId));
        Branch toBranch = branchRepository.findById(toBranchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch", toBranchId));

        if (fromBranchId.equals(toBranchId)) {
            throw new BusinessException("Cannot transfer to the same branch");
        }

        Long maxId = transferRepository.findMaxId();
        long nextId = (maxId != null ? maxId : 0) + 1;

        Long currentUserId = securityUtils.getCurrentUserId();
        UserEntity requester = currentUserId != null
                ? userRepository.findById(currentUserId).orElse(null)
                : null;

        StockTransfer transfer = StockTransfer.builder()
                .transferNumber(String.format("TRF-%06d", nextId))
                .product(product)
                .fromBranch(fromBranch)
                .toBranch(toBranch)
                .quantity(quantity)
                .status(TransferStatus.REQUESTED)
                .requestedBy(requester)
                .notes(notes)
                .build();

        StockTransfer saved = transferRepository.save(transfer);
        auditService.log("CREATE", "STOCK_TRANSFER", saved.getId(),
                "Transfer requested: " + saved.getTransferNumber());
        return saved;
    }

    @Transactional
    public StockTransfer approveTransfer(Long transferId) {
        StockTransfer transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new ResourceNotFoundException("StockTransfer", transferId));

        if (transfer.getStatus() != TransferStatus.REQUESTED) {
            throw new BusinessException("Only REQUESTED transfers can be approved");
        }

        // Deduct stock from source branch
        InventoryItem sourceItem = inventoryRepository.findByProductIdAndBranchId(
                transfer.getProduct().getId(), transfer.getFromBranch().getId())
                .orElseThrow(() -> new BusinessException("No stock at source branch"));

        if (sourceItem.getAvailableQty() < transfer.getQuantity()) {
            throw new BusinessException("Insufficient stock at source branch. Available: "
                    + sourceItem.getAvailableQty());
        }

        sourceItem.setAvailableQty(sourceItem.getAvailableQty() - transfer.getQuantity());
        sourceItem.setInTransitQty(sourceItem.getInTransitQty() + transfer.getQuantity());
        inventoryRepository.save(sourceItem);

        Long currentUserId = securityUtils.getCurrentUserId();
        if (currentUserId != null) {
            UserEntity approver = userRepository.findById(currentUserId).orElse(null);
            transfer.setApprovedBy(approver);
        }

        transfer.setStatus(TransferStatus.IN_TRANSIT);
        transfer.setShippedAt(LocalDateTime.now());
        return transferRepository.save(transfer);
    }

    @Transactional
    public StockTransfer receiveTransfer(Long transferId) {
        StockTransfer transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new ResourceNotFoundException("StockTransfer", transferId));

        if (transfer.getStatus() != TransferStatus.IN_TRANSIT) {
            throw new BusinessException("Only IN_TRANSIT transfers can be received");
        }

        // Reduce in-transit from source
        InventoryItem sourceItem = inventoryRepository.findByProductIdAndBranchId(
                transfer.getProduct().getId(), transfer.getFromBranch().getId()).orElse(null);
        if (sourceItem != null) {
            sourceItem.setInTransitQty(Math.max(0,
                    sourceItem.getInTransitQty() - transfer.getQuantity()));
            inventoryRepository.save(sourceItem);
        }

        // Add stock at destination branch
        InventoryItem destItem = inventoryRepository.findByProductIdAndBranchId(
                transfer.getProduct().getId(), transfer.getToBranch().getId()).orElseGet(
                        () -> InventoryItem.builder()
                                .product(transfer.getProduct())
                                .branch(transfer.getToBranch())
                                .availableQty(0).reservedQty(0).inTransitQty(0)
                                .build());

        destItem.setAvailableQty(destItem.getAvailableQty() + transfer.getQuantity());
        destItem.setLastRestockedAt(LocalDateTime.now());
        inventoryRepository.save(destItem);

        transfer.setStatus(TransferStatus.RECEIVED);
        transfer.setReceivedAt(LocalDateTime.now());
        return transferRepository.save(transfer);
    }

    public PageResponse<StockTransfer> getTransfers(String search, int page, int size,
            String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Page<StockTransfer> transferPage = transferRepository.search(search,
                PageRequest.of(page, size, sort));

        return PageResponse.<StockTransfer>builder()
                .content(transferPage.getContent())
                .page(transferPage.getNumber())
                .size(transferPage.getSize())
                .totalElements(transferPage.getTotalElements())
                .totalPages(transferPage.getTotalPages())
                .last(transferPage.isLast())
                .first(transferPage.isFirst())
                .build();
    }
}
