package org.example.cosmocats.domain;

import java.util.List;
import lombok.Data;
import org.example.cosmocats.entity.ProductEntity;

@Data
public class Cart {
  private Long id;
  private List<ProductEntity> items;
}
