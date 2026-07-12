package com.hmall.point.rabbitmq;


import com.hmall.common.constant.RabbitMQConstant;
import com.hmall.point.service.IPointsTransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;

/**
 * 消息监听
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class PayMQListener {

    private final IPointsTransactionService pointsTransactionService;


    /**
     * 监听支付消息，支付成功,积分+2
     * @param info   用户id + 订单id
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = RabbitMQConstant.POINT_QUEUE_NAME),
            exchange = @Exchange(name = RabbitMQConstant.POINT_EXCHANGE_NAME, delayed = "true", type = ExchangeTypes.TOPIC),
            key = RabbitMQConstant.POINT_ROUTING_KEY
    ))
    public void addPoints(Map<String, Long> info) {
        Long userId = info.get("userId");
        Long orderId = info.get("orderId");
        log.info("监听到用户信息:{}，订单信息:{}" + userId, orderId);
        pointsTransactionService.updatePointsTransaction( userId, orderId);
    }
}
