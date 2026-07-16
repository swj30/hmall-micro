package com.hmall.item;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import com.hmall.item.domain.vo.ItemVO;
import com.hmall.item.service.IItemService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.ResponseException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;

@Slf4j
@SpringBootTest
public class ESTest {

    @Autowired
    private ElasticsearchClient client;
    @Autowired
    private IItemService iItemService;

    /**
     * 批量导入
     * @throws IOException
     */
    @Test
    void testBulkRequest() throws IOException {
        // 1.批量查询酒店数据
        var items = this.iItemService.list();

        // 2.流式处理，得到BulkOperation集合
        var operationList = items.stream()
                .map(ItemVO::new)
                .map(item -> new BulkOperation.Builder()
                        .index(builder -> builder
                                .id(item.getId().toString())
                                .document(item))
                        .build()
                ).toList();

        // 3.批量新增
        var bulkResponse = this.client.bulk(builder -> builder
                .index("item")
                .operations(operationList));

        // 4.如果出现错误，输出所有错误信息
        if (bulkResponse.errors()) {
            log.error("Bulk had errors");
            for (var item : bulkResponse.items()) {
                if (item.error() != null) {
                    log.error(item.error().reason());
                }
            }
        }
    }

    @Test
    void testBulkRequest1() throws IOException, InterruptedException {
        // 1. 批量查询酒店数据
        var items = this.iItemService.list();
        if (items == null || items.isEmpty()) {
            log.warn("No items found, skip bulk indexing");
            return;
        }

        // 2. 流式处理，得到BulkOperation集合
        var operationList = items.stream()
                .map(ItemVO::new)
                .map(item -> new BulkOperation.Builder()
                        .index(builder -> builder
                                .id(item.getId().toString())
                                .document(item))
                        .build()
                )
                .toList();

        // 3. 分批写入，每批200条（根据单条约53KB计算，每批约10MB，安全范围内）
        int batchSize = 200;
        int maxRetries = 3;

        for (int i = 0; i < operationList.size(); i += batchSize) {
            int end = Math.min(i + batchSize, operationList.size());
            List<BulkOperation> batch = operationList.subList(i, end);

            // 带指数退避的重试机制
            for (int retry = 0; retry <= maxRetries; retry++) {
                try {
                    var bulkResponse = this.client.bulk(builder -> builder
                            .index("item")
                            .operations(batch));

                    // 4. 如果出现错误，输出所有错误信息
                    if (bulkResponse.errors()) {
                        log.error("Bulk had errors in batch [{}-{}]", i, end);
                        for (var item : bulkResponse.items()) {
                            if (item.error() != null) {
                                log.error("Doc id: {}, error: {}", item.id(), item.error().reason());
                            }
                        }
                    } else {
                        log.info("Batch [{}-{}] indexed successfully, took {}ms",
                                i, end, bulkResponse.took());
                    }
                    // 成功则跳出重试循环
                    break;

                } catch (ResponseException e) {
                    if (e.getResponse().getStatusLine().getStatusCode() == 429 && retry < maxRetries) {
                        long waitMs = (long) Math.pow(2, retry) * 1000;
                        log.warn("ES returned 429, retrying after {}ms (attempt {}/{})",
                                waitMs, retry + 1, maxRetries);
                        Thread.sleep(waitMs);
                    } else {
                        throw e;
                    }
                }
            }
        }

        log.info("All {} items indexed completed", operationList.size());
    }
}
