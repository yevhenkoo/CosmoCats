package org.example.cosmocats.featuretoggle;

import lombok.Getter;

@Getter
public enum FeatureToggles {
  COSMO_CATS("cosmo-kitty"),
  KITTY_PRODUCTS("cat-products");

  private final String featureName;

  FeatureToggles(String featureName) {
    this.featureName = featureName;
  }
}
