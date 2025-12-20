package org.example.cosmocats.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

  private final String headerName;
  private final AuthenticationManager authenticationManager;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String providedKey = request.getHeader(headerName);

    if (!StringUtils.hasText(providedKey)) {
      filterChain.doFilter(request, response);
      return;
    }

    try {
      var authRequest = new PreAuthenticatedAuthenticationToken(providedKey, null);

      Authentication authResult = authenticationManager.authenticate(authRequest);

      SecurityContextHolder.getContext().setAuthentication(authResult);

    } catch (AuthenticationException e) {
      SecurityContextHolder.clearContext();
    }

    filterChain.doFilter(request, response);
  }
}
