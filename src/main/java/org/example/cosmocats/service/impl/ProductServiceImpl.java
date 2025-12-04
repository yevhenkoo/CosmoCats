package org.example.cosmocats.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.cosmocats.client.SupplierClient;
import org.example.cosmocats.dto.product.ProductDetailsDto;
import org.example.cosmocats.dto.product.ProductDetailsEntry;
import org.example.cosmocats.dto.product.SupplierInfoDto;
import org.example.cosmocats.entity.CategoryEntity;
import org.example.cosmocats.entity.ProductEntity;
import org.example.cosmocats.web.exceptions.CosmoCatsPersistenceException;
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
    try {
      CategoryEntity category =
          categoryRepository
              .findByName(productDto.getCategory())
              .orElseGet(
                  () -> {
                    CategoryEntity newCat = new CategoryEntity();
                    newCat.setName(productDto.getCategory());
                    return categoryRepository.save(newCat);
                  });

      ProductEntity product = productMapper.toProductEntity(productDto);
      product.setCategory(category);

      ProductEntity savedProduct = productRepository.save(product);
      log.debug("Product saved in DB with id: {}", savedProduct.getId());

      return productMapper.toProductDetailsEntry(savedProduct);

    } catch (Exception e) {
      log.error("Error creating product: {}", productDto.getName(), e);
      throw new CosmoCatsPersistenceException(
          "Failed to create product: " + productDto.getName(), e);
    }
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

    ProductEntity product =
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
    try {
      ProductEntity existingProduct =
          productRepository
              .findById(id)
              .orElseThrow(
                  () -> new ProductNotFoundException("Product with id " + id + " not found"));

      existingProduct.setName(productDto.getName());
      existingProduct.setDescription(productDto.getDescription());
      existingProduct.setPrice(productDto.getPrice());
      existingProduct.setSku(productDto.getSku());
      existingProduct.setStockQuantity(productDto.getStockQuantity());

      if (!existingProduct.getCategory().getName().equals(productDto.getCategory())) {
        CategoryEntity category =
            categoryRepository
                .findByName(productDto.getCategory())
                .orElseGet(
                    () -> {
                      CategoryEntity newCat = new CategoryEntity();
                      newCat.setName(productDto.getCategory());
                      return categoryRepository.save(newCat);
                    });
        existingProduct.setCategory(category);
      }

      ProductEntity saved = productRepository.save(existingProduct);
      return productMapper.toProductDetailsEntry(saved);

    } catch (ProductNotFoundException e) {
      throw e;
    } catch (Exception e) {

      log.error("Error updating product with id: {}", id, e);
      throw new CosmoCatsPersistenceException("Failed to update product with id: " + id, e);
    }
  }

  @Override
  @Transactional
  public void deleteProduct(Long id) {
    log.info("Deleting product with id: {}", id);
    try {
      if (productRepository.existsById(id)) {
        productRepository.deleteById(id);
      } else {
        log.warn("Product with id {} not found during delete", id);
      }
    } catch (Exception e) {
      log.error("Error deleting product with id: {}", id, e);
      throw new CosmoCatsPersistenceException("Failed to delete product with id: " + id, e);
    }
  }
}
