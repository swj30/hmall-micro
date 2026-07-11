package com.hmall.point.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hmall.common.domain.PageDTO;
import com.hmall.point.domain.PointsTransaction;
import com.hmall.point.domain.dto.PointsTransactionQueryDTO;
import com.hmall.point.domain.vo.PointsTransactionVO;

public interface IPointsTransactionService extends IService<PointsTransaction> {

    PageDTO<PointsTransactionVO> queryMyTransactions(PointsTransactionQueryDTO query);
}
