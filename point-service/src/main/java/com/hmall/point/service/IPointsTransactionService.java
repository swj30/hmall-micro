package com.hmall.point.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hmall.common.domain.PageDTO;
import com.hmall.point.domain.PointsTransaction;
import com.hmall.point.domain.dto.PointsTransactionQueryDTO;
import com.hmall.point.domain.vo.PointsTransactionVO;

import java.util.Map;

public interface IPointsTransactionService extends IService<PointsTransaction> {

    PageDTO<PointsTransactionVO> queryMyTransactions(PointsTransactionQueryDTO query);

    /**
     * 根据订单id更新积分表
     * @param userId 用户id
     * @param orderId 订单id
     */
    void updatePointsTransaction(Long userId, Long orderId);
}
