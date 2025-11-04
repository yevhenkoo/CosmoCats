package org.example.cosmocats.service;

import org.example.cosmocats.featuretoggle.FeatureToggles;
import org.example.cosmocats.featuretoggle.annotation.FeatureToggle;
import org.springframework.stereotype.Service;

@Service
public class CosmoCatService {

  @FeatureToggle(FeatureToggles.COSMO_CATS)
  public String getCosmoCats() {
    return "Ось ваші КосмоКоти!";
  }

  @FeatureToggle(FeatureToggles.KITTY_PRODUCTS)
  public String getKittyProducts() {
    return "Ось продукти для кошенят!";
  }
}
