package com.hmall.cart.controller;


import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.hmall.cart.domain.dto.CartFormDTO;
import com.hmall.cart.domain.po.Cart;
import com.hmall.cart.domain.vo.CartVO;
import com.hmall.cart.service.ICartService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Api(tags = "购物车相关接口")
@Slf4j
@RestController
@RequestMapping("/carts")
@RequiredArgsConstructor
public class CartController {
    private final ICartService cartService;

    @ApiOperation("添加商品到购物车")
    @PostMapping
    public void addItem2Cart(@Valid @RequestBody CartFormDTO cartFormDTO){
        cartService.addItem2Cart(cartFormDTO);
    }

    @ApiOperation("更新购物车数据")
    @PutMapping
    public void updateCart(@RequestBody Cart cart){
        cartService.updateById(cart);
    }

    @ApiOperation("删除购物车中商品")
    @DeleteMapping("{id}")
    public void deleteCartItem(@Param ("购物车条目id")@PathVariable("id") Long id){
        cartService.removeById(id);
    }

    @ApiOperation("查询购物车列表")
    @GetMapping
    /**
     * blockHandler专门处理"限流/熔断"等Sentinel规则触发
     * 而fallback专门处理"业务异常"
     */
    @SentinelResource(value = "queryMyCarts", fallback = "queryMyCartsFallback", blockHandler = "queryMyCartsBlockHandler")
    public List<CartVO> queryMyCarts(){
        return cartService.queryMyCarts();
    }

    // 当发生非限流非熔断异常走此方法
    public List<CartVO> queryMyCartsFallback(Throwable throwable) {
        log.error("非限流、非熔断异常执行的降级方法，throwable:", throwable);
        return List.of();
    }

    // 当发生熔断、限流走此方法
    public List<CartVO> queryMyCartsBlockHandler(BlockException blockException) {
        log.error("触发限流、熔断时执行的降级方法，blockException:", blockException);
        return List.of();
    }

    @ApiOperation("批量删除购物车中商品")
    @ApiImplicitParam(name = "ids", value = "购物车条目id集合")
    @DeleteMapping
    public void deleteCartItemByIds(@RequestParam("ids") List<Long> ids){
        cartService.removeByItemIds(ids);
    }
}
