package org.example.cosmocats.web;

import org.example.cosmocats.AbstractIT;
import org.example.cosmocats.entity.CategoryEntity;
import org.example.cosmocats.entity.ProductEntity;
import org.example.cosmocats.repository.CategoryRepository;
import org.example.cosmocats.repository.ProductRepository;
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

  @Autowired private ProductRepository productRepository;

  @Autowired private CategoryRepository categoryRepository;

  @Value("${application.security.api-key}")
  private String apiKey;

  @Value("${application.security.api-key-header}")
  private String apiKeyHeader;

  @BeforeEach
  void setUp() {
    productRepository.deleteAll();
    categoryRepository.deleteAll();

    CategoryEntity category = new CategoryEntity();
    category.setName("Food");
    CategoryEntity savedCategory = categoryRepository.save(category);

    ProductEntity product = new ProductEntity();
    product.setName("Secured Tuna");
    product.setDescription("Top secret");
    product.setPrice(100.0);
    product.setSku("SECURE-123");
    product.setStockQuantity(10);
    product.setCategory(savedCategory);

    productRepository.save(product);
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
        .andExpect(status().isUnauthorized());
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
