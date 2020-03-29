package com.bloknoma.organization.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;

@Configuration
public class ResourcesServerConfiguration extends ResourceServerConfigurerAdapter {

    // 접근 제한 설정
    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .antMatchers(HttpMethod.DELETE, "/v1/organizations/**") // 보호할 자원
                .hasRole("ADMIN") // 접근 가능 역할
                .anyRequest()
                .authenticated();
    }
}
