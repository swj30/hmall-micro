package com.hmall.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

/**
 * 自定义GatewayFilter
 * 配置文件中可以为单个服务配置也可以为全局配置
 * filters:
 *  - FirstFilter # 此处直接以自定义的GatewayFilterFactory类名称前缀类声明过滤器    单个微服务配置(路由位置配置)
 *
 *  spring:
 *   cloud:
 *     gateway:
 *       default-filters:
 *         - FirstFilter # 此处直接以自定义的GatewayFilterFactory类名称前缀来声明过滤器 全局配置
 */
@Slf4j
@Component
public class FirstFilterGatewayFilterFactory extends AbstractGatewayFilterFactory<Object> {
    @Override
    public GatewayFilter apply(Object config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            log.info("请求路径：{}",request.getPath());
            log.info("网关过滤器FirstFilterGatewayFilterFactory执行啦...");
            //放行
            return chain.filter(exchange);
            //拦截 返回401状态码
            //exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            //return exchange.getResponse().setComplete();
        };
    }
}
