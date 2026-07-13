package com.hmall.trade.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmall.api.cart.CartClient;
import com.hmall.api.item.ItemClient;
import com.hmall.api.item.dto.ItemDTO;
import com.hmall.api.item.dto.OrderDetailDTO;
import com.hmall.api.pay.PayClient;
import com.hmall.common.constant.RabbitMQConstant;
import com.hmall.api.trade.dto.CancelOrderDTO;
import com.hmall.common.domain.dto.CartClearDTO;
import com.hmall.common.exception.BadRequestException;
import com.hmall.common.utils.UserContext;
import com.hmall.trade.domain.dto.OrderFormDTO;
import com.hmall.trade.domain.po.Order;
import com.hmall.trade.domain.po.OrderDetail;
import com.hmall.trade.mapper.OrderMapper;
import com.hmall.trade.service.IOrderDetailService;
import com.hmall.trade.service.IOrderService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderService {

    private final ItemClient itemClient;
    private final IOrderDetailService detailService;
    private final CartClient cartClient;
    private final RabbitTemplate rabbitTemplate;
    private final PayClient payClient;


    @Override
    @GlobalTransactional    // @GlobalTransactional 注解就是在标记事务的起点，将来 TM 就会基于这个方法判断全局事务范围，初始化全局事务。
    public Long createOrder(OrderFormDTO orderFormDTO) {
        var order = new Order();
        var detailDTOS = orderFormDTO.getDetails();
        var itemNumMap = detailDTOS.stream()
                .collect(Collectors.toMap(OrderDetailDTO::getItemId, OrderDetailDTO::getNum));
        Set<Long> itemIds = itemNumMap.keySet();
        List<ItemDTO> items = itemClient.queryItemByIds(itemIds);
        if (items == null || items.size() < itemIds.size()) {
            throw new BadRequestException("商品不存在");
        }
        var total = 0;
        for (ItemDTO item : items) {
            total += item.getPrice() * itemNumMap.get(item.getId());
        }
        order.setTotalFee(total);
        order.setPaymentType(orderFormDTO.getPaymentType());
        order.setUserId(UserContext.getUser());
        order.setStatus(1);
        save(order);

        List<OrderDetail> details = buildDetails(order.getId(), items, itemNumMap);
        detailService.saveBatch(details);

        // 改为异步调用   ->  根据商品ids清空购物车内的商品
        // cartClient.deleteCartItemByIds(itemIds);
        rabbitTemplate.convertAndSend(RabbitMQConstant.TRADE_EXCHANGE_NAME, RabbitMQConstant.TRADE_SUCCESS_ROUTING_KEY,
                new CartClearDTO(UserContext.getUser(), itemIds));


        // 延迟消息，用户下单后在规定的时间未支付，则商品数量退回库存
        // 需要传递用户id， 商品id,以及订单id
        // 传递的参数
        CancelOrderDTO cancelOrderDTO = CancelOrderDTO.builder()
                .userId(UserContext.getUser())
                .orderId(order.getId())
                .details(orderFormDTO.getDetails())
                .build();
        // 发送消息
        rabbitTemplate.convertAndSend(RabbitMQConstant.PAY_TIMEOUT_EXCHANGE_NAME, RabbitMQConstant.PAY_TIMEOUT_SUCCESS_ROUTING_KEY, cancelOrderDTO, message -> {
            // 添加延迟消息属性
            message.getMessageProperties().setDelay(1800000);
            return message;
        });

        try {
            // 减库存
            itemClient.deductStock(detailDTOS);
        } catch (Exception e) {
            throw new RuntimeException("库存不足！");
        }
        return order.getId();
    }

    @Override
    public void markOrderPaySuccess(Long orderId) {
        /*var order = new Order();
        order.setId(orderId);
        order.setStatus(2);
        order.setPayTime(LocalDateTime.now());
        updateById(order);*/
        // 保持
        lambdaUpdate()
                .set(Order::getStatus, 2)
                .set(Order::getPayTime, LocalDateTime.now())
                .eq(Order::getId, orderId)
                .eq(Order::getStatus, 1)    // 仅未支付状态更新
                .update();
    }

    /**
     * 订单超时未支付处理逻辑
     * @param cancelOrderDTO
     */
    @Override
    public void payTimeout(CancelOrderDTO cancelOrderDTO) {
        Long orderId = cancelOrderDTO.getOrderId();
        // 先查询pay_order表，如果订单已支付，直接返回
        Boolean isOrderPay = payClient.isOrderPay(orderId);
        if (isOrderPay) {
            // 订单已经支付了，直接返回
            return;
        }
        // 订单未支付，退回库存
        // 先修改pay_order表的订单状态
        payClient.updatePayOrderStatus(orderId, 2);

        // 修改order表的状态为5订单关闭
        lambdaUpdate()
                .eq(Order::getId, orderId)
                .set(Order::getStatus, 5)
                .update();

        // 回退库存
        // 先拿到订单商品详情表
        List<OrderDetailDTO> orderDetailList = cancelOrderDTO.getDetails();
        List<OrderDetailDTO> detailDTOList = orderDetailList.stream()
                .map(d -> OrderDetailDTO.builder()
                        .itemId(d.getItemId())
                        .num(d.getNum())
                        .build()).toList();
        // 调用item-service微服务的FeignClient方法去回退库存
        itemClient.addStock(detailDTOList);

    }

    private List<OrderDetail> buildDetails(Long orderId, List<ItemDTO> items, Map<Long, Integer> numMap) {
        var details = new ArrayList<OrderDetail>(items.size());
        for (ItemDTO item : items) {
            var detail = new OrderDetail();
            detail.setName(item.getName());
            detail.setSpec(item.getSpec());
            detail.setPrice(item.getPrice());
            detail.setNum(numMap.get(item.getId()));
            detail.setItemId(item.getId());
            detail.setImage(item.getImage());
            detail.setOrderId(orderId);
            details.add(detail);
        }
        return details;
    }
}
