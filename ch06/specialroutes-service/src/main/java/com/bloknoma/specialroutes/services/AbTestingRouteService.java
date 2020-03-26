package com.bloknoma.specialroutes.services;

import com.bloknoma.specialroutes.exception.NoRouteFound;
import com.bloknoma.specialroutes.model.AbTestingRoute;
import com.bloknoma.specialroutes.repository.AbTestingRouteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AbTestingRouteService {

    @Autowired
    private AbTestingRouteRepository abTestingRouteRepository;

    public AbTestingRoute getRoute(String serviceName) {
        AbTestingRoute route = abTestingRouteRepository.findByServiceName(serviceName);

        if (route == null) {
            throw new NoRouteFound();
        }

        return route;
    }

    public void saveRoute(AbTestingRoute route) {
        abTestingRouteRepository.save(route);
    }

    public void updateRoute(AbTestingRoute route) {
        abTestingRouteRepository.save(route);
    }

    public void deleteRoute(AbTestingRoute route) {
        abTestingRouteRepository.delete(route);
    }
}
