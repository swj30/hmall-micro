package com.hmall.cart.rabbitmq;

import com.hmall.cart.service.ICartService;
import com.hmall.common.constant.RabbitMQConstant;
import com.hmall.common.domain.dto.CartClearDTO;
import com.hmall.common.utils.UserContext;
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
public class CartMQListener {

    @Autowired
    private ICartService cartService;


    /**
     * 异步清空购物车
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = RabbitMQConstant.TRADE_QUEUE_NAME),
            exchange = @Exchange(name = RabbitMQConstant.TRADE_EXCHANGE_NAME, type = ExchangeTypes.TOPIC),
            key = RabbitMQConstant.TRADE_ROUTING_KEY
    ))
    public void markOrderPaySuccess(CartClearDTO dto) {
        log.info("监听到清空购物车消息, userId: {}, itemIds: {}", dto.getUserId(), dto.getItemIds());
        UserContext.setUser(dto.getUserId());
        try {
            cartService.removeByItemIds(dto.getItemIds());
        } finally {
            UserContext.removeUser();
        }
    }
}
