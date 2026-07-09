package com.hmall.trade.rabbitmq;

import com.hmall.common.constant.RabbitMQConstant;
import com.hmall.trade.service.IOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 消息监听
 */

@Slf4j
@Component
public class OrderMQListener {

    @Autowired
    private IOrderService orderService;


    /**
     * 异步更新订单状态
     * @param orderId
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = RabbitMQConstant.PAY_QUEUE_NAME),
            exchange = @Exchange(name = RabbitMQConstant.PAY_EXCHANGE_NAME, type = ExchangeTypes.TOPIC),
            key = RabbitMQConstant.PAY_ROUTING_KEY
    ))
    public void markOrderPaySuccess(Long orderId) {
        log.info("监听到订单id: " + orderId);
        orderService.markOrderPaySuccess(orderId);
    }
}
