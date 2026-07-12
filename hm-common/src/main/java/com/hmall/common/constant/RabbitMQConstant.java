package com.hmall.common.constant;

/**
 * 消息队列相关常量类
 */
public class RabbitMQConstant {


    /**
     * 支付订单相关MQ常量
     */

    /**
     * 交换机名称
     */
    public static final  String PAY_EXCHANGE_NAME = "pay.topic";
    /**
     * 队列名称
     */
    public static final String PAY_QUEUE_NAME = "pay.success.queue";
    /**
     * 接收消息RoutingKey
     */
    public static final String PAY_ROUTING_KEY = "pay.#";
    /**
     * 发送消息RoutingKey
     */
    public static final String PAY_SUCCESS_ROUTING_KEY = "pay.success";

    /**
     * 清空购物车相关MQ常量
     */

    /**
     * 交换机名称
     */
    public static final  String TRADE_EXCHANGE_NAME = "trade.topic";
    /**
     * 队列名称
     */
    public static final String TRADE_QUEUE_NAME = "cart.clear.queue";
    /**
     * 接收消息RoutingKey
     */
    public static final String TRADE_ROUTING_KEY = "order.#";
    /**
     * 发送消息RoutingKey
     */
    public static final String TRADE_SUCCESS_ROUTING_KEY = "order.create";

    /**
     * 加积分相关MQ常量
     */

    /**
     * 交换机名称
     */
    public static final  String POINT_EXCHANGE_NAME = "point.topic";
    /**
     * 队列名称
     */
    public static final String POINT_QUEUE_NAME = "point.add.queue";
    /**
     * 接收消息RoutingKey
     */
    public static final String POINT_ROUTING_KEY = "point.#";
    /**
     * 发送消息RoutingKey
     */
    public static final String POINT_SUCCESS_ROUTING_KEY = "point.add";

}
