package org.example.cosmocats.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.cosmocats.client.SupplierClient;
import org.example.cosmocats.domain.Category;
import org.example.cosmocats.domain.Product;
import org.example.cosmocats.dto.product.ProductDetailsDto;
import org.example.cosmocats.dto.product.ProductDetailsEntry;
import org.example.cosmocats.dto.product.SupplierInfoDto;
import org.example.cosmocats.repository.CategoryRepository;
import org.example.cosmocats.repository.ProductRepository;
import org.example.cosmocats.service.ProductService;
import org.example.cosmocats.service.mapper.ProductMapper;
import org.example.cosmocats.web.exceptions.ProductNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

  private final ProductRepository productRepository;
  private final CategoryRepository categoryRepository;
  private final ProductMapper productMapper;
  private final SupplierClient supplierClient;

  @Override
  @Transactional
  public ProductDetailsEntry createProduct(ProductDetailsDto productDto) {
    log.info("Creating product: {}", productDto);

    Category category =
        categoryRepository
            .findByName(productDto.getCategory())
            .orElseGet(
                () -> {
                  Category newCat = new Category();
                  newCat.setName(productDto.getCategory());
                  return categoryRepository.save(newCat);
                });

    Product product = productMapper.toProductEntity(productDto);
    product.setCategory(category);

    Product savedProduct = productRepository.save(product);
    log.debug("Product saved in DB with id: {}", savedProduct.getId());

    return productMapper.toProductDetailsEntry(savedProduct);
  }

  @Override
  @Transactional(readOnly = true)
  public List<ProductDetailsEntry> getAllProducts() {
    log.info("Fetching all products from DB");
    return productRepository.findAll().stream().map(productMapper::toProductDetailsEntry).toList();
  }

  @Override
  @Transactional(readOnly = true)
  public ProductDetailsEntry getProductById(Long id) {
    log.info("Fetching product by id: {}", id);

    Product product =
        productRepository
            .findById(id)
            .orElseThrow(
                () -> new ProductNotFoundException("Product with id " + id + " not found"));

    ProductDetailsEntry productDto = productMapper.toProductDetailsEntry(product);

    try {
      SupplierInfoDto supplierInfo = supplierClient.getSupplierInfo(product.getSku());
      if (supplierInfo != null) {
        productDto.setSupplierName(supplierInfo.getSupplierName());
        productDto.setSupplierCountry(supplierInfo.getCountry());
      }
    } catch (Exception e) {
      log.warn("Failed to fetch supplier info for sku: {}", product.getSku());
    }

    return productDto;
  }

  @Override
  @Transactional
  public ProductDetailsEntry updateProduct(Long id, ProductDetailsDto productDto) {
    log.info("Updating product with id: {}", id);

    Product existingProduct =
        productRepository
            .findById(id)
            .orElseThrow(
                () -> new ProductNotFoundException("Product with id " + id + " not found"));

    existingProduct.setName(productDto.getName());
    existingProduct.setDescription(productDto.getDescription());
    existingProduct.setPrice(productDto.getPrice());
    existingProduct.setSku(productDto.getSku());
    existingProduct.setStockQuantity(productDto.getStockQuantity());

    // Оновлюємо категорію, якщо змінилася
    if (!existingProduct.getCategory().getName().equals(productDto.getCategory())) {
      Category category =
          categoryRepository
              .findByName(productDto.getCategory())
              .orElseGet(
                  () -> {
                    Category newCat = new Category();
                    newCat.setName(productDto.getCategory());
                    return categoryRepository.save(newCat);
                  });
      existingProduct.setCategory(category);
    }

    Product saved = productRepository.save(existingProduct);

    return productMapper.toProductDetailsEntry(saved);
  }

  @Override
  @Transactional
  public void deleteProduct(Long id) {
    log.info("Deleting product with id: {}", id);
    if (productRepository.existsById(id)) {
      productRepository.deleteById(id);
    } else {
      log.warn("Product with id {} not found during delete", id);
    }
  }
}
