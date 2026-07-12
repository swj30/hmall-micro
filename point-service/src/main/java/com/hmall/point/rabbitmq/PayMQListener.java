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
     * @param orderId   订单id
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = RabbitMQConstant.PAY_QUEUE_NAME),
            exchange = @Exchange(name = RabbitMQConstant.PAY_EXCHANGE_NAME, type = ExchangeTypes.TOPIC),
            key = RabbitMQConstant.PAY_ROUTING_KEY
    ))
    public void addPoints(Long orderId) {
        log.info("监听到订单id: " + orderId);
        pointsTransactionService.updatePointsTransaction(orderId);
    }
}
