package com.bloknoma.specialroutes.controllers;

import com.bloknoma.specialroutes.model.AbTestingRoute;
import com.bloknoma.specialroutes.services.AbTestingRouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "v1/route")
public class SpecialRoutesServiceController {

    @Autowired
    AbTestingRouteService routeService;

    @RequestMapping(value = "/abtesting/{serviceName}", method = RequestMethod.GET)
    public AbTestingRoute abTestings(@PathVariable("serviceName") String serviceName) {
        return routeService.getRoute(serviceName);
    }
}
