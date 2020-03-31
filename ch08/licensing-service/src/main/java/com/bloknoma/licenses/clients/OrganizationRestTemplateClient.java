package com.bloknoma.licenses.clients;

import com.bloknoma.licenses.model.Organization;
import com.bloknoma.licenses.repository.OrganizationRedisRepository;
import com.bloknoma.licenses.utils.UserContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class OrganizationRestTemplateClient {
    private static final Logger logger = LoggerFactory.getLogger(OrganizationRestTemplateClient.class);

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    OrganizationRedisRepository orgRedisRepo;

    public Organization getOrganization(String organizationId) {
        logger.debug("In Licensing Service.getOrganization: {}",
                UserContextHolder.getContext().getCorrelationId());

        // redis search
        Organization org = checkRedisCache(organizationId);

        if (org != null) {
            logger.debug("I have successfully retrieved an organization {} from redis cache: {}",
                    organizationId, org);
            return org;
        }

        logger.debug("Unable to locate organization from the redis cache: {}", organizationId);

        // JWT 사용하므로 OAuth2RestTemplate 사용하지 않고, LoadBalanced 해서 zuulservice discovery 함
        ResponseEntity<Organization> restExchange = restTemplate.exchange(
                "http://zuulservice/api/organization/v1/organizations/{organizationId}",
                HttpMethod.GET, null, Organization.class, organizationId);

        // Save the record from cache
        org = restExchange.getBody();
        if (org != null) {
            cacheOrganizationObject(org);
        }

        return org;
    }

    // redis search
    private Organization checkRedisCache(String organizationId) {
        try {
            return orgRedisRepo.findOrganization(organizationId);
        } catch (Exception ex) {
            logger.error("Error encountered while trying to retrieve organization {} check Redis Cache. Exception {}", organizationId, ex);
            return null; // error 발행해도 서비스 중단되지 않도록 처리한다
        }
    }

    // redis save
    private void cacheOrganizationObject(Organization org) {
        try {
            orgRedisRepo.saveOrganization(org);
        } catch (Exception ex) {
            logger.error("Unable to cache organization {} in Redis. Exception {}", org.getId(), ex);
        }
    }
}
