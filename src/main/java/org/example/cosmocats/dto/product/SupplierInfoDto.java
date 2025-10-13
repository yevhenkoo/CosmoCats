package org.example.cosmocats.dto.product;

import lombok.Value;

@Value
public class SupplierInfoDto {
  String supplierName;
  String country;
  int deliveryTimeDays;
}
