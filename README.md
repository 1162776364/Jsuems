## SAML2.0 combines OAuth2.0 demo project.

## Demo item Log in to OKTA using SAML2.0, Generate OAuth token after successful.

## Config
### resource
- key.jks
- /saml-certificate/okta.crt

### application config 
    okta-saml:
        identityprovider:
          entity-id: you-entity-id
          verification.credentials:
              - certificate-location: "classpath:saml-certificate/okta.crt"
          singlesignon.url: you-singlesignon-url
          singlesignon.sign-request: false

## Core code
- WebSecurityConfig
```java
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
```
- Saml2UserDetailsAuthenticationManager
```java
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
```
- CustomUserDetailsServiceImpl
  (You can customize privileges based on data returned by Saml2Authentication)
```java
 @Override
    public UserDetails loadUserByUsername(String username) throws
            UsernameNotFoundException {
        // get user by user name
        User user = new User();
        user.setUsername(username);
//        user = userRepository.findValidUserByName(username);
        return user;
    }
```
