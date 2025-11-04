package org.example.cosmocats.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.example.cosmocats.featuretoggle.exception.FeatureToggleNotEnabledException;
import org.example.cosmocats.featuretoggle.FeatureToggles;
import org.example.cosmocats.featuretoggle.annotation.DisabledFeatureToggle;
import org.example.cosmocats.featuretoggle.annotation.EnabledFeatureToggle;
import org.example.cosmocats.featuretoggle.FeatureToggleExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@ExtendWith(FeatureToggleExtension.class)
class CosmoCatServiceTest {

  @Autowired private CosmoCatService cosmoCatService;

  @Test
  @EnabledFeatureToggle(FeatureToggles.COSMO_CATS)
  void whenCosmoCatsEnabled_shouldReturnData() {
    String result = cosmoCatService.getCosmoCats();
    assertNotNull(result);
    assertEquals("Ось ваші КосмоКоти!", result);
  }

  @Test
  @DisabledFeatureToggle(FeatureToggles.COSMO_CATS)
  void whenCosmoCatsDisabled_shouldThrowException() {
    FeatureToggleNotEnabledException exception =
        assertThrows(FeatureToggleNotEnabledException.class, () -> cosmoCatService.getCosmoCats());

    assertEquals("Feature toggle cosmo-cats is not enabled!", exception.getMessage());
  }

  @Test
  void whenKittyProductsDisabled_shouldThrowException() {
    assertThrows(FeatureToggleNotEnabledException.class, () -> cosmoCatService.getKittyProducts());
  }

  @Test
  @EnabledFeatureToggle(FeatureToggles.KITTY_PRODUCTS)
  void whenKittyProductsEnabled_shouldReturnData() {
    String result = cosmoCatService.getKittyProducts();
    assertNotNull(result);
    assertEquals("Ось продукти для кошенят!", result);
  }
}
