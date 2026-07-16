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
     * @param payOrderId 支付单id
     * @return
     */
    @GetMapping("/{payOrderId}")
    public Boolean isOrderPay(@PathVariable Long payOrderId);

    /**
     * 修改pay_order表的status字段
     * @param payOrderId 支付单id
     */
    @PutMapping("/updateStatus")
    public void updatePayOrderStatus(@RequestParam("orderId") Long payOrderId);
}
