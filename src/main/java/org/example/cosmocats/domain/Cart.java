package org.example.cosmocats.domain;

import java.util.List;
import lombok.Data;

@Data
public class Cart {
  private Long id;
  private List<Product> items;
}
