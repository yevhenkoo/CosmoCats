package org.example.cosmocats.service.mapper;

import org.example.cosmocats.domain.Product;

import org.example.cosmocats.dto.product.ProductDetailsDto;
import org.example.cosmocats.dto.product.ProductDetailsEntry;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductMapper {

  @Mapping(source = "category.name", target = "category")
  ProductDetailsEntry toProductDetailsEntry(Product product);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "category", ignore = true)
  Product toProductEntity(ProductDetailsDto dto);
}
