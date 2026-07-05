package com.hmall.cart.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmall.api.item.ItemClient;
import com.hmall.api.item.dto.ItemDTO;
import com.hmall.common.exception.BizIllegalException;
import com.hmall.common.utils.BeanUtils;
import com.hmall.common.utils.CollUtils;
import com.hmall.cart.domain.dto.CartFormDTO;
import com.hmall.cart.domain.po.Cart;
import com.hmall.cart.domain.vo.CartVO;
import com.hmall.cart.mapper.CartMapper;
import com.hmall.cart.service.ICartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.function.Function;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor    // 配合 final/@NonNull 字段实现构造器注入
public class CartServiceImpl extends ServiceImpl<CartMapper, Cart> implements ICartService {

    private final ItemClient itemClient;

    @Override
    public void addItem2Cart(CartFormDTO cartFormDTO) {
        var userId = 1L;
        if (checkItemExists(cartFormDTO.getItemId(), userId)) {
            baseMapper.updateNum(cartFormDTO.getItemId(), userId);
            return;
        }
        checkCartsFull(userId);
        var cart = BeanUtils.copyBean(cartFormDTO, Cart.class);
        cart.setUserId(userId);
        save(cart);
    }

    @Override
    public List<CartVO> queryMyCarts() {
        var carts = lambdaQuery().eq(Cart::getUserId, 1L).list();
        if (CollUtils.isEmpty(carts)) {
            return CollUtils.emptyList();
        }
        var vos = BeanUtils.copyList(carts, CartVO.class);
        handleCartItems(vos);
        return vos;
    }

    private void handleCartItems(List<CartVO> vos) {
        // 通过 Feign 远程调用获取商品信息
         var itemIds = vos.stream().map(CartVO::getItemId).collect(Collectors.toSet());
         var items = itemClient.queryItemByIds(itemIds);
         if (CollUtils.isEmpty(items)) {
             return;
         }
         var itemMap = items.stream().collect(Collectors.toMap(ItemDTO::getId, Function.identity()));
         for (CartVO v : vos) {
             var item = itemMap.get(v.getItemId());
             if (item == null) {
                 continue;
             }
             v.setNewPrice(item.getPrice());
             v.setStatus(item.getStatus());
             v.setStock(item.getStock());
         }
    }

    @Override
    public void removeByItemIds(Collection<Long> itemIds) {
        var queryWrapper = new QueryWrapper<Cart>();
        queryWrapper.lambda()
                .eq(Cart::getUserId, 1L)
                .in(Cart::getItemId, itemIds);
        remove(queryWrapper);
    }

    private void checkCartsFull(Long userId) {
        var count = lambdaQuery().eq(Cart::getUserId, userId).count();
        if (count >= 10) {
            throw new BizIllegalException(StrUtil.format("用户购物车课程不能超过{}", 10));
        }
    }

    private boolean checkItemExists(Long itemId, Long userId) {
        var count = lambdaQuery()
                .eq(Cart::getUserId, userId)
                .eq(Cart::getItemId, itemId)
                .count();
        return count > 0;
    }
}
