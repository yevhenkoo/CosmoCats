package org.example.cosmocats.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

  private final SecurityProperties securityProperties;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String configuredApiKey = securityProperties.getApiKey();
    String headerName = securityProperties.getApiKeyHeader();
    String providedKey = request.getHeader(headerName);

    if (!StringUtils.hasText(providedKey)) {
      filterChain.doFilter(request, response);
      return;
    }

    if (configuredApiKey != null && configuredApiKey.equals(providedKey)) {

      var authorities = AuthorityUtils.createAuthorityList("ROLE_API", "ROLE_ADMIN");
      var authentication = new ApiKeyAuthenticationToken(providedKey, authorities);

      authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
      SecurityContextHolder.getContext().setAuthentication(authentication);

    } else {
      writeUnauthorizedResponse(response, "Invalid API Key");
      return;
    }

    filterChain.doFilter(request, response);
  }

  private void writeUnauthorizedResponse(HttpServletResponse response, String message)
      throws IOException {
    response.setStatus(HttpStatus.UNAUTHORIZED.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response
        .getOutputStream()
        .write(
            ("{\"status\": 401, \"message\": \"" + message + "\"}")
                .getBytes(StandardCharsets.UTF_8));
  }
}
