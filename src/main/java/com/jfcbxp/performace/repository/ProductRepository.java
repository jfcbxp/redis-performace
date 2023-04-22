package com.jfcbxp.performace.repository;

import com.jfcbxp.performace.entity.Product;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends ReactiveCrudRepository<Product,Integer> {
}
