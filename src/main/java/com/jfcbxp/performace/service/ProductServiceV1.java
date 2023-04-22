package com.jfcbxp.performace.service;

import com.jfcbxp.performace.entity.Product;
import com.jfcbxp.performace.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class ProductServiceV1 {

    @Autowired
    private ProductRepository repository;

    public Mono<Product> getProduct(int id){
        return this.repository.findById(id);

    }

    public Mono<Product> updateProduct(int id,Mono<Product> product){
        return this.repository.findById(id).flatMap(p -> product.doOnNext(pr ->pr.setId(id)))
                .flatMap(this.repository::save);
    }
}
