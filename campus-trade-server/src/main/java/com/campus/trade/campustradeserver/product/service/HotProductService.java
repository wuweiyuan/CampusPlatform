package com.campus.trade.campustradeserver.product.service;

import com.campus.trade.campustradeserver.common.cache.CacheKeys;
import com.campus.trade.campustradeserver.product.mapper.ProductMapper;
import com.campus.trade.campustradeserver.product.vo.HotProductResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class HotProductService {
    private final ProductMapper productMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final JsonMapper jsonMapper;


    public List<HotProductResponse> listHotProducts(){

        try{
            String cachedJson = stringRedisTemplate.opsForValue().get(CacheKeys.PRODUCT_HOT);
            if(cachedJson != null){
                HotProductResponse[] hotProductResponses = jsonMapper.readValue(cachedJson,HotProductResponse[].class);
                log.info("热门商品缓存命中");
                return Arrays.asList(hotProductResponses);
            }
            log.info("热门商品缓存未命中");
        }catch (Exception exception){
            log.warn("热门商品缓存读取失败，降级查询MySQL",exception);
        }

        List<HotProductResponse> responses =  productMapper.selectHotProducts();
        try{
            String json = jsonMapper.writeValueAsString(responses);
            stringRedisTemplate.opsForValue().set(CacheKeys.PRODUCT_HOT,json, Duration.ofMinutes(10));
            log.info("热门商品缓存已写入");
        }catch (Exception exception){
            log.warn("热门商品缓存写入失败，不影响热门商品读取",exception);
        }
        return responses;
    }

    public void evictHotProductCache(){
        try {
            stringRedisTemplate.delete(CacheKeys.PRODUCT_HOT);
            log.info("热门商品缓存已删除");
        }catch (Exception exception){
            log.warn("热门商品缓存删除失败，不影响业务写操作", exception);
        }
    }

    public void evictHotProductCacheAfterCommit(){
        if(!TransactionSynchronizationManager.isSynchronizationActive()
                ||!TransactionSynchronizationManager.isActualTransactionActive() ){
            evictHotProductCache();
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        evictHotProductCache();
                    }
                }
        );

    }
}
