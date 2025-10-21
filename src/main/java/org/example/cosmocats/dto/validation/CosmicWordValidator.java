package org.example.cosmocats.dto.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.List;

public class CosmicWordValidator implements ConstraintValidator<CosmicWordCheck, String> {

  private static final List<String> COSMIC_WORDS =
      List.of("star", "galaxy", "comet", "nebula", "planet", "cosmo");

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null || value.isBlank()) {
      return true;
    }
    String lowerCaseValue = value.toLowerCase();
    return COSMIC_WORDS.stream().anyMatch(lowerCaseValue::contains);
  }
}
