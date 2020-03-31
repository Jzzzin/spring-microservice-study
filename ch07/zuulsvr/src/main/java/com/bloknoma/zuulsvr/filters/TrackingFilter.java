package com.bloknoma.zuulsvr.filters;

import com.bloknoma.zuulsvr.config.ServiceConfig;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TrackingFilter extends ZuulFilter {

    private static final Logger logger = LoggerFactory.getLogger(TrackingFilter.class);

    private static final int FILTER_ORDER = 1;
    private static final boolean SHOULD_FILTER = true;

    @Autowired
    private FilterUtils filterUtils;

    @Autowired
    private ServiceConfig serviceConfig;

    @Override
    public String filterType() {
        return FilterUtils.PRE_FILTER_TYPE;
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

        if (isCorrelationIdPresent()) {
            logger.debug("{} found in tracking filter: {}",
                    FilterUtils.CORRELATION_ID, filterUtils.getCorrelationId());
        } else {
            filterUtils.setCorrelationId(generateCorrelationId());
            logger.debug("{} generated in tracking filter: {}",
                    FilterUtils.CORRELATION_ID, filterUtils.getCorrelationId());
        }

        logger.debug("The organization id from the token is : " + getOrganizationId());
        filterUtils.setOrgId(getOrganizationId());

        RequestContext ctx = RequestContext.getCurrentContext();
        logger.debug("Processing incoming request for {}", ctx.getRequest().getRequestURI());
        return null;
    }

    private boolean isCorrelationIdPresent() {
        if (filterUtils.getCorrelationId() != null) {
            return true;
        }
        return false;
    }

    private String generateCorrelationId() {
        return java.util.UUID.randomUUID().toString();
    }

    private String getOrganizationId() {
        String result = "";
        if (filterUtils.getAuthToken() != null) {
            // Http header parsing
            String authToken = filterUtils.getAuthToken().replace("Bearer ", "");
            try {
                // token parsing
                Claims claims = Jwts.parser()
                        .setSigningKey(serviceConfig.getJwtSigningKey().getBytes("UTF-8")) // 서명 키 설정
                        .parseClaimsJws(authToken) // token 설정
                        .getBody();
                result = (String) claims.get("organizationId");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }
}
