package com.hmall.trade.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hmall.trade.domain.dto.PayApplyDTO;
import com.hmall.trade.domain.dto.PayOrderFormDTO;
import com.hmall.trade.domain.po.PayOrder;

public interface IPayOrderService extends IService<PayOrder> {

    String applyPayOrder(PayApplyDTO applyDTO);

    void tryPayOrderByBalance(PayOrderFormDTO payOrderFormDTO);
}
