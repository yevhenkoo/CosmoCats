package org.example.cosmocats;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("local") // Щоб підтягнути application.yml
public abstract class AbstractIT {

  @Container @ServiceConnection
  static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

  @org.junit.jupiter.api.extension.RegisterExtension
  protected static WireMockExtension wireMockServer =
      WireMockExtension.newInstance()
          .options(wireMockConfig().dynamicPort())
          .configureStaticDsl(true)
          .build();

  @DynamicPropertySource
  static void configureDynamicProperties(DynamicPropertyRegistry registry) {
    // 1. OAUTH2 / JWT (динамічний порт)
    registry.add(
        "spring.security.oauth2.resourceserver.jwt.jwk-set-uri",
        () -> wireMockServer.baseUrl() + "/.well-known/jwks.json");

    // Додаємо алгоритм явно, щоб уникнути помилок
    registry.add("spring.security.oauth2.resourceserver.jwt.jws-algorithms", () -> "RS256");

    // 2. PAYMENT SERVICE (динамічний порт) - ВИПРАВЛЕНО (додано лямбду)
    registry.add("application.payment-service.base-path", () -> wireMockServer.baseUrl());

    // 3. API KEY (Гарантуємо наявність значень для тестів)
    registry.add("application.security.api-key", () -> "cosmo-secret-key-123");
    registry.add("application.security.api-key-header", () -> "X-Api-Key");
  }

  @BeforeAll
  static void setupJwksMock() {
    stubFor(
        get(urlPathEqualTo("/.well-known/jwks.json"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(
                        """
                {
                  "keys": [
                    {
                      "kty": "RSA",
                      "e": "AQAB",
                      "use": "sig",
                      "kid": "test-key-id",
                      "alg": "RS256",
                      "n": "v1..."
                    }
                  ]
                }
                """)));
  }
}
