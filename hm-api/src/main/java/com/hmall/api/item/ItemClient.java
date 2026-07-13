package com.hmall.api.item;

import com.hmall.api.item.dto.ItemDTO;
import com.hmall.api.item.dto.OrderDetailDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;

@FeignClient(name = "item-service", path = "/items", fallbackFactory = ItemClientFallbackFactory.class)
public interface ItemClient {

    @GetMapping
    List<ItemDTO> queryItemByIds(@RequestParam("ids") Collection<Long> ids);

    @PutMapping("/stock/deduct")
    void deductStock(@RequestBody List<OrderDetailDTO> items);

    /**
     * 订单超时未支付回退库存
     * @param items
     */
    @PostMapping("/reStock")
    public void addStock(@RequestBody List<OrderDetailDTO> items);
}
