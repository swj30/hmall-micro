package com.hmall.api.trade.dto;

import com.hmall.api.item.dto.OrderDetailDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 订单超市未支付回退库存的dto
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancelOrderDTO {

    /**
     * 用户id
     */
    private Long userId;
    /**
     * 订单id
     */
    private Long orderId;
    /**
     * 商品详情
     */
    private List<OrderDetailDTO> details;
}
