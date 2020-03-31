package com.bloknoma.licenses.repository;

import com.bloknoma.licenses.model.Organization;

public interface OrganizationRedisRepository {
    void saveOrganization(Organization org);

    void updateOrganization(Organization org);

    void deleteOrganization(String organizationId);

    Organization findOrganization(String organizationId);
}
