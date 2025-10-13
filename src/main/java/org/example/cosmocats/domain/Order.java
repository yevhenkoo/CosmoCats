package org.example.cosmocats.domain;

import java.util.List;
import lombok.Data;

@Data
public class Order {
  private Long id;
  private List<Product> products;
  private double totalPrice;
}
