package com.hmall.point.controller;

import com.hmall.common.domain.PageDTO;
import com.hmall.point.domain.dto.PointsTransactionQueryDTO;
import com.hmall.point.domain.vo.PointsTransactionVO;
import com.hmall.point.service.IPointsProductSnapshotService;
import com.hmall.point.service.IPointsTransactionService;
import com.hmall.point.service.IUserPointsSummaryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "积分相关接口")
@Slf4j
@RestController
@RequestMapping("/points")
@RequiredArgsConstructor
public class PointsController {
    private final IPointsTransactionService pointsTransactionService;
    private final IUserPointsSummaryService userPointsSummaryService;
    private final IPointsProductSnapshotService pointsProductSnapshotService;

    @ApiOperation("分页查询我的积分流水")
    @GetMapping("/transactions")
    public PageDTO<PointsTransactionVO> queryMyTransactions(PointsTransactionQueryDTO query) {
        return pointsTransactionService.queryMyTransactions(query);
    }
}
