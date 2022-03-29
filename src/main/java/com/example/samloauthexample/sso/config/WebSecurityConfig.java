package com.example.samloauthexample.sso.config;


import com.example.samloauthexample.service.impl.CustomUserDetailsServiceImpl;
import com.example.samloauthexample.sso.saml.Saml2AuthenticationSuccessHandler;
import com.example.samloauthexample.sso.saml.Saml2UserDetailsAuthenticationManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.saml2.provider.service.metadata.OpenSamlMetadataResolver;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.servlet.filter.Saml2WebSsoAuthenticationFilter;
import org.springframework.security.saml2.provider.service.web.DefaultRelyingPartyRegistrationResolver;
import org.springframework.security.saml2.provider.service.web.Saml2MetadataFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.servlet.http.HttpServletRequest;

@Configuration
@Order(1)
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    private RelyingPartyRegistrationRepository relyingPartyRegistrationRepository;
    private CustomUserDetailsServiceImpl customUserDetailsService;
    private Saml2AuthenticationSuccessHandler saml2AuthenticationSuccessHandler;
    private JwtAccessTokenConverter jwtAccessTokenConverter;
    private DefaultTokenServices defaultTokenServices;

    @Autowired
    public WebSecurityConfig(RelyingPartyRegistrationRepository relyingPartyRegistrationRepository,
                             CustomUserDetailsServiceImpl customUserDetailsService,
                             Saml2AuthenticationSuccessHandler saml2AuthenticationSuccessHandler,
                             JwtAccessTokenConverter jwtAccessTokenConverter,
                             DefaultTokenServices defaultTokenServices){
        this.relyingPartyRegistrationRepository = relyingPartyRegistrationRepository;
        this.saml2AuthenticationSuccessHandler = saml2AuthenticationSuccessHandler;
        this.customUserDetailsService = customUserDetailsService;
        this.jwtAccessTokenConverter = jwtAccessTokenConverter;
        this.defaultTokenServices = defaultTokenServices;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests(authorize ->
                        authorize.antMatchers("/").permitAll().
                        anyRequest().authenticated()
                )
                .saml2Login(
                   saml2 -> saml2
                           .authenticationManager(new Saml2UserDetailsAuthenticationManager(
                                   customUserDetailsService , jwtAccessTokenConverter, defaultTokenServices))
                           .loginPage("/")
                           .successHandler(saml2AuthenticationSuccessHandler)
                )
                .logout();

        // add auto-generation of ServiceProvider Metadata
        Converter<HttpServletRequest, RelyingPartyRegistration> relyingPartyRegistrationResolver
                = new DefaultRelyingPartyRegistrationResolver(relyingPartyRegistrationRepository);
        Saml2MetadataFilter filter
                = new Saml2MetadataFilter(relyingPartyRegistrationResolver, new OpenSamlMetadataResolver());
        http.addFilterBefore(filter, Saml2WebSsoAuthenticationFilter.class);
        http.cors().configurationSource(configurationSource());
    }

    private CorsConfigurationSource configurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOriginPattern("*");
        config.setAllowCredentials(true);
        config.addAllowedHeader("X-Requested-With");
        config.addAllowedHeader("Content-Type");
        config.addAllowedMethod(HttpMethod.POST);
        source.registerCorsConfiguration("/logout", config);
        return source;
    }

    @Override
    public void configure(final WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/css/**", "/images/**", "/js/**", "/actuator/**");
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManager() throws Exception {
        return super.authenticationManager();
    }
}
