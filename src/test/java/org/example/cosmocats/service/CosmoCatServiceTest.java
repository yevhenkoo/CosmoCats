package org.example.cosmocats.service;

import org.example.cosmocats.domain.Order;
import org.example.cosmocats.domain.Product;
import org.example.cosmocats.repository.OrderRepository;
import org.example.cosmocats.repository.ProductRepository;
import org.example.cosmocats.repository.projection.ProductSalesProjection;
import org.example.cosmocats.service.impl.CosmoCatServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CosmoCatServiceTest {

  @Mock private OrderRepository orderRepository;

  @Mock private ProductRepository productRepository;

  @InjectMocks private CosmoCatServiceImpl cosmoCatService;

  @Test
  @DisplayName("getCosmoCats: Should return default list")
  void getCosmoCats_ShouldReturnList() {
    List<String> result = cosmoCatService.getCosmoCats();

    assertThat(result).containsExactly("Star Cat", "Galaxy Cat", "Comet Cat");
  }

  @Test
  @DisplayName("createOrder: Should calculate total price and save order")
  void createOrder_ShouldSaveOrder() {

    Product p1 = new Product();
    p1.setId(1L);
    p1.setPrice(100.0);

    Product p2 = new Product();
    p2.setId(2L);
    p2.setPrice(50.0);

    List<Long> productIds = List.of(1L, 2L);

    when(productRepository.findAllById(productIds)).thenReturn(List.of(p1, p2));

    when(orderRepository.save(any(Order.class)))
        .thenAnswer(
            invocation -> {
              Order order = invocation.getArgument(0);
              order.setId(123L);
              return order;
            });

    Order createdOrder = cosmoCatService.createOrder(productIds);

    assertThat(createdOrder).isNotNull();
    assertThat(createdOrder.getId()).isEqualTo(123L);
    assertThat(createdOrder.getTotalPrice()).isEqualTo(150.0); // 100 + 50
    assertThat(createdOrder.getProducts()).hasSize(2);
    assertThat(createdOrder.getOrderNumber()).isNotNull();

    verify(orderRepository).save(any(Order.class));
  }

  @Test
  @DisplayName("createOrder: Should throw exception if products not found")
  void createOrder_ShouldThrowException_WhenNoProductsFound() {
    List<Long> productIds = List.of(99L);
    when(productRepository.findAllById(productIds)).thenReturn(Collections.emptyList());
    assertThrows(IllegalArgumentException.class, () -> cosmoCatService.createOrder(productIds));
    verify(orderRepository, never()).save(any());
  }

  @Test
  @DisplayName("getTopSellingReport: Should delegate to repository")
  void getTopSellingReport_ShouldCallRepo() {

    ProductSalesProjection projection = new ProductSalesProjection("Test Product", 10L);
    when(productRepository.getTopSellingProductsReport()).thenReturn(List.of(projection));

    List<ProductSalesProjection> report = cosmoCatService.getTopSellingReport();

    assertThat(report).hasSize(1);
    assertThat(report.get(0).productName()).isEqualTo("Test Product");

    verify(productRepository).getTopSellingProductsReport();
  }
}
