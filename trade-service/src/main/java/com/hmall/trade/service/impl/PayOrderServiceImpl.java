package com.hmall.trade.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmall.api.user.UserClient;
import com.hmall.common.exception.BizIllegalException;
import com.hmall.common.utils.BeanUtils;
import com.hmall.trade.domain.dto.PayApplyDTO;
import com.hmall.trade.domain.dto.PayOrderFormDTO;
import com.hmall.trade.domain.po.Order;
import com.hmall.trade.domain.po.PayOrder;
import com.hmall.trade.enums.PayStatus;
import com.hmall.trade.mapper.PayOrderMapper;
import com.hmall.trade.service.IOrderService;
import com.hmall.trade.service.IPayOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PayOrderServiceImpl extends ServiceImpl<PayOrderMapper, PayOrder> implements IPayOrderService {

    private final UserClient userClient;
    private final IOrderService orderService;

    @Override
    public String applyPayOrder(PayApplyDTO applyDTO) {
        var payOrder = checkIdempotent(applyDTO);
        return payOrder.getId().toString();
    }

    @Override
    @Transactional
    public void tryPayOrderByBalance(PayOrderFormDTO payOrderFormDTO) {
        var po = getById(payOrderFormDTO.getId());
        if (!PayStatus.WAIT_BUYER_PAY.equalsValue(po.getStatus())) {
            throw new BizIllegalException("交易已支付或关闭！");
        }
        userClient.deductMoney(payOrderFormDTO.getPw(), po.getAmount());
        boolean success = markPayOrderSuccess(payOrderFormDTO.getId(), LocalDateTime.now());
        if (!success) {
            throw new BizIllegalException("交易已支付或关闭！");
        }
        var order = new Order();
        order.setId(po.getBizOrderNo());
        order.setStatus(2);
        order.setPayTime(LocalDateTime.now());
        orderService.updateById(order);
    }

    public boolean markPayOrderSuccess(Long id, LocalDateTime successTime) {
        return lambdaUpdate()
                .set(PayOrder::getStatus, PayStatus.TRADE_SUCCESS.getValue())
                .set(PayOrder::getPaySuccessTime, successTime)
                .eq(PayOrder::getId, id)
                .in(PayOrder::getStatus, PayStatus.NOT_COMMIT.getValue(), PayStatus.WAIT_BUYER_PAY.getValue())
                .update();
    }

    private PayOrder checkIdempotent(PayApplyDTO applyDTO) {
        var oldOrder = queryByBizOrderNo(applyDTO.getBizOrderNo());
        if (oldOrder == null) {
            var payOrder = buildPayOrder(applyDTO);
            payOrder.setPayOrderNo(IdWorker.getId());
            save(payOrder);
            return payOrder;
        }
        if (PayStatus.TRADE_SUCCESS.equalsValue(oldOrder.getStatus())) {
            throw new BizIllegalException("订单已经支付！");
        }
        if (PayStatus.TRADE_CLOSED.equalsValue(oldOrder.getStatus())) {
            throw new BizIllegalException("订单已关闭");
        }
        if (!StringUtils.equals(oldOrder.getPayChannelCode(), applyDTO.getPayChannelCode())) {
            var payOrder = buildPayOrder(applyDTO);
            payOrder.setId(oldOrder.getId());
            payOrder.setQrCodeUrl("");
            updateById(payOrder);
            payOrder.setPayOrderNo(oldOrder.getPayOrderNo());
            return payOrder;
        }
        return oldOrder;
    }

    private PayOrder buildPayOrder(PayApplyDTO payApplyDTO) {
        var payOrder = BeanUtils.toBean(payApplyDTO, PayOrder.class);
        payOrder.setPayOverTime(LocalDateTime.now().plusMinutes(120L));
        payOrder.setStatus(PayStatus.WAIT_BUYER_PAY.getValue());
        payOrder.setBizUserId(1L);
        return payOrder;
    }

    public PayOrder queryByBizOrderNo(Long bizOrderNo) {
        return lambdaQuery()
                .eq(PayOrder::getBizOrderNo, bizOrderNo)
                .one();
    }
}
