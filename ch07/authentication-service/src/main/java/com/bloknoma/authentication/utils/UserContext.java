package com.bloknoma.authentication.utils;

import org.springframework.stereotype.Component;

@Component
public class UserContext {
    public static final String AUTH_TOKEN = "Authorization";
    public static final String CORRELATION_ID = "bn-correlation-id";
    public static final String USER_ID = "bn-user-id";
    public static final String ORG_ID = "bn-org-id";

    private String authToken = new String();
    private String correlationId = new String();
    private String userId = new String();
    private String orgId = new String();

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }
}
