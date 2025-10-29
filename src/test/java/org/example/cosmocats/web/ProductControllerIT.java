package org.example.cosmocats.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.cosmocats.AbstractIt;
import org.example.cosmocats.dto.product.ProductDetailsDto;
import org.example.cosmocats.dto.product.SupplierInfoDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@DisplayName("Product Controller Integration Tests")
class ProductControllerIT extends AbstractIt {

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private ProductDetailsDto validDto;

    @BeforeEach
    void setUp() {
        wireMockServer.resetAll();

        validDto = ProductDetailsDto.builder()
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
    void createProduct_shouldReturn201_whenValid() throws Exception {
        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Galaxy Product")));
    }

    @Test
    @DisplayName("POST /products (Negative): Should return 400 Bad Request when name is blank")
    void createProduct_shouldReturn400_whenNameIsBlank() throws Exception {
        ProductDetailsDto invalidDto = validDto.toBuilder()
                .name("")
                .build();

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /products (Negative): Should return 400 Bad Request when price is negative")
    void createProduct_shouldReturn400_whenPriceIsNegative() throws Exception {
        ProductDetailsDto invalidDto = validDto.toBuilder()
                .price(-10.0)
                .build();

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /products (Negative): Should return 400 Bad Request when SKU is invalid")
    void createProduct_shouldReturn400_whenSkuIsInvalid() throws Exception {
        ProductDetailsDto invalidDto = validDto.toBuilder()
                .sku("invalid sku pattern")
                .build();

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /products/{id} (Positive): Should return 200 OK and product with WireMock data")
    void getProductById_shouldReturn200_whenFound_withSupplierInfo() throws Exception {

        Long id = 1L;
        String sku = "ELEC-MON-001";

        SupplierInfoDto supplierInfo = new SupplierInfoDto("Mocked Supplier", "Mock-Country", 3);
        String jsonBody = objectMapper.writeValueAsString(supplierInfo);

        stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(urlEqualTo("/suppliers/info/" + sku))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(jsonBody)));

        mockMvc.perform(get("/api/v1/products/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Starship Monitor")))
                .andExpect(jsonPath("$.supplierName", is("Mocked Supplier")))
                .andExpect(jsonPath("$.supplierCountry", is("Mock-Country")));
    }

    @Test
    @DisplayName("GET /products/{id} (Negative): Should return 404 when product is not found")
    void getProductById_shouldReturn404_whenNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/products/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /products/{id}: Should return 204 No Content")
    void deleteProduct_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/v1/products/2"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/products/2"))
                .andExpect(status().isNotFound());
    }
    @Test
    @DisplayName("GET /products: Should return 200 OK and list of products")
    void getAllProducts_shouldReturn200_andList() throws Exception {
        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("Starship Monitor")));
    }

    @Test
    @DisplayName("PUT /products/{id}: Should return 200 OK and updated product")
    void updateProduct_shouldReturn200_whenValid() throws Exception {
        Long id = 1L;
        ProductDetailsDto updateDto = validDto.toBuilder()
                .name("Updated Galaxy")
                .sku("SKU-UPD-001")
                .build();

        mockMvc.perform(put("/api/v1/products/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Updated Galaxy")));
    }
}
