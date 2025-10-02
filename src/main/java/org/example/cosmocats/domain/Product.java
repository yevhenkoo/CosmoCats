package org.example.cosmocats.domain;
import lombok.Data;

@Data
public class Product {
    private Long id;
    private String name;
    private String description;
    private double price;
    private String sku;
    private int stockQuantity;
    private Category category;
}
