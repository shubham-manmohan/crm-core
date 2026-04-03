package com.mini2more.crm.modules.superadmin.service;

import com.mini2more.crm.modules.core.entity.Company;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SuperAdminService {

    private final EntityManager entityManager;

    @Transactional(readOnly = true)
    public List<Company> getAllCompaniesGodView() {
        // Explicitly unwrapping the session to override global tenant filters safely
        Session session = entityManager.unwrap(Session.class);
        session.disableFilter("tenantFilter");
        
        return session.createQuery("SELECT c FROM Company c", Company.class).getResultList();
    }
}
