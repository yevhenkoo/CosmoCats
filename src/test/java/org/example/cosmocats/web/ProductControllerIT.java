package org.example.cosmocats.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.example.cosmocats.AbstractIT;
import org.example.cosmocats.dto.product.ProductDetailsDto;
import org.example.cosmocats.dto.product.SupplierInfoDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@DisplayName("Product Controller Integration Tests")
@Tag("product-controller")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ProductControllerIT extends AbstractIT {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    wireMockServer.resetAll();
  }

  private static ProductDetailsDto buildValidDto() {
    return ProductDetailsDto.builder()
        .name("Galaxy Product")
        .description("A valid description")
        .price(99.99)
        .sku("VALID-SKU-123")
        .category("Valid Category")
        .stockQuantity(10)
        .build();
  }

  @Test
  @DisplayName("POST /products (Positive): Should return 201 Created when data is valid")
  @SneakyThrows
  void createProduct_shouldReturn201_whenValid() {
    mockMvc
        .perform(
            post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(buildValidDto())))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.name", is("Galaxy Product")));
  }

  @Test
  @DisplayName("POST /products (Negative): Should return 400 Bad Request when name is blank")
  @SneakyThrows
  void createProduct_shouldReturn400_whenNameIsBlank() {
    ProductDetailsDto invalidDto = buildValidDto().toBuilder().name("").build();

    mockMvc
        .perform(
            post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status", is(400)))
        .andExpect(jsonPath("$.errors[*]", hasItem(containsString("Field 'name'"))));
  }

  @Test
  @DisplayName("POST /products (Negative): Should return 400 Bad Request when price is negative")
  @SneakyThrows
  void createProduct_shouldReturn400_whenPriceIsNegative() {
    ProductDetailsDto invalidDto = buildValidDto().toBuilder().price(-10.0).build();

    mockMvc
        .perform(
            post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status", is(400)))
        // ВИПРАВЛЕНО: перевірка через поле errors
        .andExpect(jsonPath("$.errors[*]", hasItem(containsString("Field 'price'"))));
  }

  @Test
  @DisplayName("POST /products (Negative): Should return 400 Bad Request when SKU is invalid")
  @SneakyThrows
  void createProduct_shouldReturn400_whenSkuIsInvalid() {
    ProductDetailsDto invalidDto = buildValidDto().toBuilder().sku("invalid sku pattern").build();

    mockMvc
        .perform(
            post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status", is(400)))
        .andExpect(jsonPath("$.errors[*]", hasItem(containsString("Field 'sku'"))));
  }

  @Test
  @DisplayName("GET /products/{id} (Positive): Should return 200 OK and product with WireMock data")
  @SneakyThrows
  void getProductById_shouldReturn200_whenFound_withSupplierInfo() {
    Long id = 1L;
    String sku = "ELEC-MON-001";

    SupplierInfoDto supplierInfo = new SupplierInfoDto("Mocked Supplier", "Mock-Country", 3);
    String jsonBody = objectMapper.writeValueAsString(supplierInfo);

    stubFor(
        com.github.tomakehurst.wiremock.client.WireMock.get(
                urlPathEqualTo("/suppliers/info/" + sku))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .withBody(jsonBody)));

    mockMvc
        .perform(get("/api/v1/products/{id}", id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(1)))
        .andExpect(jsonPath("$.name", is("Starship Monitor")))
        .andExpect(jsonPath("$.supplierName", is("Mocked Supplier")))
        .andExpect(jsonPath("$.supplierCountry", is("Mock-Country")));
  }

  @Test
  @DisplayName("GET /products/{id} (Negative): Should return 404 when product is not found")
  @SneakyThrows
  void getProductById_shouldReturn404_whenNotFound() {
    mockMvc.perform(get("/api/v1/products/999")).andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("DELETE /products/{id}: Should return 204 No Content")
  @SneakyThrows
  void deleteProduct_shouldReturn204() {
    mockMvc.perform(delete("/api/v1/products/2")).andExpect(status().isNoContent());

    mockMvc.perform(get("/api/v1/products/2")).andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("GET /products: Should return 200 OK and list of products")
  @SneakyThrows
  void getAllProducts_shouldReturn200_andList() {
    mockMvc
        .perform(get("/api/v1/products"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[0].name", is("Starship Monitor")));
  }

  @Test
  @DisplayName("PUT /products/{id}: Should return 200 OK and updated product")
  @SneakyThrows
  void updateProduct_shouldReturn200_whenValid() {
    Long id = 1L;
    ProductDetailsDto updateDto =
        buildValidDto().toBuilder().name("Updated Galaxy").sku("SKU-UPD-001").build();

    mockMvc
        .perform(
            put("/api/v1/products/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(1)))
        .andExpect(jsonPath("$.name", is("Updated Galaxy")));
  }
}
