package com.example.samloauthexample.sso.saml;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.saml2.provider.service.authentication.Saml2Authentication;

import java.util.Collection;

public class Saml2WithUserDetailsAuthentication implements Authentication{

    private UserDetails userDetails;
    private Saml2Authentication saml2Authentication;
    private OAuth2AccessToken accessToken;

    public Saml2WithUserDetailsAuthentication(Saml2Authentication saml2Authentication, UserDetails userDetails,
                                              OAuth2AccessToken accessToken) {
        this.saml2Authentication = saml2Authentication;
        this.userDetails = userDetails;
        this.accessToken = accessToken;
    }

    @Override
    public String getName() {
        return this.userDetails.getUsername();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.userDetails.getAuthorities();
    }

    @Override
    public Object getCredentials() {
        return this.saml2Authentication;
    }

    @Override
    public Object getDetails() {
        return this.accessToken;
    }

    @Override
    public Object getPrincipal() {
        return this.userDetails;
    }

    @Override
    public boolean isAuthenticated() {
        return true;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
    }
}
