package org.example.cosmocats.web;

import org.example.cosmocats.AbstractIT;
import org.example.cosmocats.entity.CategoryEntity;
import org.example.cosmocats.entity.OrderEntity;
import org.example.cosmocats.entity.ProductEntity;
import org.example.cosmocats.repository.CategoryRepository;
import org.example.cosmocats.repository.OrderRepository;
import org.example.cosmocats.repository.ProductRepository;
import org.example.cosmocats.repository.projection.ProductSalesProjection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@WithMockUser(roles = "ADMIN")
class OrderIntegrationIT extends AbstractIT {

  @Autowired private CategoryRepository categoryRepository;
  @Autowired private ProductRepository productRepository;
  @Autowired private OrderRepository orderRepository;

  @Test
  @DisplayName("Should save order and retrieve sales report correctly")
  @Transactional
  void shouldSaveOrderAndGetReport() {
    CategoryEntity electronics = new CategoryEntity();
    electronics.setName("Electronics");
    categoryRepository.save(electronics);

    ProductEntity galaxyCommunicator = new ProductEntity();
    galaxyCommunicator.setName("Galaxy Communicator 3000");
    galaxyCommunicator.setPrice(500.0);
    galaxyCommunicator.setCategory(electronics);
    productRepository.save(galaxyCommunicator);

    ProductEntity starMap = new ProductEntity();
    starMap.setName("Interstellar Map");
    starMap.setPrice(100.0);
    starMap.setCategory(electronics);
    productRepository.save(starMap);

    OrderEntity order1 = new OrderEntity();
    order1.setOrderNumber(UUID.randomUUID().toString());
    order1.setTotalPrice(600.0);
    order1.setProducts(List.of(galaxyCommunicator, starMap));

    orderRepository.save(order1);

    OrderEntity order2 = new OrderEntity();
    order2.setOrderNumber(UUID.randomUUID().toString());
    order2.setTotalPrice(500.0);
    order2.setProducts(List.of(galaxyCommunicator));
    orderRepository.save(order2);

    List<ProductSalesProjection> report = productRepository.getTopSellingProductsReport();

    assertThat(report).hasSize(2);

    ProductSalesProjection topProduct = report.get(0);
    assertThat(topProduct.productName()).isEqualTo("Galaxy Communicator 3000");
    assertThat(topProduct.salesCount()).isEqualTo(2);

    ProductSalesProjection secondProduct = report.get(1);
    assertThat(secondProduct.productName()).isEqualTo("Interstellar Map");
    assertThat(secondProduct.salesCount()).isEqualTo(1);
  }

  @Test
  @DisplayName("Should generate Natural ID and CreatedAt properly")
  void shouldVerifyConstraints() {

    CategoryEntity cat = new CategoryEntity();
    cat.setName("Food");
    categoryRepository.save(cat);

    ProductEntity tuna = new ProductEntity();
    tuna.setName("Space Tuna");
    tuna.setPrice(10.0);
    tuna.setCategory(cat);
    productRepository.save(tuna);

    OrderEntity order = new OrderEntity();
    String naturalId = "ORD-2024-001";
    order.setOrderNumber(naturalId);
    order.setTotalPrice(10.0);
    order.setProducts(List.of(tuna));

    OrderEntity savedOrder = orderRepository.save(order);

    assertThat(savedOrder.getId()).isNotNull();
    assertThat(savedOrder.getCreatedAt()).isNotNull();
    assertThat(savedOrder.getOrderNumber()).isEqualTo(naturalId);
  }
}
