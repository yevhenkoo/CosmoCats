package org.example.cosmocats.repository;

import org.example.cosmocats.entity.ProductEntity;
import org.example.cosmocats.repository.projection.ProductSalesProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, Long> {

  @Query(
      """
        SELECT new org.example.cosmocats.repository.projection.ProductSalesProjection(p.name, COUNT(o))
        FROM OrderEntity o
        JOIN o.products p
        GROUP BY p.name
        ORDER BY COUNT(o) DESC
    """)
  List<ProductSalesProjection> getTopSellingProductsReport();
}
