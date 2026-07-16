package com.hmall.pay.service.impl;

import cn.hutool.core.convert.Convert;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmall.api.trade.TradeClient;
import com.hmall.api.trade.dto.CancelOrderDTO;
import com.hmall.api.user.UserClient;
import com.hmall.common.constant.RabbitMQConstant;
import com.hmall.common.exception.BizIllegalException;
import com.hmall.common.utils.BeanUtils;
import com.hmall.common.utils.UserContext;
import com.hmall.pay.domain.dto.PayApplyDTO;
import com.hmall.pay.domain.dto.PayOrderFormDTO;
import com.hmall.pay.domain.po.PayOrder;
import com.hmall.pay.enums.PayStatus;
import com.hmall.pay.mapper.PayOrderMapper;
import com.hmall.pay.service.IPayOrderService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayOrderServiceImpl extends ServiceImpl<PayOrderMapper, PayOrder> implements IPayOrderService {

    private final UserClient userClient;
    private final TradeClient tradeClient;
    private final RabbitTemplate rabbitTemplate;
    private final RedissonClient redissonClient;

    @Override
    public String applyPayOrder(PayApplyDTO applyDTO) {
        var payOrder = checkIdempotent(applyDTO);


        // 延迟消息，用户下单后在规定的时间未支付，把支付单的状态改为已超时
        // 只需要传递支付单id就行
        // 发送消息
        rabbitTemplate.convertAndSend(RabbitMQConstant.PAY_TIMEOUT_EXCHANGE_NAME,
                RabbitMQConstant.PAY_TIMEOUT_SUCCESS_ROUTING_KEY,
                payOrder.getId(),
                message -> {
            // 添加延迟消息属性
            message.getMessageProperties().setDelay(10000);
            return message;
        });
        log.info("消息发送成功，支付单id: {}", payOrder.getId());

        return payOrder.getId().toString();
    }

    @Override
    @GlobalTransactional
    public void tryPayOrderByBalance(PayOrderFormDTO payOrderFormDTO) {
        // 获取锁的名称
        String lockName = Convert.toStr(payOrderFormDTO.getId());
        // 获取锁
        RLock lock = redissonClient.getFairLock(lockName);

        try {
            // 尝试获取锁,重试时间5s
            boolean isLocked = lock.tryLock(5L, TimeUnit.SECONDS);
            if (!isLocked) {
                throw new RuntimeException("支付订单繁忙，请稍后重试");
            }
            // 调用支付逻辑
            this.payOrderByBalance(payOrderFormDTO);
        } catch (Exception e) {
            throw new BizIllegalException("支付订单繁忙，请稍后重试");
        } finally {
            // 释放锁
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }

    }

    private void payOrderByBalance(PayOrderFormDTO payOrderFormDTO) {
        var po = getById(payOrderFormDTO.getId());
        if (!PayStatus.WAIT_BUYER_PAY.equalsValue(po.getStatus())) {
            throw new BizIllegalException("交易已支付或关闭！");
        }
        userClient.deductMoney(payOrderFormDTO.getPw(), po.getAmount());
        boolean success = markPayOrderSuccess(payOrderFormDTO.getId(), LocalDateTime.now());
        if (!success) {
            throw new BizIllegalException("交易已支付或关闭！");
        }

        // 同步调用改为异步调用
        //tradeClient.markOrderPaySuccess(po.getBizOrderNo());
        // 发送消息给RabbitMQ,传递订单id
        rabbitTemplate.convertAndSend(RabbitMQConstant.PAY_EXCHANGE_NAME, RabbitMQConstant.PAY_SUCCESS_ROUTING_KEY, po.getBizOrderNo());

        Long userId = UserContext.getUser();
        Map<String, Long> info = new HashMap<>();
        info.put("userId", userId);
        info.put("orderId", po.getBizOrderNo());
        // 发送消息给RabbitMQ，传递订单id和用户id,用来加积分
        rabbitTemplate.convertAndSend(RabbitMQConstant.POINT_EXCHANGE_NAME, RabbitMQConstant.POINT_SUCCESS_ROUTING_KEY, info, message -> {
            // 添加延迟消息属性
            message.getMessageProperties().setDelay(10000);
            return message;
        });
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
        payOrder.setBizUserId(UserContext.getUser());
        return payOrder;
    }

    public PayOrder queryByBizOrderNo(Long bizOrderNo) {
        return lambdaQuery()
                .eq(PayOrder::getBizOrderNo, bizOrderNo)
                .one();
    }
}
