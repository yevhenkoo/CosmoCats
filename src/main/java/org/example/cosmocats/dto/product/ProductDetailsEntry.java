package org.example.cosmocats.dto.product;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
public class ProductDetailsEntry {
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Long id;

  private String name;
  private double price;
  private String sku;
  private String category;
  private int stockQuantity;

  private String supplierName;
  private String supplierCountry;
}
