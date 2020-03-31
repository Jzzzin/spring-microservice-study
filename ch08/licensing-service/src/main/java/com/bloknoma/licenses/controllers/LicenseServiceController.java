package com.bloknoma.licenses.controllers;

import com.bloknoma.licenses.model.License;
import com.bloknoma.licenses.services.LicenseService;
import com.bloknoma.licenses.utils.UserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping(value = "v1/organizations/{organizationId}/licenses")
public class LicenseServiceController {
    private static final Logger logger = LoggerFactory.getLogger(LicenseServiceController.class);

    @Autowired
    private LicenseService licenseService;

    @Autowired
    private HttpServletRequest request;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public List<License> getLicenses(@PathVariable("organizationId") String organizationId) {
        return licenseService.getLicensesByOrg(organizationId);
    }

    @RequestMapping(value = "/{licenseId}", method = RequestMethod.GET)
    public License getLicenses(@PathVariable("organizationId") String organizationId,
                               @PathVariable("licenseId") String licenseId) {
        logger.debug("Entering the license-service-controller");
        logger.debug("Found {} in license-service-controller: {}",
                UserContext.CORRELATION_ID, request.getHeader(UserContext.CORRELATION_ID));
        return licenseService.getLicense(organizationId, licenseId);
    }

    @RequestMapping(value = "/{licenseId}", method = RequestMethod.PUT)
    public void updateLicenses(@PathVariable("licenseId") String licenseId,
                               @RequestBody License license) {
        licenseService.updateLicense(license);
    }

    @RequestMapping(value = "/", method = RequestMethod.POST)
    public void saveLicenses(@RequestBody License license) {
        licenseService.saveLicense(license);
    }

    @RequestMapping(value = "/{licenseId}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLicenses(@PathVariable("licenseId") String licenseId) {
        licenseService.deleteLicense(licenseId);
    }


}
