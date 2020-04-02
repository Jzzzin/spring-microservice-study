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
    private OrganizationRepository orgRepositroy;

    @Autowired
    private Tracer tracer;

    @Autowired
    SimpleSourceBean simpleSourceBean;

    public Organization getOrg(String organizationId) {
        Optional<Organization> organization;

        // setting new span
        Span newSpan = tracer.nextSpan().name("getOrgDBCall");
        logger.debug("In the organizationService.getOrg() call");
        // span start
        try (SpanInScope ws = tracer.withSpanInScope(newSpan.start())) {
            organization = orgRepositroy.findById(organizationId);
        } finally {
            // tag
            newSpan.tag("peer.service", "postgres");
            // annotate close time
            newSpan.annotate("cr");
            // span close
            newSpan.finish();
        }

        if (!organization.isPresent()) {
            throw new NullPointerException("organizationId - " + organizationId);
        }
        return organization.get();
    }

    public void saveOrg(Organization org) {
        org.setId(UUID.randomUUID().toString());
        orgRepositroy.save(org);
        simpleSourceBean.publishOrgChange("SAVE", org.getId());
    }

    public void updateOrg(Organization org) {
        orgRepositroy.save(org);
        simpleSourceBean.publishOrgChange("UPDATE", org.getId());
    }

    public void deleteOrg(String organizationId) {
        orgRepositroy.deleteById(organizationId);
        simpleSourceBean.publishOrgChange("DELETE", organizationId);
    }
}
