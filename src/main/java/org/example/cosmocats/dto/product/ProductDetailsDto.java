package org.example.cosmocats.dto.product;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Value;
import org.example.cosmocats.dto.validation.CosmicWordCheck;

@Value
@Builder(toBuilder = true)
public class ProductDetailsDto {

  @NotBlank(message = "must not be blank")
  @Size(max = 150, message = "must be less than or equal to 150 characters")
  @CosmicWordCheck
  String name;

  @Size(max = 500, message = "must be less than or equal to 500 characters")
  String description;

  @NotNull(message = "must not be null")
  @Positive(message = "must be greater than 0")
  Double price;

  @Pattern(regexp = "^[A-Z0-9-]{3,20}$", message = "must match the format '^[A-Z0-9-]{3,20}$'")
  @NotBlank(message = "must not be blank")
  String sku;

  @NotBlank(message = "must not be blank")
  String category;

  @NotNull(message = "must not be null")
  @Min(value = 0, message = "must be greater than or equal to 0")
  Integer stockQuantity;
}
