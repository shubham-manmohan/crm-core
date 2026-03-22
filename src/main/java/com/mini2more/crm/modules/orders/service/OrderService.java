/** Copyright © 2026 Mini2More. All Rights Reserved. Product: Mini2More CRM **/
package com.mini2more.crm.modules.orders.service;
import com.mini2more.crm.modules.orders.entity.OrderItem;
import com.mini2more.crm.modules.orders.entity.Dispatch;
import com.mini2more.crm.modules.orders.entity.DispatchItem;
import com.mini2more.crm.modules.orders.repository.SalesOrderRepository;
import com.mini2more.crm.modules.orders.repository.DispatchRepository;
import com.mini2more.crm.modules.orders.entity.SalesOrder;

import com.mini2more.crm.common.dto.PageResponse;
import com.mini2more.crm.common.enums.DispatchStatus;
import com.mini2more.crm.common.enums.OrderStatus;
import com.mini2more.crm.common.exception.BusinessException;
import com.mini2more.crm.common.exception.ResourceNotFoundException;
import com.mini2more.crm.config.AuditService;
import com.mini2more.crm.modules.inventory.service.InventoryService;
import com.mini2more.crm.modules.inventory.entity.Product;
import com.mini2more.crm.modules.inventory.repository.ProductRepository;
import com.mini2more.crm.modules.crm.entity.LeadEntity;
import com.mini2more.crm.modules.crm.repository.LeadRepository;
import com.mini2more.crm.modules.quotation.entity.Quotation;
import com.mini2more.crm.modules.quotation.repository.QuotationRepository;
import com.mini2more.crm.modules.core.entity.Branch;
import com.mini2more.crm.modules.core.repository.BranchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final SalesOrderRepository orderRepository;
    private final DispatchRepository dispatchRepository;
    private final LeadRepository leadRepository;
    private final QuotationRepository quotationRepository;
    private final BranchRepository branchRepository;
    private final ProductRepository productRepository;
    private final InventoryService inventoryService;
    private final AuditService auditService;

    // ─── CREATE ORDER ─────────────────────────────────────────────

    @Transactional
    @SuppressWarnings("unchecked")
    public SalesOrder createOrder(Map<String, Object> payload) {
        Long maxId = orderRepository.findMaxId();
        long nextId = (maxId != null ? maxId : 0) + 1;

        SalesOrder.SalesOrderBuilder builder = SalesOrder.builder()
                .orderNumber(String.format("ORD-%06d", nextId))
                .status(OrderStatus.PENDING_CONFIRMATION)
                .clientName(str(payload, "clientName"))
                .clientEmail(str(payload, "clientEmail"))
                .clientPhone(str(payload, "clientPhone"))
                .clientCompany(str(payload, "clientCompany"))
                .clientGst(str(payload, "clientGst"))
                .shippingAddress(str(payload, "shippingAddress"))
                .billingAddress(str(payload, "billingAddress"))
                .paymentTerms(str(payload, "paymentTerms"))
                .deliveryTerms(str(payload, "deliveryTerms"))
                .notes(str(payload, "notes"))
                .clientPoNumber(str(payload, "clientPoNumber"))
                .items(new ArrayList<>());

        // Link to lead
        if (payload.get("leadId") != null) {
            Long leadId = Long.valueOf(payload.get("leadId").toString());
            LeadEntity lead = leadRepository.findById(leadId).orElse(null);
            builder.lead(lead);
        }
        // Link to quotation
        if (payload.get("quotationId") != null) {
            Long quotationId = Long.valueOf(payload.get("quotationId").toString());
            Quotation quotation = quotationRepository.findById(quotationId).orElse(null);
            builder.quotation(quotation);
        }
        // Link to branch
        if (payload.get("branchId") != null) {
            Long branchId = Long.valueOf(payload.get("branchId").toString());
            Branch branch = branchRepository.findById(branchId).orElse(null);
            builder.branch(branch);
        }

        SalesOrder order = builder.build();

        // Process items
        List<Map<String, Object>> itemsData = (List<Map<String, Object>>) payload.get("items");
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal gstTotal = BigDecimal.ZERO;

        if (itemsData != null) {
            for (Map<String, Object> itemData : itemsData) {
                OrderItem.OrderItemBuilder itemBuilder = OrderItem.builder()
                        .salesOrder(order)
                        .productName(str(itemData, "productName"))
                        .productCode(str(itemData, "productCode"))
                        .brand(str(itemData, "brand"))
                        .unit(itemData.getOrDefault("unit", "Nos").toString())
                        .orderedQty(Integer.parseInt(itemData.getOrDefault("orderedQty", "1").toString()))
                        .unitPrice(new BigDecimal(itemData.getOrDefault("unitPrice", "0").toString()))
                        .gstPercent(new BigDecimal(itemData.getOrDefault("gstPercent", "18").toString()))
                        .hsnCode(str(itemData, "hsnCode"));

                if (itemData.get("productId") != null) {
                    Long productId = Long.valueOf(itemData.get("productId").toString());
                    Product product = productRepository.findById(productId).orElse(null);
                    itemBuilder.product(product);
                }

                OrderItem item = itemBuilder.build();
                BigDecimal lineTotal = item.getUnitPrice()
                        .multiply(BigDecimal.valueOf(item.getOrderedQty()));
                item.setTotalPrice(lineTotal);

                BigDecimal gst = lineTotal.multiply(item.getGstPercent())
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                gstTotal = gstTotal.add(gst);
                subtotal = subtotal.add(lineTotal);

                order.getItems().add(item);
            }
        }

        BigDecimal discount = payload.get("discountAmount") != null
                ? new BigDecimal(payload.get("discountAmount").toString()) : BigDecimal.ZERO;

        order.setSubtotal(subtotal);
        order.setDiscountAmount(discount);
        order.setGstAmount(gstTotal);
        order.setTotalAmount(subtotal.subtract(discount).add(gstTotal));

        SalesOrder saved = orderRepository.save(order);
        auditService.log("CREATE", "ORDER", saved.getId(), "Order created: " + saved.getOrderNumber());
        return saved;
    }

    // ─── LIFECYCLE ────────────────────────────────────────────────

    @Transactional
    public SalesOrder confirmOrder(Long id) {
        SalesOrder order = getById(id);
        if (order.getStatus() != OrderStatus.PENDING_CONFIRMATION) {
            throw new BusinessException("Only PENDING_CONFIRMATION orders can be confirmed");
        }
        order.setStatus(OrderStatus.CONFIRMED);

        // Reserve stock for all items
        if (order.getBranch() != null) {
            for (OrderItem item : order.getItems()) {
                if (item.getProduct() != null) {
                    try {
                        inventoryService.reserveStock(
                                item.getProduct().getId(), order.getBranch().getId(), item.getOrderedQty()
                        );
                    } catch (Exception e) {
                        log.warn("Could not reserve stock for {}: {}", item.getProductName(), e.getMessage());
                    }
                }
            }
        }

        order.setStatus(OrderStatus.PROCESSING);
        auditService.log("CONFIRM", "ORDER", order.getId(), "Order confirmed");
        return orderRepository.save(order);
    }

    @Transactional
    public SalesOrder cancelOrder(Long id, String reason) {
        SalesOrder order = getById(id);
        if (order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.CANCELLED) {
            throw new BusinessException("Cannot cancel delivered or already cancelled orders");
        }

        // Release reserved stock
        if (order.getBranch() != null) {
            for (OrderItem item : order.getItems()) {
                if (item.getProduct() != null) {
                    try {
                        int reservedQty = item.getOrderedQty() - (item.getDispatchedQty() != null ? item.getDispatchedQty() : 0);
                        if (reservedQty > 0) {
                            inventoryService.releaseReservation(
                                    item.getProduct().getId(), order.getBranch().getId(), reservedQty
                            );
                        }
                    } catch (Exception e) {
                        log.warn("Could not release stock for {}: {}", item.getProductName(), e.getMessage());
                    }
                }
            }
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setCancellationReason(reason);
        auditService.log("CANCEL", "ORDER", order.getId(), "Order cancelled: " + reason);
        return orderRepository.save(order);
    }

    // ─── DISPATCH ─────────────────────────────────────────────────

    @Transactional
    @SuppressWarnings("unchecked")
    public Dispatch createDispatch(Long orderId, Map<String, Object> payload) {
        SalesOrder order = getById(orderId);

        if (order.getStatus() != OrderStatus.PROCESSING
                && order.getStatus() != OrderStatus.CONFIRMED
                && order.getStatus() != OrderStatus.PARTIALLY_DISPATCHED) {
            throw new BusinessException("Order must be PROCESSING or PARTIALLY_DISPATCHED to dispatch");
        }

        Long maxId = dispatchRepository.findMaxId();
        long nextId = (maxId != null ? maxId : 0) + 1;

        Dispatch dispatch = Dispatch.builder()
                .dispatchNumber(String.format("DSP-%06d", nextId))
                .salesOrder(order)
                .branch(order.getBranch())
                .status(DispatchStatus.PACKING)
                .courierName(str(payload, "courierName"))
                .trackingNumber(str(payload, "trackingNumber"))
                .vehicleNumber(str(payload, "vehicleNumber"))
                .driverName(str(payload, "driverName"))
                .driverPhone(str(payload, "driverPhone"))
                .shippingAddress(order.getShippingAddress())
                .notes(str(payload, "notes"))
                .items(new ArrayList<>())
                .build();

        // Process dispatch items
        List<Map<String, Object>> itemsData = (List<Map<String, Object>>) payload.get("items");
        if (itemsData == null || itemsData.isEmpty()) {
            throw new BusinessException("At least one item must be dispatched");
        }

        for (Map<String, Object> itemData : itemsData) {
            Long orderItemId = Long.valueOf(itemData.get("orderItemId").toString());
            int qty = Integer.parseInt(itemData.get("quantity").toString());

            OrderItem orderItem = order.getItems().stream()
                    .filter(i -> i.getId().equals(orderItemId))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("OrderItem", orderItemId));

            if (qty > orderItem.getPendingQty()) {
                throw new BusinessException("Dispatch qty (" + qty
                        + ") exceeds pending qty (" + orderItem.getPendingQty()
                        + ") for " + orderItem.getProductName());
            }

            // Update order item
            orderItem.setDispatchedQty(
                    (orderItem.getDispatchedQty() != null ? orderItem.getDispatchedQty() : 0) + qty
            );

            DispatchItem di = DispatchItem.builder()
                    .dispatch(dispatch)
                    .orderItem(orderItem)
                    .dispatchedQty(qty)
                    .productName(orderItem.getProductName())
                    .productCode(orderItem.getProductCode())
                    .build();
            dispatch.getItems().add(di);
        }

        // Update order status
        boolean allDispatched = order.getItems().stream()
                .allMatch(i -> i.getPendingQty() <= 0);
        order.setStatus(allDispatched ? OrderStatus.FULLY_DISPATCHED : OrderStatus.PARTIALLY_DISPATCHED);

        orderRepository.save(order);
        Dispatch saved = dispatchRepository.save(dispatch);
        auditService.log("DISPATCH", "ORDER", orderId,
                "Dispatch " + saved.getDispatchNumber() + " created");
        return saved;
    }

    @Transactional
    public Dispatch updateDispatchStatus(Long dispatchId, DispatchStatus newStatus) {
        Dispatch dispatch = dispatchRepository.findById(dispatchId)
                .orElseThrow(() -> new ResourceNotFoundException("Dispatch", dispatchId));

        dispatch.setStatus(newStatus);

        if (newStatus == DispatchStatus.SHIPPED || newStatus == DispatchStatus.IN_TRANSIT) {
            dispatch.setDispatchedAt(LocalDateTime.now());
        }
        if (newStatus == DispatchStatus.DELIVERED) {
            dispatch.setDeliveredAt(LocalDateTime.now());

            // Update order item delivered qty
            for (DispatchItem di : dispatch.getItems()) {
                OrderItem oi = di.getOrderItem();
                oi.setDeliveredQty(
                        (oi.getDeliveredQty() != null ? oi.getDeliveredQty() : 0) + di.getDispatchedQty()
                );
            }

            // Check if all items delivered
            SalesOrder order = dispatch.getSalesOrder();
            boolean allDelivered = order.getItems().stream()
                    .allMatch(i -> i.getDeliveredQty() >= i.getOrderedQty());
            if (allDelivered) {
                order.setStatus(OrderStatus.DELIVERED);
                orderRepository.save(order);
            }
        }

        auditService.log("STATUS_UPDATE", "DISPATCH", dispatchId,
                "Dispatch status → " + newStatus);
        return dispatchRepository.save(dispatch);
    }

    // ─── READ ─────────────────────────────────────────────────────

    public SalesOrder getById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));
    }

    public PageResponse<SalesOrder> getOrders(String search, OrderStatus status,
                                               int page, int size,
                                               String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Page<SalesOrder> orderPage = orderRepository.search(search, status,
                PageRequest.of(page, size, sort));
        return PageResponse.<SalesOrder>builder()
                .content(orderPage.getContent())
                .page(orderPage.getNumber())
                .size(orderPage.getSize())
                .totalElements(orderPage.getTotalElements())
                .totalPages(orderPage.getTotalPages())
                .last(orderPage.isLast())
                .first(orderPage.isFirst())
                .build();
    }

    public List<Dispatch> getDispatchesForOrder(Long orderId) {
        return dispatchRepository.findBySalesOrderId(orderId);
    }

    public PageResponse<Dispatch> getDispatches(String search, int page, int size,
                                                  String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Page<Dispatch> dPage = dispatchRepository.search(search, PageRequest.of(page, size, sort));
        return PageResponse.<Dispatch>builder()
                .content(dPage.getContent())
                .page(dPage.getNumber())
                .size(dPage.getSize())
                .totalElements(dPage.getTotalElements())
                .totalPages(dPage.getTotalPages())
                .last(dPage.isLast())
                .first(dPage.isFirst())
                .build();
    }

    private String str(Map<String, Object> map, String key) {
        Object v = map.get(key);
        return v != null ? v.toString() : null;
    }
}
