package org.example.cosmocats.client;

import org.example.cosmocats.dto.product.SupplierInfoDto;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class SupplierClient {

  private final RestClient restClient;

  public SupplierClient(RestClient supplierRestClient) {
    this.restClient = supplierRestClient;
  }

  public SupplierInfoDto getSupplierInfo(String sku) {
    try {
      return restClient
          .get()
          .uri("/suppliers/info/{sku}", sku)
          .retrieve()
          .body(SupplierInfoDto.class);
    } catch (Exception e) {

      System.err.println("Error fetching supplier info: " + e.getMessage());
      return null;
    }
  }
}
