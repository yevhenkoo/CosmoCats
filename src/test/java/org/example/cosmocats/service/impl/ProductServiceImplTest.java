package org.example.cosmocats.service.impl;

import org.example.cosmocats.client.SupplierClient;
import org.example.cosmocats.config.MappersTestConfiguration;
import org.example.cosmocats.dto.product.ProductDetailsDto;
import org.example.cosmocats.dto.product.ProductDetailsEntry;
import org.example.cosmocats.dto.product.SupplierInfoDto;
import org.example.cosmocats.web.exceptions.ProductNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;


@SpringBootTest(classes = {ProductServiceImpl.class})
@Import({MappersTestConfiguration.class, ProductServiceImplTest.MockClientConfig.class})

@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("Product Service Tests (Slice)")
class ProductServiceImplTest {

    @TestConfiguration
    static class MockClientConfig {
        @Bean
        public SupplierClient supplierClient() {
            return Mockito.mock(SupplierClient.class);
        }
    }

    @Autowired
    private SupplierClient supplierClient;

    @Autowired
    private ProductServiceImpl productService;

    private SupplierInfoDto supplierInfo;

    @BeforeEach
    void setUp() {
        supplierInfo = new SupplierInfoDto("Mars Supplies", "Mars", 5);
    }

    @Test
    @DisplayName("createProduct: Must create and return a new product")
    void createProduct_shouldReturnCreatedProduct() {
        ProductDetailsDto createDto = ProductDetailsDto.builder()
                .name("New Product")
                .description("Desc")
                .price(100.0)
                .sku("NEW-SKU-001")
                .category("Gadgets")
                .stockQuantity(10)
                .build();

        ProductDetailsEntry result = productService.createProduct(createDto);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("New Product");
        assertThat(result.getId()).isNotNull();
        assertThat(productService.getAllProducts()).hasSize(3);
    }

    @Test
    @DisplayName("getAllProducts: Should return a list of all products (from init())")
    void getAllProducts_shouldReturnAllProducts() {

        List<ProductDetailsEntry> results = productService.getAllProducts();

        assertThat(results).isNotNull();
        assertThat(results).hasSize(2);
    }

    @Test
    @DisplayName("getProductById: Must return product with supplier information")
    void getProductById_shouldReturnProductWithSupplierInfo() {

        Long id = 1L;
        String sku = "ELEC-MON-001";

        when(supplierClient.getSupplierInfo(sku)).thenReturn(supplierInfo);

        ProductDetailsEntry result = productService.getProductById(id);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getSupplierName()).isEqualTo(supplierInfo.getSupplierName());
        assertThat(result.getSupplierCountry()).isEqualTo(supplierInfo.getCountry());
    }

    @Test
    @DisplayName("getProductById: Should throw ProductNotFoundException if product not found")
    void getProductById_shouldThrowExceptionWhenNotFound() {

        Exception exception = assertThrows(ProductNotFoundException.class, () -> {
            productService.getProductById(999L);
        });

        assertThat(exception.getMessage()).isEqualTo("Product with id 999 not found");
    }

    @Test
    @DisplayName("updateProduct: Need to update and return the product")
    void updateProduct_shouldReturnUpdatedProduct() {
        // Arrange
        Long id = 1L;
        ProductDetailsDto updateDto = ProductDetailsDto.builder()
                .name("Updated Name")
                .description("Updated Desc")
                .price(1500.0)
                .sku("UPD-SKU-001")
                .category("Updated Category")
                .stockQuantity(5)
                .build();

        ProductDetailsEntry result = productService.updateProduct(id, updateDto);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Updated Name");

        ProductDetailsEntry fetchedAfterUpdate = productService.getProductById(id);
        assertThat(fetchedAfterUpdate.getName()).isEqualTo("Updated Name");
    }

    @Test
    @DisplayName("deleteProduct: Should delete an existing product")
    void deleteProduct_shouldRemoveProduct() {

        Long id = 1L;
        assertThat(productService.getAllProducts()).hasSize(2);

        productService.deleteProduct(id);

        assertThrows(ProductNotFoundException.class, () -> {
            productService.getProductById(id);
        });
        assertThat(productService.getAllProducts()).hasSize(1);
    }

    @Test
    @DisplayName("deleteProduct: Should exit silently if product not found")
    void deleteProduct_shouldDoNothingWhenNotFound() {

        Long id = 999L;
        assertThat(productService.getAllProducts()).hasSize(2);

        assertDoesNotThrow(() -> productService.deleteProduct(id));
        assertThat(productService.getAllProducts()).hasSize(2);
    }
}