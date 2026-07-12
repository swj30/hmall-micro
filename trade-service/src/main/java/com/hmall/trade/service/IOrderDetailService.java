package com.hmall.trade.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hmall.trade.domain.po.OrderDetail;

import java.util.List;

public interface IOrderDetailService extends IService<OrderDetail> {

    /**
     * 根据订单id查询订单详情
     * @param orderId
     * @return
     */
    List<OrderDetail> queryOrderDetailByOrderId(Long orderId);
}
