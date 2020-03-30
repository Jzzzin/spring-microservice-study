package com.bloknoma.authentication.security;

import com.bloknoma.authentication.model.UserOrganization;
import com.bloknoma.authentication.repository.OrgUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;

import java.util.HashMap;
import java.util.Map;

public class JWTTokenEnhancer implements TokenEnhancer {

    @Autowired
    private OrgUserRepository orgUserRepo;

    private String getOrgId(String userName) {
        UserOrganization orgUser = orgUserRepo.findByUserName(userName);
        return orgUser.getOrganizationId();
    }

    @Override
    public OAuth2AccessToken enhance(OAuth2AccessToken accessToken,
                                     OAuth2Authentication authentication) {

        Map<String, Object> additionalInfo = new HashMap<>();
        String orgId = getOrgId(authentication.getName());
        // map 에 추가 정보 저장
        additionalInfo.put("organizationId", orgId);

        // token 에 추가 정보 저장
        ((DefaultOAuth2AccessToken) accessToken).setAdditionalInformation(additionalInfo);
        return accessToken;
    }
}
