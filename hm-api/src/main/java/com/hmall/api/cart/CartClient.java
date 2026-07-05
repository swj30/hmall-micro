package com.hmall.api.cart;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;

@FeignClient(name = "cart-service", path = "/carts")
public interface CartClient {

    @DeleteMapping
    void deleteCartItemByIds(@RequestParam("ids") Collection<Long> ids);
}
