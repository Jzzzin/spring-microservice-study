package com.bloknoma.authentication.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

/* JDBC 사용 시 주석 처리
@Configuration
public class WebSecurityConfigurer extends WebSecurityConfigurerAdapter {

    // 인증 처리 bean
    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    // 사용자 정보 저장 bean
    @Override
    @Bean
    public UserDetailsService userDetailsServiceBean() throws Exception {
        return super.userDetailsServiceBean();
    }

    // 사용자 정의
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        auth
                .inMemoryAuthentication()
                .passwordEncoder(encoder)
                .withUser("john.carnell")
                    .password(encoder.encode("password1"))
                    .roles("USER")
                .and()
                .withUser("william.woodward")
                    .password(encoder.encode("password2"))
                    .roles("USER", "ADMIN");
    }
}
*/
