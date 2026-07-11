package com.hmall.point.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmall.common.domain.PageDTO;
import com.hmall.common.utils.BeanUtils;
import com.hmall.common.utils.CollUtils;
import com.hmall.common.utils.UserContext;
import com.hmall.point.domain.PointsProductSnapshot;
import com.hmall.point.domain.PointsTransaction;
import com.hmall.point.domain.dto.PointsTransactionQueryDTO;
import com.hmall.point.domain.vo.PointsTransactionVO;
import com.hmall.point.mapper.PointsTransactionMapper;
import com.hmall.point.service.IPointsProductSnapshotService;
import com.hmall.point.service.IPointsTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PointsTransactionServiceImpl extends ServiceImpl<PointsTransactionMapper, PointsTransaction> implements IPointsTransactionService {

    private final IPointsProductSnapshotService pointsProductSnapshotService;

    @Override
    public PageDTO<PointsTransactionVO> queryMyTransactions(PointsTransactionQueryDTO query) {
        Long userId = UserContext.getUser();
        var page = lambdaQuery()
                .eq(PointsTransaction::getUserId, userId)
                .eq(query.getTransactionType() != null, PointsTransaction::getTransactionType, query.getTransactionType())
                .eq(query.getStatus() != null, PointsTransaction::getStatus, query.getStatus())
                .eq(StrUtil.isNotBlank(query.getOrderId()), PointsTransaction::getOrderId, query.getOrderId())
                .page(query.toMpPage("created_at", false));
        var voList = BeanUtils.copyList(page.getRecords(), PointsTransactionVO.class);
        enrichWithProductSnapshots(voList);
        return PageDTO.of(page, voList);
    }

    private void enrichWithProductSnapshots(List<PointsTransactionVO> voList) {
        var transactionIds = voList.stream().map(PointsTransactionVO::getId).collect(Collectors.toSet());
        var snapshots = pointsProductSnapshotService.lambdaQuery()
                .in(PointsProductSnapshot::getTransactionId, transactionIds)
                .list();
        if (CollUtils.isEmpty(snapshots)) {
            return;
        }
        Map<Long, PointsProductSnapshot> snapshotMap = snapshots.stream()
                .collect(Collectors.toMap(PointsProductSnapshot::getTransactionId, Function.identity(), (a, b) -> a));
        for (PointsTransactionVO vo : voList) {
            PointsProductSnapshot snapshot = snapshotMap.get(vo.getId());
            if (snapshot == null) {
                continue;
            }
            vo.setProductName(snapshot.getProductName());
            vo.setProductImage(snapshot.getProductImage());
            vo.setSkuInfo(snapshot.getSkuInfo());
            vo.setUnitPrice(snapshot.getUnitPrice());
            vo.setQuantity(snapshot.getQuantity());
        }
    }
}
