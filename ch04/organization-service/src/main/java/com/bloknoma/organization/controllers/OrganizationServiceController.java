package com.bloknoma.organization.controllers;

import com.bloknoma.organization.model.Organization;
import com.bloknoma.organization.services.OrganizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "v1/organizations")
public class OrganizationServiceController {

    @Autowired
    private OrganizationService orgService;

    @RequestMapping(value = "/{organizationId}", method = RequestMethod.GET)
    public Organization getOrganization(@PathVariable("organizationId") String organizationId) {
        return orgService.getOrg(organizationId);
    }

    @RequestMapping(value = "/{organizationId}", method = RequestMethod.PUT)
    public void updateOrganization(@PathVariable("organizationId") String organizationId,
                                   @RequestBody Organization organization) {
        orgService.updateOrg(organization);
    }

    // OrganizationId 가 없으므로 value 변경함
    @RequestMapping(value = "/", method = RequestMethod.POST)
    public void saveOrganization(@RequestBody Organization organization) {
        orgService.saveOrg(organization);
    }

    @RequestMapping(value = "/{organizationId}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOrganization(@PathVariable("organizationId") String organizationId,
                                   @RequestBody Organization organization) {
        orgService.deleteOrg(organization);
    }
}
