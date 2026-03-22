/** Copyright © 2026 Mini2More. All Rights Reserved. Product: Mini2More CRM **/
package com.mini2more.crm.modules.crm.repository;
import com.mini2more.crm.modules.crm.entity.LeadEntity;

import com.mini2more.crm.common.enums.LeadPriority;
import com.mini2more.crm.common.enums.LeadStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface LeadRepository extends JpaRepository<LeadEntity, Long> {

    Optional<LeadEntity> findByLeadNumber(String leadNumber);

    @Query("SELECT l FROM LeadEntity l WHERE " +
            "(:search IS NULL OR :search = '' OR " +
            "LOWER(l.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(l.leadNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(l.clientName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(l.clientCompany) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(l.clientEmail) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(l.clientPhone) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND (:status IS NULL OR l.status = :status) " +
            "AND (:priority IS NULL OR l.priority = :priority) " +
            "AND (:assignedToId IS NULL OR l.assignedTo.id = :assignedToId) " +
            "AND (:branchId IS NULL OR l.branch.id = :branchId) " +
            "AND (:source IS NULL OR :source = '' OR l.source = :source)")
    Page<LeadEntity> findAllWithFilters(
            @Param("search") String search,
            @Param("status") LeadStatus status,
            @Param("priority") LeadPriority priority,
            @Param("assignedToId") Long assignedToId,
            @Param("branchId") Long branchId,
            @Param("source") String source,
            Pageable pageable);

    // SLA breach detection
    @Query("SELECT l FROM LeadEntity l WHERE l.slaDeadline IS NOT NULL " +
            "AND l.slaDeadline < :now AND l.slaBreached = false " +
            "AND l.status NOT IN ('LOST', 'ORDER_CONFIRMED', 'ON_HOLD')")
    List<LeadEntity> findLeadsBreachingSla(@Param("now") LocalDateTime now);

    // Duplicate detection: same client email + similar title
    @Query("SELECT l FROM LeadEntity l WHERE " +
            "l.clientEmail = :email AND LOWER(l.title) LIKE LOWER(CONCAT('%', :titleKeyword, '%')) " +
            "AND l.status NOT IN ('LOST', 'ORDER_CONFIRMED') " +
            "AND l.createdAt > :since")
    List<LeadEntity> findPotentialDuplicates(
            @Param("email") String email,
            @Param("titleKeyword") String titleKeyword,
            @Param("since") LocalDateTime since);

    // Count by status for dashboard
    long countByStatus(LeadStatus status);

    long countByAssignedToIdAndStatus(Long assignedToId, LeadStatus status);

    // Leads needing follow-up today
    @Query("SELECT l FROM LeadEntity l WHERE l.nextFollowUp IS NOT NULL " +
            "AND l.nextFollowUp <= :endOfDay " +
            "AND l.status NOT IN ('LOST', 'ORDER_CONFIRMED')")
    List<LeadEntity> findLeadsNeedingFollowUp(@Param("endOfDay") LocalDateTime endOfDay);

    // Lead number generation
    @Query("SELECT MAX(l.id) FROM LeadEntity l")
    Long findMaxId();

    @Query("SELECT COUNT(l) FROM LeadEntity l WHERE l.status NOT IN ('LOST', 'ORDER_CONFIRMED', 'CANCELLED')")
    long countActiveLeads();

    @Query("SELECT COUNT(l) FROM LeadEntity l WHERE l.slaBreached = true")
    long countSlaBreachedLeads();

    @Query("SELECT COUNT(l) FROM LeadEntity l WHERE l.slaBreached = false AND l.status NOT IN ('LOST', 'ORDER_CONFIRMED', 'CANCELLED')")
    long countSlaCompliantActiveLeads();
}
