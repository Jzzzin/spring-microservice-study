package com.bloknoma.authentication.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;

import java.util.Arrays;

@Configuration
public class JWTOAuth2Config extends AuthorizationServerConfigurerAdapter {

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private UserDetailsService userDetailsService;

    // JWTTokenStoreConfig 에서 설정한 bean
    @Autowired
    private TokenStore tokenStore;
    @Autowired
    private DefaultTokenServices tokenServices;
    @Autowired
    private JwtAccessTokenConverter jwtAccessTokenConverter;
    @Autowired
    private TokenEnhancer jwtTokenEnhancer;

    // JDBC 사용하는 경우 client secret 도 passwordEncoder 를 맞춰줘야 함
    @Autowired
    private PasswordEncoder passwordEncoder;

    // endpoint 설정
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        // token enhancer 사용 위하여 tokenenhancerchain 등록
        TokenEnhancerChain tokenEnhancerChain = new TokenEnhancerChain();
        tokenEnhancerChain.setTokenEnhancers(Arrays.asList(jwtTokenEnhancer, jwtAccessTokenConverter));

        endpoints
                .tokenStore(tokenStore) // token store 설정
                .accessTokenConverter(jwtAccessTokenConverter) // OAuth2 JWT 사용
                .tokenEnhancer(tokenEnhancerChain) // token enhancer 설정
                .authenticationManager(authenticationManager)
                .userDetailsService(userDetailsService);
    }

    // client 설정
    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients
                .inMemory()
                .withClient("eagleeye") // client application name
//                .secret(PasswordEncoderFactories.createDelegatingPasswordEncoder().encode("thisissecret")
                .secret(passwordEncoder.encode("thisissecret")) // jdbc 사용 시 secret key
                .authorizedGrantTypes("refresh_token", "password", "client_credentials") // grant type
                .scopes("webclient", "mobileclient"); // scope
    }
}
