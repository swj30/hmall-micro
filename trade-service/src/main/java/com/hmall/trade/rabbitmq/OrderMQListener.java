package com.hmall.trade.rabbitmq;

import com.hmall.api.trade.dto.CancelOrderDTO;
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


    /**
     * 订单超时未支付，自动退回库存
     * @param cancelOrderDTO
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = RabbitMQConstant.PAY_TIMEOUT_QUEUE_NAME),
            exchange = @Exchange(name = RabbitMQConstant.PAY_TIMEOUT_EXCHANGE_NAME, type = ExchangeTypes.TOPIC),
            key = RabbitMQConstant.PAY_TIMEOUT_ROUTING_KEY
    ))
    public void payTimeOut(CancelOrderDTO cancelOrderDTO) {
        log.info("监听到超时未支付订单信息:" +  cancelOrderDTO);
        orderService.payTimeout(cancelOrderDTO);
    }
}
