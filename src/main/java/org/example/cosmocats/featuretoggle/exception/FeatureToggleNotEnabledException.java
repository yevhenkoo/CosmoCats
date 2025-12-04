package org.example.cosmocats.featuretoggle.exception;

public class FeatureToggleNotEnabledException extends RuntimeException {

  public FeatureToggleNotEnabledException(String featureName) {
    super(String.format("Feature toggle %s is not enabled!", featureName));
  }
}
