package com.hmall.item.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmall.common.exception.BizIllegalException;
import com.hmall.common.utils.BeanUtils;
import com.hmall.api.item.dto.ItemDTO;
import com.hmall.api.item.dto.OrderDetailDTO;
import com.hmall.item.domain.po.Item;
import com.hmall.item.mapper.ItemMapper;
import com.hmall.item.service.IItemService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl extends ServiceImpl<ItemMapper, Item> implements IItemService {

    private final RedissonClient redissonClient;
    private final RedisTemplate<String, Object> redisTemplate;

    @Transactional
    public void deductStock(List<OrderDetailDTO> items) {
        for (OrderDetailDTO item : items) {
            var i = baseMapper.updateStock(item);
            if(i<=0){
                throw new BizIllegalException("库存不足！");
            }
        }
    }

    @Override
    public List<ItemDTO> queryItemByIds(Collection<Long> ids) {
        return BeanUtils.copyList(listByIds(ids), ItemDTO.class);
    }

    @Override
    public ItemDTO queryItemById(Long id) {
        RBloomFilter<Long> bloomFilter = redissonClient.getBloomFilter("bloom:item:id");
        if (!bloomFilter.contains(id)) {
            return null;
        }

        String key = "item:id:" + id;
        Object cacheItem = redisTemplate.opsForValue().get(key);
        if (cacheItem != null) {
            return JSONUtil.toBean(JSONUtil.toJsonStr(cacheItem), ItemDTO.class);
        }

        Item item = getById(id);
        if (item == null) {
            return null;
        }

        ItemDTO itemDTO = BeanUtils.copyBean(item, ItemDTO.class);
        redisTemplate.opsForValue().set(key, itemDTO, 1, TimeUnit.HOURS);
        return itemDTO;
    }

    /**
     * 订单超时未支付回退库存
     *
     * @param items
     */
    @Override
    public void addStock(List<OrderDetailDTO> items) {
        // 获取商品库存
        items.forEach(item -> {
            Long itemId = item.getItemId();
            Integer num = item.getNum();
            Integer stock = getById(itemId).getStock();
            lambdaUpdate()
                    .eq(Item::getId, itemId)
                    .set(Item::getStock, stock + num)
                    .update();
        });
    }
}
