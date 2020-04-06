package com.bloknoma.zuulsvr.filters;

import brave.Tracer;
import com.netflix.zuul.context.RequestContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FilterUtils {
    public static final String AUTH_TOKEN = "Authorization";
    public static final String CORRELATION_ID = "bn-correlation-id";
    public static final String USER_ID = "bn-user-id";
    public static final String ORG_ID = "bn-org-id";

    public static final String PRE_FILTER_TYPE = "pre";
    public static final String POST_FILTER_TYPE = "post";
    public static final String ROUTE_FILTER_TYPE = "route";

    @Autowired
    Tracer tracer;

    public final String getOrgId() {
        RequestContext ctx = RequestContext.getCurrentContext();
        if (ctx.getRequest().getHeader(ORG_ID) != null) {
            return ctx.getRequest().getHeader(ORG_ID);
        } else {
            return ctx.getZuulRequestHeaders().get(ORG_ID);
        }
    }

    public void setOrgId(String orgId) {
        RequestContext ctx = RequestContext.getCurrentContext();
        ctx.addZuulRequestHeader(ORG_ID, orgId);
    }

    public final String getUserId() {
        RequestContext ctx = RequestContext.getCurrentContext();
        if (ctx.getRequest().getHeader(USER_ID) != null) {
            return ctx.getRequest().getHeader(USER_ID);
        } else {
            return ctx.getZuulRequestHeaders().get(USER_ID);
        }
    }

    public void setUserId(String userId) {
        RequestContext ctx = RequestContext.getCurrentContext();
        ctx.addZuulRequestHeader(USER_ID, userId);
    }

    public final String getAuthToken() {
        RequestContext ctx = RequestContext.getCurrentContext();
        return ctx.getRequest().getHeader(AUTH_TOKEN);
    }

    public final String getCorrelationId() {
        RequestContext ctx = RequestContext.getCurrentContext();
        if (ctx.getRequest().getHeader(CORRELATION_ID) != null) {
            return ctx.getRequest().getHeader(CORRELATION_ID);
        } else {
            return tracer.currentSpan().context().traceIdString();
        }
    }

    public String getServiceId() {
        RequestContext ctx = RequestContext.getCurrentContext();
        if (ctx.get("serviceId") == null) return "";
        return ctx.get("serviceId").toString();

    }
}
