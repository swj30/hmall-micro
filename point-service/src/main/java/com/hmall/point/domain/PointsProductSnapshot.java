package com.hmall.point.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("points_product_snapshot")
public class PointsProductSnapshot implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 关联积分流水表ID
     */
    private Long transactionId;

    /**
     * 订单号(冗余,方便独立查询)
     */
    private String orderId;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 商品名称(下单时快照)
     */
    private String productName;

    /**
     * 商品主图URL(下单时快照)
     */
    private String productImage;

    /**
     * SKU规格信息(JSON格式,如颜色/尺码)
     */
    private String skuInfo;

    /**
     * 成交单价(下单时价格)
     */
    private BigDecimal unitPrice;

    /**
     * 购买数量
     */
    private Integer quantity;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
