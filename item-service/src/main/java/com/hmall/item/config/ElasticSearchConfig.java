package com.hmall.item.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Data
@Slf4j
@Configuration
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "elasticsearch")
public class ElasticSearchConfig {

    private String host;
    private Integer port;
    private final ObjectMapper objectMapper;

    @Bean
    public ElasticsearchClient elasticsearchClient() {
        log.info("es连接ip：{}，端口：{}", host, port);
        // 基本的连接
        var restClient = RestClient.builder(new HttpHost(host, port)).build();

        // 使用自定义json序列化
        var jacksonJsonpMapper = new JacksonJsonpMapper(objectMapper);

        // 基于Jackson mapper创建ElasticsearchTransport
        var transport = new RestClientTransport(restClient, jacksonJsonpMapper);

        // 创建ElasticsearchClient客户端
        return new ElasticsearchClient(transport);
    }
}
