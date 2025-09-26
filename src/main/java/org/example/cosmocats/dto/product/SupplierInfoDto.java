package org.example.cosmocats.dto.product;

import lombok.Data;

@Data
public class SupplierInfoDto {
    private String supplierName;
    private String country;
    private int deliveryTimeDays;
}
