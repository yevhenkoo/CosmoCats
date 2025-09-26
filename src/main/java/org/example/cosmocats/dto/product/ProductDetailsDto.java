package org.example.cosmocats.dto.product;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.example.cosmocats.dto.validation.CosmicWordCheck;

@Data
public class ProductDetailsDto {

    @NotBlank(message = "must not be blank")
    @Size(max = 150, message = "must be less than or equal to 150 characters")
    @CosmicWordCheck
    private String name;

    private String description;

    @NotNull(message = "must not be null")
    @Positive(message = "must be greater than 0")
    private Double price;

    @Pattern(regexp = "^[A-Z0-9-]{3,20}$", message = "must match the format '^[A-Z0-9-]{3,20}$'")
    @NotBlank(message = "must not be blank")
    private String sku;

    @NotBlank(message = "must not be blank")
    private String category;

    @NotNull(message = "must not be null")
    @Min(value = 0, message = "must be greater than or equal to 0")
    private Integer stockQuantity;
}
