package com.jfcbxp.performace.service.util;

import com.jfcbxp.performace.entity.Product;
import com.jfcbxp.performace.repository.ProductRepository;
import org.redisson.api.RMapReactive;
import org.redisson.api.RedissonReactiveClient;
import org.redisson.codec.TypedJsonJacksonCodec;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;

//@Service
public class ProductCacheTemplate extends CacheTemplate<Integer, Product> {

    @Autowired
    private ProductRepository repository;

    private RMapReactive<Integer,Product> mapReactive;

    public ProductCacheTemplate(RedissonReactiveClient redissonReactiveClient) {
        this.mapReactive = redissonReactiveClient.getMap("product", new TypedJsonJacksonCodec(Integer.class,Product.class));
    }

    @Override
    protected Mono<Product> getFromSource(Integer id) {
        return this.repository.findById(id);
    }

    @Override
    protected Mono<Product> getFromCache(Integer id) {
        return this.mapReactive.get(id);
    }

    @Override
    protected Mono<Product> updateSource(Integer id, Product product) {
        return this.repository.findById(id).doOnNext(p-> product.setId(id)).flatMap(p -> this.repository.save(product));
    }

    @Override
    protected Mono<Product> updateCache(Integer id, Product product) {
        return this.mapReactive.fastPut(id,product).thenReturn(product);
    }

    @Override
    protected Mono<Void> deleteFromSource(Integer id) {
        return this.repository.deleteById(id);
    }

    @Override
    protected Mono<Void> deleteFromCache(Integer id) {
        return this.mapReactive.fastRemove(id).then();
    }
}
