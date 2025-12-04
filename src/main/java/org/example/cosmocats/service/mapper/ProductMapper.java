package org.example.cosmocats.service.mapper;

import org.example.cosmocats.entity.ProductEntity;
import org.example.cosmocats.dto.product.ProductDetailsDto;
import org.example.cosmocats.dto.product.ProductDetailsEntry;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductMapper {

  @Mapping(source = "category.name", target = "category")
  @Mapping(target = "supplierName", ignore = true)
  @Mapping(target = "supplierCountry", ignore = true)
  ProductDetailsEntry toProductDetailsEntry(ProductEntity product);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "category", ignore = true)
  ProductEntity toProductEntity(ProductDetailsDto dto);
}
