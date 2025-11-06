package org.example.cosmocats.service;

import org.example.cosmocats.client.SupplierClient;
import org.example.cosmocats.config.MappersTestConfiguration;
import org.example.cosmocats.dto.product.ProductDetailsDto;
import org.example.cosmocats.dto.product.ProductDetailsEntry;
import org.example.cosmocats.dto.product.SupplierInfoDto;
import org.example.cosmocats.service.impl.ProductServiceImpl;
import org.example.cosmocats.web.exceptions.ProductNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {ProductServiceImpl.class})
@Import(MappersTestConfiguration.class)
@DisplayName("Product Service Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ProductServiceTest {

  @MockBean private SupplierClient supplierClient;

  @Autowired private ProductServiceImpl productService;

  private ProductDetailsDto buildCreateDto() {
    return ProductDetailsDto.builder()
        .name("New Product")
        .description("Desc")
        .price(100.0)
        .sku("NEW-SKU-001")
        .category("Gadgets")
        .stockQuantity(10)
        .build();
  }

  @Test
  @Order(1)
  @DisplayName("getAllProducts: Should return a list of all products (from init())")
  void testGetAllProducts() {
    List<ProductDetailsEntry> results = productService.getAllProducts();

    assertNotNull(results);
    assertEquals(2, results.size());
  }

  @Test
  @Order(2)
  @DisplayName("getProductById: Must return product with supplier information")
  void testGetProductById_shouldReturnProductWithSupplierInfo() {
    Long id = 1L;
    String sku = "ELEC-MON-001";
    SupplierInfoDto supplierInfo = new SupplierInfoDto("Mars Supplies", "Mars", 5);

    when(supplierClient.getSupplierInfo(sku)).thenReturn(supplierInfo);

    ProductDetailsEntry result = productService.getProductById(id);

    assertNotNull(result);
    assertEquals(id, result.getId());
    assertEquals(supplierInfo.getSupplierName(), result.getSupplierName());
    assertEquals(supplierInfo.getCountry(), result.getSupplierCountry());
  }

  @Test
  @Order(3)
  @DisplayName("createProduct: Must create and return a new product")
  void testCreateProduct_shouldReturnCreatedProduct() {
    ProductDetailsDto createDto = buildCreateDto();

    ProductDetailsEntry result = productService.createProduct(createDto);

    assertNotNull(result);
    assertEquals("New Product", result.getName());
    assertNotNull(result.getId());
    assertEquals(3, productService.getAllProducts().size());
  }

  @Test
  @Order(4)
  @DisplayName("updateProduct: Need to update and return the product")
  void testUpdateProduct_shouldReturnUpdatedProduct() {
    Long id = 1L;
    ProductDetailsDto updateDto =
        ProductDetailsDto.builder()
            .name("Updated Name")
            .description("Updated Desc")
            .price(1500.0)
            .sku("UPD-SKU-001")
            .category("Updated Category")
            .stockQuantity(5)
            .build();

    ProductDetailsEntry result = productService.updateProduct(id, updateDto);

    assertNotNull(result);
    assertEquals("Updated Name", result.getName());

    ProductDetailsEntry fetchedAfterUpdate = productService.getProductById(id);
    assertEquals("Updated Name", fetchedAfterUpdate.getName());
  }

  @Test
  @Order(5)
  @DisplayName("deleteProduct: Should delete an existing product")
  void testDeleteProduct_shouldRemoveProduct() {
    Long id = 1L;
    assertEquals(3, productService.getAllProducts().size());

    productService.deleteProduct(id);

    assertThrows(
        ProductNotFoundException.class,
        () -> {
          productService.getProductById(id);
        });
    assertEquals(2, productService.getAllProducts().size());
  }

  @Test
  @Order(6)
  @DisplayName("getProductById: Should throw ProductNotFoundException if product not found")
  void testGetProductById_shouldThrowExceptionWhenNotFound() {
    Exception exception =
        assertThrows(
            ProductNotFoundException.class,
            () -> {
              productService.getProductById(999L);
            });

    assertEquals("Product with id 999 not found", exception.getMessage());
  }

  @Test
  @Order(7)
  @DisplayName("deleteProduct: Should exit silently if product not found")
  void testDeleteProduct_shouldDoNothingWhenNotFound() {
    Long id = 999L;

    assertEquals(2, productService.getAllProducts().size());

    assertDoesNotThrow(() -> productService.deleteProduct(id));

    assertEquals(2, productService.getAllProducts().size());
  }
}
