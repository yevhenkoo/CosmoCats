package org.example.cosmocats.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.example.cosmocats.AbstractIT;
import org.example.cosmocats.domain.Category;
import org.example.cosmocats.domain.Product;
import org.example.cosmocats.dto.product.ProductDetailsDto;
import org.example.cosmocats.dto.product.SupplierInfoDto;
import org.example.cosmocats.repository.CategoryRepository;
import org.example.cosmocats.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.github.tomakehurst.wiremock.client.WireMock;

import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@AutoConfigureMockMvc
@DisplayName("Product Controller Integration Tests")
@Tag("product-controller")
class ProductControllerIT extends AbstractIT {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @Autowired private ProductRepository productRepository;
  @Autowired private CategoryRepository categoryRepository;

  @BeforeEach
  void setUp() {
    wireMockServer.resetAll();
    productRepository.deleteAll();
    categoryRepository.deleteAll();
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
        .andExpect(jsonPath("$.errors[*]", hasItem(containsString("name"))));
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
        .andExpect(jsonPath("$.errors[*]", hasItem(containsString("price"))));
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
        .andExpect(jsonPath("$.errors[*]", hasItem(containsString("sku"))));
  }

  @Test
  @DisplayName("GET /products/{id} (Positive): Should return 200 OK and product with WireMock data")
  @SneakyThrows
  void getProductById_shouldReturn200_whenFound_withSupplierInfo() {
    Category category = new Category();
    category.setName("Electronics");
    categoryRepository.save(category);

    String sku = "ELEC-MON-001";
    Product product = new Product();
    product.setName("Starship Monitor");
    product.setCategory(category);
    product.setPrice(500.0);
    product.setSku(sku);
    product.setStockQuantity(5);

    Product savedProduct = productRepository.save(product);
    Long realId = savedProduct.getId();

    SupplierInfoDto supplierInfo = new SupplierInfoDto("Mocked Supplier", "Mock-Country", 3);
    String jsonBody = objectMapper.writeValueAsString(supplierInfo);

    stubFor(
        WireMock.get(urlPathMatching("/suppliers/info/" + sku))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .withBody(jsonBody)));

    mockMvc
        .perform(get("/api/v1/products/{id}", realId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(realId.intValue())))
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
    Category category = new Category();
    category.setName("To Delete");
    categoryRepository.save(category);

    Product product = new Product();
    product.setName("Delete Me");
    product.setPrice(10.0);
    product.setCategory(category);
    Product saved = productRepository.save(product);

    mockMvc
        .perform(delete("/api/v1/products/{id}", saved.getId()))
        .andExpect(status().isNoContent());

    mockMvc.perform(get("/api/v1/products/{id}", saved.getId())).andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("GET /products: Should return 200 OK and list of products")
  @SneakyThrows
  void getAllProducts_shouldReturn200_andList() {
    Category cat = new Category();
    cat.setName("List Cat");
    categoryRepository.save(cat);

    Product p1 = new Product();
    p1.setName("Starship Monitor");
    p1.setPrice(100.0);
    p1.setCategory(cat);
    productRepository.save(p1);

    Product p2 = new Product();
    p2.setName("Galaxy Phone");
    p2.setPrice(200.0);
    p2.setCategory(cat);
    productRepository.save(p2);

    mockMvc
        .perform(get("/api/v1/products"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[*].name", hasItem("Starship Monitor")))
        .andExpect(jsonPath("$[*].name", hasItem("Galaxy Phone")));
  }

  @Test
  @DisplayName("PUT /products/{id}: Should return 200 OK and updated product")
  @SneakyThrows
  void updateProduct_shouldReturn200_whenValid() {
    Category category = new Category();
    category.setName("Original Category");
    categoryRepository.save(category);

    Product product = new Product();
    product.setName("Original Name");
    product.setPrice(50.0);
    product.setCategory(category);
    Product saved = productRepository.save(product);

    ProductDetailsDto updateDto =
        buildValidDto().toBuilder()
            .name("Updated Galaxy")
            .sku("SKU-UPD-001")
            .category("Original Category")
            .build();

    mockMvc
        .perform(
            put("/api/v1/products/{id}", saved.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(saved.getId().intValue())))
        .andExpect(jsonPath("$.name", is("Updated Galaxy")));
  }
}
