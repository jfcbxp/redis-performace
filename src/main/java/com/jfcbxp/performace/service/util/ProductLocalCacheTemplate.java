package com.jfcbxp.performace.service.util;

import com.jfcbxp.performace.entity.Product;
import com.jfcbxp.performace.repository.ProductRepository;
import org.redisson.api.LocalCachedMapOptions;
import org.redisson.api.RLocalCachedMap;
import org.redisson.api.RedissonClient;
import org.redisson.codec.TypedJsonJacksonCodec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class ProductLocalCacheTemplate extends CacheTemplate<Integer, Product> {

    @Autowired
    private ProductRepository repository;

    private RLocalCachedMap<Integer,Product> mapReactive;

    public ProductLocalCacheTemplate(RedissonClient redissonClient) {
        LocalCachedMapOptions<Integer, Product> mapOptions = LocalCachedMapOptions.<Integer, Product>defaults()
                .syncStrategy(LocalCachedMapOptions.SyncStrategy.UPDATE)
                .reconnectionStrategy(LocalCachedMapOptions.ReconnectionStrategy.CLEAR);

        this.mapReactive = redissonClient.getLocalCachedMap("product", new TypedJsonJacksonCodec(Integer.class,Product.class),mapOptions);
    }

    @Override
    protected Mono<Product> getFromSource(Integer id) {
        return this.repository.findById(id);
    }

    @Override
    protected Mono<Product> getFromCache(Integer id) {
        return Mono.justOrEmpty(this.mapReactive.get(id)    );
    }

    @Override
    protected Mono<Product> updateSource(Integer id, Product product) {
        return this.repository.findById(id).doOnNext(p-> product.setId(id)).flatMap(p -> this.repository.save(product));
    }

    @Override
    protected Mono<Product> updateCache(Integer id, Product product) {
        return Mono.create(sink -> this.mapReactive
                .fastPutAsync(id,product)
                .thenAccept(b ->sink.success(product))
                .exceptionally(ex -> {
                    sink.error(ex);
                    return null;
                }));
    }

    @Override
    protected Mono<Void> deleteFromSource(Integer id) {
        return this.repository.deleteById(id);
    }

    @Override
    protected Mono<Void> deleteFromCache(Integer id) {
        return Mono.create(sink -> this.mapReactive
                .fastRemoveAsync(id)
                .thenAccept(b ->sink.success())
                .exceptionally(ex -> {
                    sink.error(ex);
                    return null;
                }));
    }
}
