package org.example.cosmocats.web;

import org.example.cosmocats.AbstractIT;
import org.example.cosmocats.dto.product.ProductDetailsDto;
import org.example.cosmocats.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SecurityIT extends AbstractIT {

  @Autowired private MockMvc mockMvc;

  @Autowired private ProductService productService;

  @Value("${application.security.api-key}")
  private String apiKey;

  @Value("${application.security.api-key-header}")
  private String apiKeyHeader;

  @BeforeEach
  void setUp() {
    // Створюємо продукт, щоб GET запит повертав дані (не пустий список)
    // Це аналог вашого categoryRepository.save(...)
    try {
      ProductDetailsDto product =
          ProductDetailsDto.builder()
              .name("Secured Tuna")
              .description("Top secret")
              .price(100.0)
              .category("Food")
              .sku("SECURE-123")
              .stockQuantity(10)
              .build();
      productService.createProduct(product);
    } catch (Exception e) {
      // Ігноруємо, якщо вже існує (або використовуємо @DirtiesContext)
    }
  }

  @Test
  @DisplayName("Should return 401 when no credentials provided")
  void shouldReturn401_whenNoCredentials() throws Exception {
    mockMvc
        .perform(get("/api/v1/products").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("Should allow access with valid API Key")
  void shouldAllowAccessWithApiKey() throws Exception {
    mockMvc
        .perform(get("/api/v1/products").header(apiKeyHeader, apiKey))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray());
  }

  @Test
  @DisplayName("Should return 401 for invalid API Key")
  void shouldRejectInvalidApiKey() throws Exception {
    mockMvc
        .perform(get("/api/v1/products").header(apiKeyHeader, "invalid-key-meow"))
        .andExpect(status().isUnauthorized())
        // Перевірка повідомлення з вашого фільтра
        .andExpect(jsonPath("$.message").value("Invalid API Key"));
  }

  @Test
  @DisplayName("Should allow access with valid JWT (ROLE_API)")
  void shouldAllowAccessWithJwt() throws Exception {
    mockMvc
        .perform(
            get("/api/v1/products").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_API"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray());
  }

  @Test
  @DisplayName("Should allow access with valid JWT (ROLE_ADMIN)")
  void shouldAllowAccessWithJwtAdmin() throws Exception {
    mockMvc
        .perform(
            get("/api/v1/products")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
        .andExpect(status().isOk());
  }
}
