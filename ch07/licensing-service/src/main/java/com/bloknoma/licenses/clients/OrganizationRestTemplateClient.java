package com.bloknoma.licenses.clients;

import com.bloknoma.licenses.model.Organization;
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

    public Organization getOrganization(String organizationId) {
        logger.debug("In Licensing Service.getOrganization: {}",
                UserContextHolder.getContext().getCorrelationId());

        // OAuth2RestTemplate @LoadBalanced 하지 않았기 때문에 zuulservice 를 찾지 못한다.
        // zuulserver host 파일에 추가해야됨
        ResponseEntity<Organization> restExchange = restTemplate.exchange(
                "http://zuulserver:5555/api/organization/v1/organizations/{organizationId}",
                HttpMethod.GET, null, Organization.class, organizationId);

        return restExchange.getBody();
    }
}
