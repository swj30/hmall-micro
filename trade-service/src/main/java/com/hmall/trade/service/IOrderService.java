package com.hmall.trade.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hmall.api.trade.dto.CancelOrderDTO;
import com.hmall.trade.domain.dto.OrderFormDTO;
import com.hmall.trade.domain.po.Order;

public interface IOrderService extends IService<Order> {

    Long createOrder(OrderFormDTO orderFormDTO);

    void markOrderPaySuccess(Long orderId);

    /**
     * 订单超时未支付处理逻辑
     * @param cancelOrderDTO
     */
    void payTimeout(CancelOrderDTO cancelOrderDTO);
}
