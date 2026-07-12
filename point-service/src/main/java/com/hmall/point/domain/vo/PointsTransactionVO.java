package com.hmall.point.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@ApiModel(description = "积分流水VO")
public class PointsTransactionVO {
    @ApiModelProperty("流水ID")
    private Long id;
    @ApiModelProperty("用户ID")
    private Long userId;
    @ApiModelProperty("订单号")
    private String orderId;
    @ApiModelProperty("商品ID")
    private Long productId;
    @ApiModelProperty("积分变动值")
    private Integer pointsChange;
    @ApiModelProperty("类型: 1-下单奖励 2-退款扣除 3-手动调整")
    private Integer transactionType;
    @ApiModelProperty("状态: 0-处理中 1-已到账 2-失败")
    private Integer status;
    @ApiModelProperty("积分产生时间")
    private LocalDateTime createdAt;
    @ApiModelProperty("更新时间")
    private LocalDateTime updatedAt;
    @ApiModelProperty("商品名称")
    private String productName;
    @ApiModelProperty("商品主图URL")
    private String productImage;
    @ApiModelProperty("SKU规格信息")
    private String skuInfo;
    @ApiModelProperty("成交单价")
    private BigDecimal unitPrice;
    @ApiModelProperty("购买数量")
    private Integer quantity;
}
