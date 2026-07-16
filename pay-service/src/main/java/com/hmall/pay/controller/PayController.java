package com.hmall.pay.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hmall.common.exception.BizIllegalException;
import com.hmall.pay.domain.dto.PayApplyDTO;
import com.hmall.pay.domain.dto.PayOrderFormDTO;
import com.hmall.pay.domain.po.PayOrder;
import com.hmall.pay.enums.PayType;
import com.hmall.pay.service.IPayOrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Api(tags = "支付相关接口")
@RestController
@RequestMapping("pay-orders")
@RequiredArgsConstructor
public class PayController {

    private final IPayOrderService payOrderService;

    @ApiOperation("生成支付单")
    @PostMapping
    public String applyPayOrder(@RequestBody PayApplyDTO applyDTO) {
        if (!PayType.BALANCE.equalsValue(applyDTO.getPayType())) {
            throw new BizIllegalException("抱歉，目前只支持余额支付");
        }
        return payOrderService.applyPayOrder(applyDTO);
    }

    @ApiOperation("尝试基于用户余额支付")
    @ApiImplicitParam(value = "支付单id", name = "id")
    @PostMapping("{id}")
    public void tryPayOrderByBalance(@PathVariable("id") Long id, @RequestBody PayOrderFormDTO payOrderFormDTO) throws InterruptedException {
        payOrderFormDTO.setId(id);
        payOrderService.tryPayOrderByBalance(payOrderFormDTO);
    }

    /**
     * 订单是否已经被支付
     * @param payOrderId
     * @return
     */
    @GetMapping("/{payOrderId}")
    public Boolean isOrderPay(@PathVariable Long payOrderId) {
        PayOrder payOrder = payOrderService.getOne(new LambdaQueryWrapper<PayOrder>()
                .eq(PayOrder::getId, payOrderId)
                .eq(PayOrder::getStatus, 1)
        );
        if (payOrder == null) {
            // 支付了
            return true;
        }
        return false;
    }

    /**
     * 修改pay_order表的status字段
     * @param payOrderId 支付单id
     */
    @PutMapping("/updateStatus")
    public void updatePayOrderStatus(@RequestParam("orderId") Long payOrderId) {
        payOrderService.lambdaUpdate().eq(PayOrder::getId, payOrderId)
                .set(PayOrder::getStatus, 2).update();
    }
}
