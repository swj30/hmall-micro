package com.hmall.point.domain.dto;

import com.hmall.common.domain.PageQuery;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(description = "积分流水查询条件")
public class PointsTransactionQueryDTO extends PageQuery {
    @ApiModelProperty("类型: 1-下单奖励 2-退款扣除 3-手动调整")
    private Integer transactionType;
    @ApiModelProperty("状态: 0-处理中 1-已到账 2-失败")
    private Integer status;
    @ApiModelProperty("订单号")
    private String orderId;
}
