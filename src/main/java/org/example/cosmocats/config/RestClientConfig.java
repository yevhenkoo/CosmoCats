package org.example.cosmocats.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

  @Bean
  public RestClient supplierRestClient(
      @Value("${supplier.service.url}") String supplierServiceUrl) {

    return RestClient.builder().baseUrl(supplierServiceUrl).build();
  }
}
