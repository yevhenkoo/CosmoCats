package org.example.cosmocats.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.cosmocats.domain.Order;
import org.example.cosmocats.domain.Product;
import org.example.cosmocats.repository.OrderRepository;
import org.example.cosmocats.repository.ProductRepository;
import org.example.cosmocats.repository.projection.ProductSalesProjection;
import org.example.cosmocats.service.CosmoCatService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CosmoCatServiceImpl implements CosmoCatService {

  private final OrderRepository orderRepository;
  private final ProductRepository productRepository;

  @Override
  public List<String> getCosmoCats() {
    return List.of("Star Cat", "Galaxy Cat", "Comet Cat");
  }

  @Override
  @Transactional
  public Order createOrder(List<Long> productIds) {
    log.info("Creating order with products: {}", productIds);

    List<Product> products = productRepository.findAllById(productIds);
    if (products.isEmpty()) {
      throw new IllegalArgumentException("No products found for ID list");
    }

    double totalPrice = products.stream().mapToDouble(Product::getPrice).sum();

    Order order = new Order();
    order.setOrderNumber(UUID.randomUUID().toString());
    order.setProducts(products);
    order.setTotalPrice(totalPrice);

    return orderRepository.save(order);
  }

  @Override
  @Transactional(readOnly = true)
  public List<ProductSalesProjection> getTopSellingReport() {
    log.info("Generating top selling products report");
    return productRepository.getTopSellingProductsReport();
  }
}
