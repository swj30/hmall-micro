package com.hmall.item.config;

import com.hmall.item.domain.po.Item;
import com.hmall.item.service.IItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BloomFilterInitializer implements CommandLineRunner {

    private final RedissonClient redissonClient;
    private final IItemService itemService;

    @Override
    public void run(String... args) {
        RBloomFilter<Long> bloomFilter = redissonClient.getBloomFilter("bloom:item:id");
        bloomFilter.tryInit(100000L, 0.01);

        long count = itemService.count();
        log.info("开始初始化布隆过滤器，预计加载 {} 个商品ID", count);

        itemService.lambdaQuery()
                .select(Item::getId)
                .list()
                .forEach(item -> bloomFilter.add(item.getId()));

        log.info("布隆过滤器初始化完成，已加载 {} 个商品ID", bloomFilter.count());
    }

}
