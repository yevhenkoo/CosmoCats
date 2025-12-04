package org.example.cosmocats.service;

import org.example.cosmocats.client.SupplierClient;
import org.example.cosmocats.entity.CategoryEntity;
import org.example.cosmocats.entity.ProductEntity;
import org.example.cosmocats.dto.product.ProductDetailsDto;
import org.example.cosmocats.dto.product.ProductDetailsEntry;
import org.example.cosmocats.dto.product.SupplierInfoDto;
import org.example.cosmocats.repository.CategoryRepository;
import org.example.cosmocats.repository.ProductRepository;
import org.example.cosmocats.service.impl.ProductServiceImpl;
import org.example.cosmocats.service.mapper.ProductMapper;
import org.example.cosmocats.web.exceptions.ProductNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Product Service Unit Tests")
class ProductServiceTest {

  @Mock private ProductRepository productRepository;
  @Mock private CategoryRepository categoryRepository;
  @Mock private SupplierClient supplierClient;
  @Mock private ProductMapper productMapper;

  @InjectMocks private ProductServiceImpl productService;

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
  @DisplayName("getAllProducts: Should return list of products")
  void testGetAllProducts() {

    ProductEntity product = new ProductEntity();
    product.setId(1L);

    ProductDetailsEntry entry = new ProductDetailsEntry();
    entry.setName("Test Product");

    when(productRepository.findAll()).thenReturn(List.of(product));
    when(productMapper.toProductDetailsEntry(product)).thenReturn(entry);

    List<ProductDetailsEntry> results = productService.getAllProducts();

    assertNotNull(results);
    assertEquals(1, results.size());
    verify(productRepository).findAll();
  }

  @Test
  @DisplayName("getProductById: Must return product with supplier information")
  void testGetProductById_shouldReturnProductWithSupplierInfo() {
    Long id = 1L;
    String sku = "ELEC-MON-001";

    ProductEntity product = new ProductEntity();
    product.setId(id);
    product.setSku(sku);

    ProductDetailsEntry entry = new ProductDetailsEntry();
    entry.setId(id);

    SupplierInfoDto supplierInfo = new SupplierInfoDto("Mars Supplies", "Mars", 5);

    when(productRepository.findById(id)).thenReturn(Optional.of(product));
    when(productMapper.toProductDetailsEntry(product)).thenReturn(entry);
    when(supplierClient.getSupplierInfo(sku)).thenReturn(supplierInfo);

    ProductDetailsEntry result = productService.getProductById(id);

    assertNotNull(result);
    assertEquals(id, result.getId());
    assertEquals("Mars Supplies", result.getSupplierName());
  }

  @Test
  @DisplayName("createProduct: Must create and return a new product")
  void testCreateProduct_shouldReturnCreatedProduct() {
    ProductDetailsDto createDto = buildCreateDto();

    CategoryEntity category = new CategoryEntity();
    category.setName("Gadgets");

    ProductEntity mappedProduct = new ProductEntity();
    mappedProduct.setName("New Product");

    ProductEntity savedProduct = new ProductEntity();
    savedProduct.setId(10L);
    savedProduct.setName("New Product");

    ProductDetailsEntry expectedResult = new ProductDetailsEntry();
    expectedResult.setId(10L);
    expectedResult.setName("New Product");

    when(categoryRepository.findByName(anyString())).thenReturn(Optional.of(category));

    when(productMapper.toProductEntity(createDto)).thenReturn(mappedProduct);
    when(productRepository.save(any(ProductEntity.class))).thenReturn(savedProduct);
    when(productMapper.toProductDetailsEntry(savedProduct)).thenReturn(expectedResult);

    ProductDetailsEntry result = productService.createProduct(createDto);

    assertNotNull(result);
    assertEquals(10L, result.getId());
    assertEquals("New Product", result.getName());
    verify(productRepository).save(any(ProductEntity.class));
  }

  @Test
  @DisplayName("updateProduct: Need to update and return the product")
  void testUpdateProduct_shouldReturnUpdatedProduct() {
    Long id = 1L;
    ProductDetailsDto updateDto =
        buildCreateDto().toBuilder().name("Updated Name").category("Updated Category").build();

    ProductEntity existingProduct = new ProductEntity();
    existingProduct.setId(id);
    existingProduct.setName("Old Name");

    CategoryEntity oldCategory = new CategoryEntity();
    oldCategory.setName("Old Category");
    existingProduct.setCategory(oldCategory);

    CategoryEntity newCategory = new CategoryEntity();
    newCategory.setName("Updated Category");

    ProductEntity updatedProduct = new ProductEntity();
    updatedProduct.setId(id);
    updatedProduct.setName("Updated Name");

    ProductDetailsEntry entry = new ProductDetailsEntry();
    entry.setName("Updated Name");

    when(productRepository.findById(id)).thenReturn(Optional.of(existingProduct));
    when(categoryRepository.findByName("Updated Category")).thenReturn(Optional.of(newCategory));

    when(productRepository.save(existingProduct)).thenReturn(updatedProduct);
    when(productMapper.toProductDetailsEntry(updatedProduct)).thenReturn(entry);

    ProductDetailsEntry result = productService.updateProduct(id, updateDto);

    assertNotNull(result);
    assertEquals("Updated Name", result.getName());
  }

  @Test
  @DisplayName("deleteProduct: Should delete an existing product")
  void testDeleteProduct_shouldRemoveProduct() {
    Long id = 1L;
    when(productRepository.existsById(id)).thenReturn(true);

    productService.deleteProduct(id);

    verify(productRepository).deleteById(id);
  }

  @Test
  @DisplayName("getProductById: Should throw ProductNotFoundException if product not found")
  void testGetProductById_shouldThrowExceptionWhenNotFound() {
    Long id = 999L;
    when(productRepository.findById(id)).thenReturn(Optional.empty());

    Exception exception =
        assertThrows(
            ProductNotFoundException.class,
            () -> {
              productService.getProductById(id);
            });

    assertTrue(exception.getMessage().contains("not found"));
  }

  @Test
  @DisplayName("deleteProduct: Should exit silently if product not found")
  void testDeleteProduct_shouldDoNothingWhenNotFound() {
    Long id = 999L;
    when(productRepository.existsById(id)).thenReturn(false);
    assertDoesNotThrow(() -> productService.deleteProduct(id));
    verify(productRepository, never()).deleteById(id);
  }
}
