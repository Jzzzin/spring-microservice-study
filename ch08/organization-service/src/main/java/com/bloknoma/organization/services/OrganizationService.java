package com.bloknoma.organization.services;

import com.bloknoma.organization.events.source.SimpleSourceBean;
import com.bloknoma.organization.model.Organization;
import com.bloknoma.organization.repository.OrganizationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class OrganizationService {

    @Autowired
    private OrganizationRepository orgRepository;

    // message publisher
    @Autowired
    SimpleSourceBean simpleSourceBean;

    public Organization getOrg(String organizationId) {
        Optional<Organization> organization = orgRepository.findById(organizationId);
        if (!organization.isPresent()) {
            throw new NullPointerException("organizationId - " + organizationId);
        }
        return organization.get();
    }

    public void saveOrg(Organization org) {
        org.setId(UUID.randomUUID().toString());
        orgRepository.save(org);
        simpleSourceBean.publishOrgChange("SAVE", org.getId());
    }

    public void updateOrg(Organization org) {
        orgRepository.save(org);
        simpleSourceBean.publishOrgChange("UPDATE", org.getId());
    }

    public void deleteOrg(String organizationId) {
        orgRepository.deleteById(organizationId);
        simpleSourceBean.publishOrgChange("DELETE", organizationId);
    }
}
