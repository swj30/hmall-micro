package com.hmall.common.config;

import com.hmall.common.interceptor.UserInfoFeignInterceptor;
import feign.RequestInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Feign自动装配，将用户信息通过请求头传递到下游微服务
 */
@Configuration
@ConditionalOnClass(RequestInterceptor.class)
public class FeignConfig {

    @Bean
    public RequestInterceptor userInfoFeignInterceptor() {
        return new UserInfoFeignInterceptor();
    }

}
