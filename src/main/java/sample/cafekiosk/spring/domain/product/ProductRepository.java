package sample.cafekiosk.spring.domain.product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findAllBySellingStatusIn(List<ProductSellingStatus> sellingTypes);

    List<Product> findAllByProductNumberIn(List<String> productNumbers);

    @Query(value = "select product_number from product p order by id desc limit 1", nativeQuery = true)
    String findLatestProductNumber();
}
