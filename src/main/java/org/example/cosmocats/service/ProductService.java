package org.example.cosmocats.service;

import org.example.cosmocats.dto.product.ProductDetailsDto;
import org.example.cosmocats.dto.product.ProductDetailsEntry;

import java.util.List;

public interface ProductService {
  ProductDetailsEntry createProduct(ProductDetailsDto productDto);

  List<ProductDetailsEntry> getAllProducts();

  ProductDetailsEntry getProductById(Long id);

  ProductDetailsEntry updateProduct(Long id, ProductDetailsDto productDto);

  void deleteProduct(Long id);
}
