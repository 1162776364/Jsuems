package com.example.samloauthexample.sso.config;

import com.example.samloauthexample.model.User;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;

import java.util.LinkedHashMap;
import java.util.Map;

public class AwareJwtAccessTokenConverter extends JwtAccessTokenConverter {
    @Override
    public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {
        Map<String, Object> info = new LinkedHashMap<>(accessToken.getAdditionalInformation());
        Object principal = authentication.getPrincipal();
        if (principal instanceof User) {
            User user = (User) authentication.getPrincipal();
            info.put("id", user.getId());
            info.put("displayName", user.getDisplayName());
            info.put("ipAddress", user.getIpAddress());
            if (user.getLoggedInAt() != null) {
                info.put("loggedInAt", user.getLoggedInAt().toString());
            }
        }
        info.put("name", authentication.getName());
        DefaultOAuth2AccessToken customAccessToken = new DefaultOAuth2AccessToken(accessToken);
        customAccessToken.setAdditionalInformation(info);
        return super.enhance(customAccessToken, authentication);
    }

    @Override
    public OAuth2Authentication extractAuthentication(Map<String, ?> map) {
        return super.extractAuthentication(map);
    }
}
