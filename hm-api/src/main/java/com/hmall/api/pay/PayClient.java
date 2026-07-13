package com.hmall.api.pay;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "pay-service", path = "/pay-orders")
public interface PayClient {

    /**
     * 订单是否已经被支付
     * @param orderId
     * @return
     */
    @GetMapping("/{orderId}")
    public Boolean isOrderPay(@PathVariable Long orderId);

    /**
     * 修改pay_order表的status字段
     * @param orderId
     * @param status
     */
    @PutMapping("/updateStatus")
    public void updatePayOrderStatus(@RequestParam("orderId") Long orderId,
                                     @RequestParam("/status") Integer status);
}
