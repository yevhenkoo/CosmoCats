package org.example.cosmocats.service;

import org.example.cosmocats.entity.OrderEntity;
import org.example.cosmocats.repository.projection.ProductSalesProjection;
import java.util.List;

public interface CosmoCatService {
  List<String> getCosmoCats();

  OrderEntity createOrder(List<Long> productIds);

  List<ProductSalesProjection> getTopSellingReport();
}
