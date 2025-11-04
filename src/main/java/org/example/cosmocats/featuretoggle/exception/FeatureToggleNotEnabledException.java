package org.example.cosmocats.featuretoggle.exception;

public class FeatureToggleNotEnabledException extends RuntimeException {

  public FeatureToggleNotEnabledException(String featureName) {
    super("Feature toggle " + featureName + " is not enabled!");
  }
}
