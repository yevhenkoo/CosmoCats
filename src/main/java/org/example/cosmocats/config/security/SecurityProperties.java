package org.example.cosmocats.config.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "application.security")
public class SecurityProperties {

  public static final String DEFAULT_API_KEY_HEADER = "X-Api-Key";

  private String apiKeyHeader = DEFAULT_API_KEY_HEADER;
  private String apiKey;
}
