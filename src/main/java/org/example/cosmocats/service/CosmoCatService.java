package org.example.cosmocats.service;

import org.example.cosmocats.domain.Order;
import org.example.cosmocats.repository.projection.ProductSalesProjection;
import java.util.List;

public interface CosmoCatService {
  List<String> getCosmoCats();

  Order createOrder(List<Long> productIds);

  List<ProductSalesProjection> getTopSellingReport();
}
