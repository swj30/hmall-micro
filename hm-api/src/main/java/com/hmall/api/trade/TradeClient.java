package com.hmall.api.trade;

import com.hmall.common.domain.vo.OrderDetailVO;
import com.hmall.common.domain.vo.OrderVO;
import feign.Param;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "trade-service", path = "/orders")
public interface TradeClient {

    @PutMapping("/{orderId}")
    void markOrderPaySuccess(@PathVariable("orderId") Long orderId);

    /**
     * 根据id查询订单信息
     * @param orderId
     * @return
     */
    @GetMapping("{id}")
    public OrderVO queryOrderById(@Param("订单id") @PathVariable("id") Long orderId);

    /**
     * 根据订单id查询订单详情
     * @param orderId
     * @return
     */
    @GetMapping("/orderId")
    public List<OrderDetailVO> queryOrderDetailByOrderId(@RequestParam("orderId") Long orderId);
}
