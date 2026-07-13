package com.hmall.point.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("points_transaction")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PointsTransaction implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 订单号(业务唯一键)
     */
    private String orderId;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 积分变动值(默认+2)
     */
    private Integer pointsChange;

    /**
     * 类型: 1-下单奖励 2-退款扣除 3-手动调整
     */
    private Integer transactionType;

    /**
     * 状态: 0-处理中 1-已到账 2-失败
     */
    private Integer status;

    /**
     * 积分产生时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
