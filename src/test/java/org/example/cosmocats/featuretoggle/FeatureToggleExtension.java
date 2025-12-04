package org.example.cosmocats.featuretoggle;

import org.example.cosmocats.featuretoggle.annotation.DisabledFeatureToggle;
import org.example.cosmocats.featuretoggle.annotation.EnabledFeatureToggle;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit.jupiter.SpringExtension;

public class FeatureToggleExtension implements BeforeEachCallback, AfterEachCallback {

  @Override
  public void beforeEach(ExtensionContext context) {
    context
        .getTestMethod()
        .ifPresent(
            method -> {
              FeatureToggleService featureToggleService = getFeatureToggleService(context);
              if (method.isAnnotationPresent(EnabledFeatureToggle.class)) {
                EnabledFeatureToggle annotation = method.getAnnotation(EnabledFeatureToggle.class);
                featureToggleService.enable(annotation.value().getFeatureName());
              } else if (method.isAnnotationPresent(DisabledFeatureToggle.class)) {
                DisabledFeatureToggle annotation =
                    method.getAnnotation(DisabledFeatureToggle.class);
                featureToggleService.disable(annotation.value().getFeatureName());
              }
            });
  }

  @Override
  public void afterEach(ExtensionContext context) {
    context
        .getTestMethod()
        .ifPresent(
            method -> {
              String featureName = null;
              if (method.isAnnotationPresent(EnabledFeatureToggle.class)) {
                EnabledFeatureToggle annotation = method.getAnnotation(EnabledFeatureToggle.class);
                featureName = annotation.value().getFeatureName();
              } else if (method.isAnnotationPresent(DisabledFeatureToggle.class)) {
                DisabledFeatureToggle annotation =
                    method.getAnnotation(DisabledFeatureToggle.class);
                featureName = annotation.value().getFeatureName();
              }

              if (featureName != null) {
                FeatureToggleService featureToggleService = getFeatureToggleService(context);
                if (getFeatureNamePropertyAsBoolean(context, featureName)) {
                  featureToggleService.enable(featureName);
                } else {
                  featureToggleService.disable(featureName);
                }
              }
            });
  }

  private boolean getFeatureNamePropertyAsBoolean(ExtensionContext context, String featureName) {
    Environment environment = SpringExtension.getApplicationContext(context).getEnvironment();
    return environment.getProperty("feature.toggles." + featureName, Boolean.class, Boolean.FALSE);
  }

  private FeatureToggleService getFeatureToggleService(ExtensionContext context) {
    return SpringExtension.getApplicationContext(context).getBean(FeatureToggleService.class);
  }
}
