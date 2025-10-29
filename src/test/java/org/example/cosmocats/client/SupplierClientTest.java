package org.example.cosmocats.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.example.cosmocats.dto.product.SupplierInfoDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Supplier Client Test (WireMock)")
class SupplierClientTest {

    @RegisterExtension
    static WireMockExtension wireMockServer = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();
    private SupplierClient supplierClient;

    @BeforeEach
    void setup() {

        wireMockServer.resetAll();

        String baseUrl = wireMockServer.baseUrl();

        RestClient restClient = RestClient.builder().baseUrl(baseUrl).build();

        this.supplierClient = new SupplierClient(restClient);
    }

    @Test
    @DisplayName("getSupplierInfo (Positive): Should return DTO on 200 OK response")
    void getSupplierInfo_shouldReturnInfo_whenApiCallIsSuccessful() throws Exception {
        // Arrange
        String sku = "SKU-SUCCESS";
        SupplierInfoDto supplierInfo = new SupplierInfoDto("Galactic Supplies", "Andromeda", 3);
        String jsonBody = objectMapper.writeValueAsString(supplierInfo);

        wireMockServer.stubFor(get(urlEqualTo("/suppliers/info/" + sku))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(jsonBody)));

        // Act
        SupplierInfoDto result = supplierClient.getSupplierInfo(sku);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getSupplierName()).isEqualTo("Galactic Supplies");
        assertThat(result.getDeliveryTimeDays()).isEqualTo(3);

        // Перевіряємо, що клієнт дійсно викликав потрібний URL
        wireMockServer.verify(getRequestedFor(urlEqualTo("/suppliers/info/" + sku)));
    }

    @Test
    @DisplayName("getSupplierInfo (Negative): Should return null if the API returns 404")
    void getSupplierInfo_shouldReturnNull_whenApiReturns404() {
        // Arrange
        String sku = "SKU-NOT-FOUND";
        wireMockServer.stubFor(get(urlEqualTo("/suppliers/info/" + sku))
                .willReturn(aResponse().withStatus(404)));

        SupplierInfoDto result = supplierClient.getSupplierInfo(sku);

        assertThat(result).isNull();

        wireMockServer.verify(getRequestedFor(urlEqualTo("/suppliers/info/" + sku)));
    }

    @Test
    @DisplayName("getSupplierInfo (Negative): Should return null if API returns 500")
    void getSupplierInfo_shouldReturnNull_whenApiReturns500() {

        String sku = "SKU-ERROR";
        wireMockServer.stubFor(get(urlEqualTo("/suppliers/info/" + sku))
                .willReturn(aResponse().withStatus(500)));

        SupplierInfoDto result = supplierClient.getSupplierInfo(sku);

        assertThat(result).isNull();

        wireMockServer.verify(getRequestedFor(urlEqualTo("/suppliers/info/" + sku)));
    }
}