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

}
