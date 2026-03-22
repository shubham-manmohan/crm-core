/** Copyright © 2026 Mini2More. All Rights Reserved. Product: Mini2More CRM **/
package com.mini2more.crm.modules.crm.service;
import com.mini2more.crm.modules.crm.entity.LeadActivity;
import com.mini2more.crm.modules.crm.entity.LeadEntity;
import com.mini2more.crm.modules.crm.repository.SlaConfigRepository;
import com.mini2more.crm.modules.crm.repository.SlaLogRepository;
import com.mini2more.crm.modules.crm.repository.LeadActivityRepository;
import com.mini2more.crm.modules.crm.repository.LeadRepository;

import com.mini2more.crm.common.dto.PageResponse;
import com.mini2more.crm.common.enums.LeadPriority;
import com.mini2more.crm.common.enums.LeadStatus;
import com.mini2more.crm.common.exception.BusinessException;
import com.mini2more.crm.common.exception.DuplicateResourceException;
import com.mini2more.crm.common.exception.ResourceNotFoundException;
import com.mini2more.crm.config.AuditService;
import com.mini2more.crm.modules.crm.dto.*;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LeadService {

    private final LeadRepository leadRepository;
    private final LeadActivityRepository activityRepository;
    private final SlaConfigRepository slaConfigRepository;
    private final SlaLogRepository slaLogRepository;
    private final UserRepository userRepository;
    private final BranchRepository branchRepository;
    private final SecurityUtils securityUtils;
    private final AuditService auditService;

    // ─── CREATE ────────────────────────────────────────────────────

    @Transactional
    public LeadResponse createLead(CreateLeadRequest request) {
        // Duplicate detection
        if (request.getClientEmail() != null && request.getTitle() != null) {
            String keyword = request.getTitle().length() > 10
                    ? request.getTitle().substring(0, 10)
                    : request.getTitle();
            List<LeadEntity> duplicates = leadRepository.findPotentialDuplicates(
                    request.getClientEmail(), keyword, LocalDateTime.now().minusDays(30));
            if (!duplicates.isEmpty()) {
                throw new DuplicateResourceException(
                        "Potential duplicate lead found: " + duplicates.get(0).getLeadNumber()
                                + " (" + duplicates.get(0).getTitle() + ")");
            }
        }

        LeadEntity lead = LeadEntity.builder()
                .leadNumber(generateLeadNumber())
                .title(request.getTitle())
                .description(request.getDescription())
                .clientName(request.getClientName())
                .clientEmail(request.getClientEmail())
                .clientPhone(request.getClientPhone())
                .clientCompany(request.getClientCompany())
                .status(LeadStatus.NEW)
                .priority(request.getPriority() != null ? request.getPriority() : LeadPriority.MEDIUM)
                .source(request.getSource())
                .industry(request.getIndustry())
                .application(request.getApplication())
                .estimatedValue(request.getEstimatedValue())
                .nextFollowUp(request.getNextFollowUp())
                .followUpNotes(request.getFollowUpNotes())
                .productRequirements(request.getProductRequirements())
                .quantity(request.getQuantity())
                .brandPreference(request.getBrandPreference())
                .projectName(request.getProjectName())
                .tags(request.getTags())
                .slaBreached(false)
                .escalationLevel(0)
                .build();

        // Set client reference
        if (request.getClientId() != null) {
            UserEntity client = userRepository.findById(request.getClientId())
                    .orElseThrow(() -> new ResourceNotFoundException("Client", request.getClientId()));
            lead.setClient(client);
            if (lead.getClientName() == null)
                lead.setClientName(client.getFullName());
            if (lead.getClientEmail() == null)
                lead.setClientEmail(client.getEmail());
            if (lead.getClientPhone() == null)
                lead.setClientPhone(client.getPhone());
            if (lead.getClientCompany() == null)
                lead.setClientCompany(client.getCompany() != null ? client.getCompany().getName() : null);
        }

        // Set branch
        if (request.getBranchId() != null) {
            Branch branch = branchRepository.findById(request.getBranchId())
                    .orElseThrow(() -> new ResourceNotFoundException("Branch", request.getBranchId()));
            lead.setBranch(branch);
        }

        // Set assignee & kick status to ASSIGNED
        if (request.getAssignedToId() != null) {
            UserEntity assignee = userRepository.findById(request.getAssignedToId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", request.getAssignedToId()));
            lead.setAssignedTo(assignee);
            lead.setStatus(LeadStatus.ASSIGNED);
        }

        // Set SLA deadline
        applySlaDeadline(lead);

        // Set created by
        Long currentUserId = securityUtils.getCurrentUserId();
        lead.setCreatedBy(currentUserId);

        lead = leadRepository.save(lead);

        // Log activity
        addActivityInternal(lead, "CREATION", "Lead created: " + lead.getTitle(), null, null);
        if (lead.getAssignedTo() != null) {
            addActivityInternal(lead, "ASSIGNMENT",
                    "Lead assigned to " + lead.getAssignedTo().getFullName(),
                    null, lead.getAssignedTo().getFullName());
        }

        auditService.log("CREATE", "LEAD", lead.getId(), "Lead created: " + lead.getLeadNumber());

        return mapToResponse(lead, false);
    }

    // ─── UPDATE ────────────────────────────────────────────────────

    @Transactional
    public LeadResponse updateLead(Long id, UpdateLeadRequest request) {
        LeadEntity lead = leadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lead", id));

        if (request.getTitle() != null)
            lead.setTitle(request.getTitle());
        if (request.getDescription() != null)
            lead.setDescription(request.getDescription());
        if (request.getClientName() != null)
            lead.setClientName(request.getClientName());
        if (request.getClientEmail() != null)
            lead.setClientEmail(request.getClientEmail());
        if (request.getClientPhone() != null)
            lead.setClientPhone(request.getClientPhone());
        if (request.getClientCompany() != null)
            lead.setClientCompany(request.getClientCompany());
        if (request.getPriority() != null)
            lead.setPriority(request.getPriority());
        if (request.getSource() != null)
            lead.setSource(request.getSource());
        if (request.getIndustry() != null)
            lead.setIndustry(request.getIndustry());
        if (request.getApplication() != null)
            lead.setApplication(request.getApplication());
        if (request.getEstimatedValue() != null)
            lead.setEstimatedValue(request.getEstimatedValue());
        if (request.getNextFollowUp() != null)
            lead.setNextFollowUp(request.getNextFollowUp());
        if (request.getFollowUpNotes() != null)
            lead.setFollowUpNotes(request.getFollowUpNotes());
        if (request.getProductRequirements() != null)
            lead.setProductRequirements(request.getProductRequirements());
        if (request.getQuantity() != null)
            lead.setQuantity(request.getQuantity());
        if (request.getBrandPreference() != null)
            lead.setBrandPreference(request.getBrandPreference());
        if (request.getLostReason() != null)
            lead.setLostReason(request.getLostReason());
        if (request.getProjectName() != null)
            lead.setProjectName(request.getProjectName());
        if (request.getTags() != null)
            lead.setTags(request.getTags());

        // Client change
        if (request.getClientId() != null) {
            UserEntity client = userRepository.findById(request.getClientId())
                    .orElseThrow(() -> new ResourceNotFoundException("Client", request.getClientId()));
            lead.setClient(client);
        }

        // Branch change
        if (request.getBranchId() != null) {
            Branch branch = branchRepository.findById(request.getBranchId())
                    .orElseThrow(() -> new ResourceNotFoundException("Branch", request.getBranchId()));
            lead.setBranch(branch);
        }

        // Assignment change
        if (request.getAssignedToId() != null && !request.getAssignedToId().equals(
                lead.getAssignedTo() != null ? lead.getAssignedTo().getId() : null)) {
            UserEntity newAssignee = userRepository.findById(request.getAssignedToId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", request.getAssignedToId()));
            String oldAssignee = lead.getAssignedTo() != null ? lead.getAssignedTo().getFullName() : "Unassigned";
            lead.setAssignedTo(newAssignee);
            addActivityInternal(lead, "ASSIGNMENT",
                    "Lead reassigned from " + oldAssignee + " to " + newAssignee.getFullName(),
                    oldAssignee, newAssignee.getFullName());

            // Auto-transition NEW → ASSIGNED
            if (lead.getStatus() == LeadStatus.NEW) {
                changeStatusInternal(lead, LeadStatus.ASSIGNED);
            }
        }

        // Status change
        if (request.getStatus() != null && request.getStatus() != lead.getStatus()) {
            changeStatusInternal(lead, request.getStatus());
        }

        lead = leadRepository.save(lead);
        auditService.log("UPDATE", "LEAD", lead.getId(), "Lead updated: " + lead.getLeadNumber());

        return mapToResponse(lead, false);
    }

    // ─── STATUS TRANSITION ────────────────────────────────────────

    @Transactional
    public LeadResponse changeStatus(Long id, LeadStatus newStatus) {
        LeadEntity lead = leadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lead", id));
        changeStatusInternal(lead, newStatus);
        lead = leadRepository.save(lead);
        auditService.log("STATUS_CHANGE", "LEAD", lead.getId(), "Status changed to " + newStatus);
        return mapToResponse(lead, false);
    }

    private void changeStatusInternal(LeadEntity lead, LeadStatus newStatus) {
        LeadStatus oldStatus = lead.getStatus();
        validateStatusTransition(oldStatus, newStatus);
        lead.setStatus(newStatus);

        // Reset SLA for new stage
        applySlaDeadline(lead);

        // If lost, require reason
        if (newStatus == LeadStatus.LOST && (lead.getLostReason() == null || lead.getLostReason().isBlank())) {
            lead.setLostReason("Not specified");
        }

        addActivityInternal(lead, "STATUS_CHANGE",
                "Status changed from " + oldStatus + " to " + newStatus,
                oldStatus.name(), newStatus.name());
    }

    private void validateStatusTransition(LeadStatus from, LeadStatus to) {
        // LOST and ON_HOLD can be reached from any active state
        if (to == LeadStatus.LOST || to == LeadStatus.ON_HOLD)
            return;

        // ORDER_CONFIRMED can only come from NEGOTIATION or QUOTATION_SENT
        if (to == LeadStatus.ORDER_CONFIRMED
                && from != LeadStatus.NEGOTIATION && from != LeadStatus.QUOTATION_SENT) {
            throw new BusinessException("Cannot confirm order from status: " + from);
        }

        // Can reactivate from ON_HOLD to previous states
        if (from == LeadStatus.ON_HOLD)
            return;

        // Cannot go backwards in the main flow (except ON_HOLD recovery)
        if (from == LeadStatus.LOST) {
            throw new BusinessException("Cannot change status of a lost lead. Create a new lead instead.");
        }
        if (from == LeadStatus.ORDER_CONFIRMED) {
            throw new BusinessException("Cannot change status of a confirmed order.");
        }
    }

    // ─── ASSIGN ────────────────────────────────────────────────────

    @Transactional
    public LeadResponse assignLead(Long id, Long assigneeId) {
        LeadEntity lead = leadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lead", id));
        UserEntity assignee = userRepository.findById(assigneeId)
                .orElseThrow(() -> new ResourceNotFoundException("User", assigneeId));

        String oldAssignee = lead.getAssignedTo() != null ? lead.getAssignedTo().getFullName() : "Unassigned";
        lead.setAssignedTo(assignee);

        if (lead.getStatus() == LeadStatus.NEW) {
            lead.setStatus(LeadStatus.ASSIGNED);
        }

        addActivityInternal(lead, "ASSIGNMENT",
                "Lead assigned to " + assignee.getFullName(),
                oldAssignee, assignee.getFullName());

        lead = leadRepository.save(lead);
        auditService.log("ASSIGNMENT", "LEAD", lead.getId(), "Assigned to " + assignee.getFullName());
        return mapToResponse(lead, false);
    }

    // ─── READ ──────────────────────────────────────────────────────

    public LeadResponse getLeadById(Long id) {
        LeadEntity lead = leadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lead", id));
        return mapToResponse(lead, true);
    }

    public LeadResponse getLeadByNumber(String leadNumber) {
        LeadEntity lead = leadRepository.findByLeadNumber(leadNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Lead", "leadNumber", leadNumber));
        return mapToResponse(lead, true);
    }

    public PageResponse<LeadResponse> getAllLeads(
            String search, LeadStatus status, LeadPriority priority,
            Long assignedToId, Long branchId, String source,
            int page, int size, String sortBy, String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<LeadEntity> leadPage = leadRepository.findAllWithFilters(
                search, status, priority, assignedToId, branchId, source, pageable);

        return PageResponse.<LeadResponse>builder()
                .content(leadPage.getContent().stream()
                        .map(l -> mapToResponse(l, false)).toList())
                .page(leadPage.getNumber())
                .size(leadPage.getSize())
                .totalElements(leadPage.getTotalElements())
                .totalPages(leadPage.getTotalPages())
                .last(leadPage.isLast())
                .first(leadPage.isFirst())
                .build();
    }

    // ─── ACTIVITIES ────────────────────────────────────────────────

    @Transactional
    public LeadActivityResponse addActivity(Long leadId, AddActivityRequest request) {
        LeadEntity lead = leadRepository.findById(leadId)
                .orElseThrow(() -> new ResourceNotFoundException("Lead", leadId));

        LeadActivity activity = addActivityInternal(lead, request.getType(),
                request.getDescription(), null, null);
        if (request.getScheduledAt() != null) {
            activity.setScheduledAt(request.getScheduledAt());
            activityRepository.save(activity);
        }

        // If it's a follow-up, update lead's next follow-up
        if ("FOLLOW_UP".equals(request.getType()) && request.getScheduledAt() != null) {
            lead.setNextFollowUp(request.getScheduledAt());
            lead.setFollowUpNotes(request.getDescription());
            leadRepository.save(lead);
        }

        return mapActivityToResponse(activity);
    }

    public List<LeadActivityResponse> getActivities(Long leadId) {
        return activityRepository.findByLeadIdOrderByCreatedAtDesc(leadId)
                .stream().map(this::mapActivityToResponse).collect(Collectors.toList());
    }

    // ─── DASHBOARD STATS ──────────────────────────────────────────

    public LeadDashboardStats getDashboardStats() {
        Map<String, Long> bySource = new LinkedHashMap<>();
        Map<String, Long> byPriority = new LinkedHashMap<>();

        for (LeadPriority p : LeadPriority.values()) {
            byPriority.put(p.name(), leadRepository.findAllWithFilters(
                    null, null, p, null, null, null,
                    PageRequest.of(0, 1)).getTotalElements());
        }

        LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);

        return LeadDashboardStats.builder()
                .totalLeads(leadRepository.count())
                .newLeads(leadRepository.countByStatus(LeadStatus.NEW))
                .assignedLeads(leadRepository.countByStatus(LeadStatus.ASSIGNED))
                .inDiscussion(leadRepository.countByStatus(LeadStatus.IN_DISCUSSION))
                .quotationSent(leadRepository.countByStatus(LeadStatus.QUOTATION_SENT))
                .negotiation(leadRepository.countByStatus(LeadStatus.NEGOTIATION))
                .orderConfirmed(leadRepository.countByStatus(LeadStatus.ORDER_CONFIRMED))
                .lost(leadRepository.countByStatus(LeadStatus.LOST))
                .onHold(leadRepository.countByStatus(LeadStatus.ON_HOLD))
                .slaBreached(leadRepository.findLeadsBreachingSla(LocalDateTime.now()).size())
                .followUpsDueToday(leadRepository.findLeadsNeedingFollowUp(endOfDay).size())
                .leadsBySource(bySource)
                .leadsByPriority(byPriority)
                .build();
    }

    // ─── SLA ───────────────────────────────────────────────────────

    private void applySlaDeadline(LeadEntity lead) {
        slaConfigRepository.findByStageAndIsActiveTrue(lead.getStatus())
                .ifPresent(config -> {
                    lead.setSlaDeadline(LocalDateTime.now().plusHours(config.getMaxHours()));
                    lead.setSlaBreached(false);
                    lead.setEscalationLevel(0);
                });
    }

    // ─── INTERNAL HELPERS ──────────────────────────────────────────

    private LeadActivity addActivityInternal(LeadEntity lead, String type,
            String description, String oldValue, String newValue) {
        Long userId = securityUtils.getCurrentUserId();
        UserEntity user = userId != null ? userRepository.findById(userId).orElse(null) : null;

        LeadActivity activity = LeadActivity.builder()
                .lead(lead)
                .user(user)
                .type(type)
                .description(description)
                .oldValue(oldValue)
                .newValue(newValue)
                .build();

        return activityRepository.save(activity);
    }

    private String generateLeadNumber() {
        Long maxId = leadRepository.findMaxId();
        long nextId = (maxId != null ? maxId : 0) + 1;
        return String.format("LD-%06d", nextId);
    }

    // ─── MAPPERS ───────────────────────────────────────────────────

    private LeadResponse mapToResponse(LeadEntity lead, boolean includeActivities) {
        LeadResponse.LeadResponseBuilder builder = LeadResponse.builder()
                .id(lead.getId())
                .leadNumber(lead.getLeadNumber())
                .title(lead.getTitle())
                .description(lead.getDescription())
                .clientId(lead.getClient() != null ? lead.getClient().getId() : null)
                .clientName(lead.getClientName())
                .clientEmail(lead.getClientEmail())
                .clientPhone(lead.getClientPhone())
                .clientCompany(lead.getClientCompany())
                .assignedToId(lead.getAssignedTo() != null ? lead.getAssignedTo().getId() : null)
                .assignedToName(lead.getAssignedTo() != null ? lead.getAssignedTo().getFullName() : null)
                .branchId(lead.getBranch() != null ? lead.getBranch().getId() : null)
                .branchName(lead.getBranch() != null ? lead.getBranch().getName() : null)
                .status(lead.getStatus())
                .priority(lead.getPriority())
                .source(lead.getSource())
                .industry(lead.getIndustry())
                .application(lead.getApplication())
                .estimatedValue(lead.getEstimatedValue())
                .slaDeadline(lead.getSlaDeadline())
                .slaBreached(lead.getSlaBreached())
                .escalationLevel(lead.getEscalationLevel())
                .nextFollowUp(lead.getNextFollowUp())
                .followUpNotes(lead.getFollowUpNotes())
                .productRequirements(lead.getProductRequirements())
                .quantity(lead.getQuantity())
                .brandPreference(lead.getBrandPreference())
                .lostReason(lead.getLostReason())
                .projectName(lead.getProjectName())
                .tags(lead.getTags())
                .attachmentPaths(lead.getAttachmentPaths())
                .createdAt(lead.getCreatedAt())
                .updatedAt(lead.getUpdatedAt())
                .createdBy(lead.getCreatedBy());

        if (includeActivities) {
            List<LeadActivityResponse> activities = activityRepository
                    .findByLeadIdOrderByCreatedAtDesc(lead.getId())
                    .stream().map(this::mapActivityToResponse).toList();
            builder.activities(activities);
        }

        return builder.build();
    }

    private LeadActivityResponse mapActivityToResponse(LeadActivity activity) {
        return LeadActivityResponse.builder()
                .id(activity.getId())
                .type(activity.getType())
                .description(activity.getDescription())
                .oldValue(activity.getOldValue())
                .newValue(activity.getNewValue())
                .userId(activity.getUser() != null ? activity.getUser().getId() : null)
                .userName(activity.getUser() != null ? activity.getUser().getFullName() : null)
                .scheduledAt(activity.getScheduledAt())
                .createdAt(activity.getCreatedAt())
                .build();
    }
}
