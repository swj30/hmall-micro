package com.hmall.pay.service;

import com.hmall.common.utils.UserContext;
import com.hmall.pay.domain.dto.PayOrderFormDTO;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class PayOrderServiceTest {

    @Resource
    private IPayOrderService payOrderService;

    @Test
    void tryPayOrderByBalance() {
        //多线程模拟并发
        var dto = PayOrderFormDTO.builder()
                .id(1659160218174607363L) // 设置数据表中的数据id
                .pw("123") // 用户密码
                .build();

        for (int i = 0; i < 3; i++) {
            new Thread(() -> {
                UserContext.setUser(1L); // 设置用户id，模拟传递当前登录用户id
                try {
                    payOrderService.tryPayOrderByBalance(dto);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                System.out.println("线程id = " + Thread.currentThread().getId());
            }).start();
        }

        //睡眠20秒，等待所有子线程的完成
        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}