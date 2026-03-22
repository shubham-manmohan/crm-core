/** Copyright © 2026 Mini2More. All Rights Reserved. Product: Mini2More CRM **/
package com.mini2more.crm.modules.master.service;
import com.mini2more.crm.modules.master.entity.MasterData;
import com.mini2more.crm.modules.master.dto.MasterDataRequest;
import com.mini2more.crm.modules.master.dto.MasterDataResponse;
import com.mini2more.crm.modules.master.repository.MasterDataRepository;

import com.mini2more.crm.common.exception.DuplicateResourceException;
import com.mini2more.crm.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MasterDataService {

    private final MasterDataRepository repository;

    public List<MasterDataResponse> getByType(String type) {
        return repository.findByTypeAndIsActiveTrueOrderByDisplayOrderAscNameAsc(type.toUpperCase())
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<MasterDataResponse> getByTypeAndParent(String type, String parentCode) {
        return repository.findByTypeAndParentCodeAndIsActiveTrueOrderByDisplayOrderAscNameAsc(
                type.toUpperCase(), parentCode.toUpperCase())
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<MasterDataResponse> search(String type, String search) {
        return repository.searchByType(type.toUpperCase(), search)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    public MasterDataResponse create(MasterDataRequest request) {
        String type = request.getType().toUpperCase();
        String code = request.getCode().toUpperCase();

        if (repository.existsByTypeAndCode(type, code)) {
            throw new DuplicateResourceException("Master data already exists: " + type + "/" + code);
        }

        MasterData entity = MasterData.builder()
                .type(type)
                .code(code)
                .name(request.getName())
                .description(request.getDescription())
                .parentCode(request.getParentCode() != null ? request.getParentCode().toUpperCase() : null)
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .metadata(request.getMetadata())
                .build();

        entity = repository.save(entity);
        return mapToResponse(entity);
    }

    @Transactional
    public MasterDataResponse update(Long id, MasterDataRequest request) {
        MasterData entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MasterData", id));

        if (request.getName() != null)
            entity.setName(request.getName());
        if (request.getDescription() != null)
            entity.setDescription(request.getDescription());
        if (request.getParentCode() != null)
            entity.setParentCode(request.getParentCode().toUpperCase());
        if (request.getDisplayOrder() != null)
            entity.setDisplayOrder(request.getDisplayOrder());
        if (request.getIsActive() != null)
            entity.setIsActive(request.getIsActive());
        if (request.getMetadata() != null)
            entity.setMetadata(request.getMetadata());

        entity = repository.save(entity);
        return mapToResponse(entity);
    }

    @Transactional
    public void delete(Long id) {
        MasterData entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MasterData", id));
        entity.setIsActive(false);
        repository.save(entity);
    }

    private MasterDataResponse mapToResponse(MasterData entity) {
        return MasterDataResponse.builder()
                .id(entity.getId())
                .type(entity.getType())
                .code(entity.getCode())
                .name(entity.getName())
                .description(entity.getDescription())
                .parentCode(entity.getParentCode())
                .displayOrder(entity.getDisplayOrder())
                .isActive(entity.getIsActive())
                .metadata(entity.getMetadata())
                .build();
    }
}
