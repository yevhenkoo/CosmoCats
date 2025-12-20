package org.example.cosmocats.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ApiKeyAuthenticationProvider implements AuthenticationProvider {

  private final SecurityProperties securityProperties;

  @Override
  public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    String providedKey = (String) authentication.getPrincipal();
    String validKey = securityProperties.getApiKey();

    if (validKey != null && validKey.equals(providedKey)) {
      return new PreAuthenticatedAuthenticationToken(
          providedKey, null, AuthorityUtils.createAuthorityList("ROLE_API", "ROLE_ADMIN"));
    }

    throw new BadCredentialsException("Invalid API Key");
  }

  @Override
  public boolean supports(Class<?> authentication) {
    return PreAuthenticatedAuthenticationToken.class.isAssignableFrom(authentication);
  }
}
