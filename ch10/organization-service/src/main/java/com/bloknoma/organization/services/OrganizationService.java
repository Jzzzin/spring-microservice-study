package com.bloknoma.organization.services;

import brave.Span;
import brave.Tracer;
import brave.Tracer.SpanInScope;
import com.bloknoma.organization.events.source.SimpleSourceBean;
import com.bloknoma.organization.model.Organization;
import com.bloknoma.organization.repository.OrganizationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class OrganizationService {
    private static final Logger logger = LoggerFactory.getLogger(OrganizationService.class);

    @Autowired
    private OrganizationRepository orgRepository;

    @Autowired
    private Tracer tracer;

    @Autowired
    SimpleSourceBean simpleSourceBean;

    public Organization getOrg(String organizationId) {
        logger.debug(">>> In the OrganizationService.getOrg() call");

        Optional<Organization> organization;

        Span newSpan = tracer.nextSpan().name("getOrgDBCall");
        try (SpanInScope ws = tracer.withSpanInScope(newSpan.start())) {
            organization = orgRepository.findById(organizationId);
        } finally {
            newSpan.tag("peer.service", "postgres");
            newSpan.annotate("cr");
            newSpan.finish();
        }

        if (!organization.isPresent()) {
            throw new NullPointerException("organizationId - " + organizationId);
        }
        return organization.get();
    }

    public void saveOrg(Organization org) {
        org.setId(UUID.randomUUID().toString());
        orgRepository.save(org);
        simpleSourceBean.publishOrgChanges("SAVE", org.getId());
    }

    public void updateOrg(Organization org) {
        orgRepository.save(org);
        simpleSourceBean.publishOrgChanges("UPDATE", org.getId());
    }

    public void deleteOrg(String organizationId) {
        orgRepository.deleteById(organizationId);
        simpleSourceBean.publishOrgChanges("DELETE", organizationId);
    }
}
