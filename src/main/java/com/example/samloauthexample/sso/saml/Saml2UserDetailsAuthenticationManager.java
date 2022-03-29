package com.example.samloauthexample.sso.saml;

import com.example.samloauthexample.service.impl.CustomUserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.saml2.provider.service.authentication.OpenSamlAuthenticationProvider;
import org.springframework.security.saml2.provider.service.authentication.Saml2Authentication;

import java.util.Set;

@Configuration
public class Saml2UserDetailsAuthenticationManager implements AuthenticationManager {

    private CustomUserDetailsServiceImpl customUserDetailsService;
    private JwtAccessTokenConverter jwtAccessTokenConverter;
    private DefaultTokenServices defaultTokenServices;
    private OpenSamlAuthenticationProvider openSamlAuthProvider = new OpenSamlAuthenticationProvider();

    private Set<String> scope = Set.of("read", "write", "user_info");
    private String clientId = "clientId";

    @Autowired
    public Saml2UserDetailsAuthenticationManager(CustomUserDetailsServiceImpl customUserDetailsService,
                                                 JwtAccessTokenConverter jwtAccessTokenConverter,
                                                 DefaultTokenServices defaultTokenServices){
        this.customUserDetailsService = customUserDetailsService;
        this.jwtAccessTokenConverter = jwtAccessTokenConverter;
        this.defaultTokenServices = defaultTokenServices;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        Saml2Authentication saml2AuthenticationResult = (Saml2Authentication) openSamlAuthProvider.authenticate(authentication);
        AuthenticatedPrincipal principal = (AuthenticatedPrincipal) saml2AuthenticationResult.getPrincipal();

        // load SAML authenticated user form local user details
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(principal.getName());

        // generate token
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                new UsernamePasswordAuthenticationToken(userDetails, null,  userDetails.getAuthorities());
        OAuth2Request oAuth2Request = new OAuth2Request(null, clientId,
                userDetails.getAuthorities(), true, scope,
                null, null, null,
                null);
        OAuth2Authentication oAuth2Authentication = new OAuth2Authentication(oAuth2Request, usernamePasswordAuthenticationToken);
        OAuth2AccessToken accessToken = defaultTokenServices.createAccessToken(oAuth2Authentication);
        accessToken = jwtAccessTokenConverter.enhance(accessToken, oAuth2Authentication);

        // generate customize authentication
        return new Saml2WithUserDetailsAuthentication(saml2AuthenticationResult, userDetails, accessToken);
    }
}
