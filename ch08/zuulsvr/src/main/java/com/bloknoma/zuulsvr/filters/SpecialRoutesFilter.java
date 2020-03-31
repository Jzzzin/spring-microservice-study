package com.bloknoma.zuulsvr.filters;

import com.bloknoma.zuulsvr.model.AbTestingRoute;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URL;
import java.util.Random;

@Component
public class SpecialRoutesFilter extends ZuulFilter {
    private static final Logger logger = LoggerFactory.getLogger(SpecialRoutesFilter.class);

    private static final int FILTER_ORDER = 1;
    private static final boolean SHOULD_FILTER = true;

    @Autowired
    FilterUtils filterUtils;

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    private ZuulProperties zuulProperties;

    @Override
    public String filterType() {
        return FilterUtils.ROUTE_FILTER_TYPE;
    }

    @Override
    public int filterOrder() {
        return FILTER_ORDER;
    }

    @Override
    public boolean shouldFilter() {
        return SHOULD_FILTER;
    }

    @Override
    public Object run() {
        if (zuulProperties.getRoutes().get("specialroutesservice") == null) {
            logger.debug("special routes service is not running");
            return null;
        }

        AbTestingRoute abTestingRoute = getAbRoutingInfo(filterUtils.getServiceId());

        if (abTestingRoute != null && useSpecialRoute(abTestingRoute)) {
            RequestContext ctx = RequestContext.getCurrentContext();

            try {
                URL host = new URL(abTestingRoute.getEndpoint());
                ctx.setRouteHost(host);
                logger.debug("New route: {}", host.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    private AbTestingRoute getAbRoutingInfo(String serviceName) {
        ResponseEntity<AbTestingRoute> restExchange = null;

        HttpHeaders headers = new HttpHeaders();
        headers.set(FilterUtils.CORRELATION_ID, filterUtils.getCorrelationId());
        HttpEntity<String> entity = new HttpEntity<String>(headers);

        try {
            logger.debug(">>> In SpecialRouterFilter.getAbRoutingInfo: {}",
                    filterUtils.getCorrelationId());
            restExchange = restTemplate.exchange(
                    "http://specialroutesservice/v1/route/abtesting/{serviceName}",
                    HttpMethod.GET, entity, AbTestingRoute.class, serviceName);
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) return null;
            throw ex;
        }

        return restExchange.getBody();
    }

    private boolean useSpecialRoute(AbTestingRoute abTestingRoute) {
        if (abTestingRoute.getActive().equals("N")) return false;

        Random random = new Random();
        int value = random.nextInt((10 - 1) + 1) + 1;

        if (abTestingRoute.getWeight() < value) return true;

        return false;
    }
}
