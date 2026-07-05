package com.hmall;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @EnableFeignClients注解的作用寻找使用 @FeignClient 注解的接口生成Feign客户端(代理对象)，
 * 自动配置 Feign 客户端与 Spring Cloud LoadBalancer 的集成。
 */
@EnableFeignClients
@MapperScan("com.hmall.cart.mapper")
@SpringBootApplication
public class CartServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(CartServiceApplication.class, args);
    }
}
