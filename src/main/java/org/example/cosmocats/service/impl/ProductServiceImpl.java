package org.example.cosmocats.service.impl;

import jakarta.annotation.PostConstruct;
import org.example.cosmocats.client.SupplierClient;
import org.example.cosmocats.domain.Category;
import org.example.cosmocats.domain.Product;
import org.example.cosmocats.dto.product.ProductDetailsDto;
import org.example.cosmocats.dto.product.ProductDetailsEntry;
import org.example.cosmocats.dto.product.SupplierInfoDto;
import org.example.cosmocats.web.exceptions.ProductNotFoundException;
import org.example.cosmocats.service.ProductService;
import org.example.cosmocats.service.mapper.ProductMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ProductServiceImpl implements ProductService {

    private final Map<Long, Product> productStore = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(0);
    private final ProductMapper productMapper;
    private final SupplierClient supplierClient;

    public ProductServiceImpl(ProductMapper productMapper, SupplierClient supplierClient) {
        this.productMapper = productMapper;
        this.supplierClient = supplierClient;
    }

    @PostConstruct
    public void init() {
        // Додамо декілька продуктів для тестування
        Category electronics = new Category();
        electronics.setId(1L);
        electronics.setName("Electronics");

        Product product1 = new Product();
        product1.setId(sequence.incrementAndGet());
        product1.setName("Starship Monitor");
        product1.setPrice(1999.99);
        product1.setCategory(electronics);
        product1.setSku("ELEC-MON-001");
        product1.setStockQuantity(10);
        productStore.put(product1.getId(), product1);

        Product product2 = new Product();
        product2.setId(sequence.incrementAndGet());
        product2.setName("Galaxy Smartphone");
        product2.setPrice(899.50);
        product2.setCategory(electronics);
        product2.setSku("ELEC-PHONE-007");
        product2.setStockQuantity(50);
        productStore.put(product2.getId(), product2);
    }

    @Override
    public ProductDetailsEntry createProduct(ProductDetailsDto productDto) {
        Product product = productMapper.toProductEntity(productDto);
        product.setId(sequence.incrementAndGet());
        Category mockCategory = new Category();
        mockCategory.setName(productDto.getCategory());
        product.setCategory(mockCategory);

        productStore.put(product.getId(), product);
        return productMapper.toProductDetailsEntry(product);
    }

    @Override
    public List<ProductDetailsEntry> getAllProducts() {
        return productStore.values().stream()
                .map(productMapper::toProductDetailsEntry)
                .toList();
    }

    @Override
    public ProductDetailsEntry getProductById(Long id) {
        Product product = productStore.get(id);
        if (product == null) {
            throw new ProductNotFoundException("Product with id " + id + " not found");
        }

        ProductDetailsEntry productDto = productMapper.toProductDetailsEntry(product);

        SupplierInfoDto supplierInfo = supplierClient.getSupplierInfo(product.getSku());

        if (supplierInfo != null) {
            productDto.setSupplierName(supplierInfo.getSupplierName());
            productDto.setSupplierCountry(supplierInfo.getCountry());
        }


        return productDto;
    }

    @Override
    public ProductDetailsEntry updateProduct(Long id, ProductDetailsDto productDto) {
        Product existingProduct = productStore.get(id);
        if (existingProduct == null) {
            throw new ProductNotFoundException("Product with id " + id + " not found");
        }

        existingProduct.setName(productDto.getName());
        existingProduct.setDescription(productDto.getDescription());
        existingProduct.setPrice(productDto.getPrice());
        existingProduct.setSku(productDto.getSku());
        existingProduct.setStockQuantity(productDto.getStockQuantity());
        Category mockCategory = new Category();
        mockCategory.setName(productDto.getCategory());
        existingProduct.setCategory(mockCategory);

        productStore.put(id, existingProduct);
        return productMapper.toProductDetailsEntry(existingProduct);
    }

    @Override
    public void deleteProduct(Long id) {
        if (!productStore.containsKey(id)) {
            throw new ProductNotFoundException("Product with id " + id + " not found");
        }
        productStore.remove(id);
    }
}
