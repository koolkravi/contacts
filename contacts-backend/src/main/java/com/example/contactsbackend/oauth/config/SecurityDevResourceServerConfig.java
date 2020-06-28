// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.example.contactsbackend.oauth.config;

import java.net.MalformedURLException;
import java.net.URL;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.DefaultAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.IssuerClaimVerifier;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtClaimsSetVerifier;
import org.springframework.security.oauth2.provider.token.store.jwk.JwkTokenStore;

import com.microsoft.azure.spring.autoconfigure.aad.AADAuthenticationFilter;

@Profile("dev")
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityDevResourceServerConfig extends ResourceServerConfigurerAdapter {

    @Value("${security.oauth2.resource.jwt.key-uri}")
    private String keySetUri;

    @Value("${security.oauth2.resource.id}")
    private String resourceId;

    @Value("${security.oauth2.issuer}")
    private String issuer;

    @Value("${security.oauth2.scope.access-as-user}")
    private String accessAsUserScope;

    private final String AAD_SCOPE_CLAIM = "scp";
    @Autowired
   private AADAuthenticationFilter aadAuthFilter;

//    @Autowired
//    private AADAppRoleStatelessAuthenticationFilter appRoleAuthFilter;
//    
    @Override
    public void configure(HttpSecurity http) throws Exception {
    	  http.cors().disable();
	        http.csrf().disable();
        http
                
               // .addFilterBefore(appRoleAuthFilter, UsernamePasswordAuthenticationFilter.class);
                .authorizeRequests()
                
                
                .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .antMatchers("/**")
                .access("#oauth2.hasScope('" + accessAsUserScope + "')")
                .antMatchers(HttpMethod.OPTIONS, "/**").permitAll();
              // http.addFilterBefore(aadAuthFilter, Prea.class);
                ; // required scope to access /api URL
    }

    @Bean
    public TokenStore tokenStore() {
        JwkTokenStore jwkTokenStore = new JwkTokenStore(keySetUri, accessTokenConverter(), issuerClaimVerifier());
        return jwkTokenStore;
    }

    @Bean
    public JwtAccessTokenConverter accessTokenConverter() {
        JwtAccessTokenConverter jwtConverter = new JwtAccessTokenConverter();

        DefaultAccessTokenConverter accessTokenConverter = new DefaultAccessTokenConverter();
        accessTokenConverter.setScopeAttribute(AAD_SCOPE_CLAIM);
        //accessTokenConverter.
        jwtConverter.setAccessTokenConverter(accessTokenConverter);

        return jwtConverter;
    }

    @Override
    public void configure(ResourceServerSecurityConfigurer resources){
        resources.resourceId(resourceId);
    }
   
    @Bean
    public JwtClaimsSetVerifier issuerClaimVerifier() {
        try {
            return new IssuerClaimVerifier(new URL(issuer));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}