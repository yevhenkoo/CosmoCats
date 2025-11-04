package org.example.cosmocats.featuretoggle;

import lombok.Getter;

@Getter
public enum FeatureToggles {
  COSMO_CATS("cosmo-cats"),
  KITTY_PRODUCTS("kitty-products");

  private final String featureName;

  FeatureToggles(String featureName) {
    this.featureName = featureName;
  }
}
