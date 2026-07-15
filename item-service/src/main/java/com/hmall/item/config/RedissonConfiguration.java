package com.hmall.item.config;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

@Data
@Configuration
@EnableConfigurationProperties(RedisProperties.class)
public class RedissonConfiguration {

    @Resource
    private RedisProperties redisProperties;

    @Bean
    public RedissonClient redissonSingle() {
        var config = new Config();
        var address = StrUtil.format("redis://{}:{}", redisProperties.getHost(), redisProperties.getPort());
        var serverConfig = config.useSingleServer().setAddress(address);
        if (null != (redisProperties.getTimeout())) {
            serverConfig.setTimeout(1000 * Convert.toInt(redisProperties.getTimeout().getSeconds()));
        }
        if (StrUtil.isNotEmpty(redisProperties.getPassword())) {
            serverConfig.setPassword(redisProperties.getPassword());
        }
        return Redisson.create(config);
    }

}
