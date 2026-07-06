package com.hmall.api.item;

import com.hmall.api.item.dto.ItemDTO;
import com.hmall.api.item.dto.OrderDetailDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

@Slf4j
@Component
public class ItemClientFallbackFactory implements FallbackFactory<ItemClient> {

    @Override
    public ItemClient create(Throwable cause) {
        return new ItemClient() {
            @Override
            public List<ItemDTO> queryItemByIds(Collection<Long> ids) {
                // 记录包含异常原因的 error 日志
                log.error("调用商品服务查询商品失败，参数ids：{}", ids, cause);
                // 方法有返回值，模拟返回空集合
                return ids.stream().map(id -> ItemDTO.builder()
                        .id(id)
                        .price(9999)
                        .build()).toList();
            }

            @Override
            public void deductStock(List<OrderDetailDTO> items) {
                // 记录包含异常原因的 error 日志
                log.error("调用商品服务扣减库存失败，参数items：{}", items, cause);
                // 方法无返回值，仅打印日志记录异常
            }
        };
    }
}