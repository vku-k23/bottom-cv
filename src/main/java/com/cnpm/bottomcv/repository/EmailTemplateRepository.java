package com.cnpm.bottomcv.repository;

import com.cnpm.bottomcv.constant.EmailTemplateType;
import com.cnpm.bottomcv.model.EmailTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmailTemplateRepository extends JpaRepository<EmailTemplate, Long> {

    List<EmailTemplate> findByIsActiveTrue();

    List<EmailTemplate> findByTypeAndIsActiveTrue(EmailTemplateType type);

    @Query("SELECT e FROM EmailTemplate e WHERE e.isActive = true AND (e.company IS NULL OR e.company.id = :companyId)")
    List<EmailTemplate> findActiveTemplatesForCompany(@Param("companyId") Long companyId);

    @Query("SELECT e FROM EmailTemplate e WHERE e.isActive = true AND e.company IS NULL")
    List<EmailTemplate> findGlobalActiveTemplates();

    Optional<EmailTemplate> findByIdAndIsActiveTrue(Long id);

    List<EmailTemplate> findByCompanyIdAndIsActiveTrue(Long companyId);
}
