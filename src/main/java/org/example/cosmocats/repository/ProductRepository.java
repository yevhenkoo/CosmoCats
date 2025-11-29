package org.example.cosmocats.repository;

import org.example.cosmocats.domain.Product;
import org.example.cosmocats.repository.projection.ProductSalesProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

  @Query(
      """
        SELECT new org.example.cosmocats.repository.projection.ProductSalesProjection(p.name, COUNT(o))
        FROM Order o
        JOIN o.products p
        GROUP BY p.name
        ORDER BY COUNT(o) DESC
    """)
  List<ProductSalesProjection> getTopSellingProductsReport();
}
