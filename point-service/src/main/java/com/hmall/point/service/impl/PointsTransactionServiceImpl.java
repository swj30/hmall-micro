package com.hmall.point.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmall.api.trade.TradeClient;
import com.hmall.common.domain.PageDTO;
import com.hmall.common.domain.vo.OrderDetailVO;
import com.hmall.common.domain.vo.OrderVO;
import com.hmall.common.utils.BeanUtils;
import com.hmall.common.utils.CollUtils;
import com.hmall.common.utils.UserContext;
import com.hmall.point.domain.PointsProductSnapshot;
import com.hmall.point.domain.PointsTransaction;
import com.hmall.point.domain.UserPointsSummary;
import com.hmall.point.domain.dto.PointsTransactionQueryDTO;
import com.hmall.point.domain.vo.PointsTransactionVO;
import com.hmall.point.mapper.PointsTransactionMapper;
import com.hmall.point.service.IPointsProductSnapshotService;
import com.hmall.point.service.IPointsTransactionService;
import com.hmall.point.service.IUserPointsSummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PointsTransactionServiceImpl extends ServiceImpl<PointsTransactionMapper, PointsTransaction> implements IPointsTransactionService {

    private final IPointsProductSnapshotService pointsProductSnapshotService;
    private final IUserPointsSummaryService userPointsSummaryService;
    private final TradeClient tradeClient;

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

    @Override
    public void updatePointsTransaction(Long orderId) {
        // 获取用户Id
        Long userId = UserContext.getUser();
        // 根据订单Id查询订单表
        List<OrderDetailVO> orderDetailVOS = tradeClient.queryOrderDetailByOrderId(orderId);

        PointsTransaction pointsTransaction = new PointsTransaction();
        PointsProductSnapshot pointsProductSnapshot = new PointsProductSnapshot();
        // 遍历商品详情列表，
        orderDetailVOS.forEach(orderDetail -> {
            // 补全积分流水表
            pointsTransaction.setUserId(userId);
            pointsTransaction.setOrderId(StrUtil.toString(orderId));
            pointsTransaction.setProductId(orderDetail.getItemId());
            pointsTransaction.setPointsChange(2);
            pointsTransaction.setTransactionType(1);
            pointsTransaction.setStatus(1);
            pointsTransaction.setCreatedAt(LocalDateTime.now());
            pointsTransaction.setUpdatedAt(LocalDateTime.now());
            save(pointsTransaction);

            // 补全积分流水快照表
            pointsProductSnapshot.setTransactionId(orderDetail.getId());
            pointsProductSnapshot.setProductId(orderDetail.getItemId());
            pointsProductSnapshot.setOrderId(StrUtil.toString(orderId));
            pointsProductSnapshot.setProductName(orderDetail.getName());
            pointsProductSnapshot.setProductImage(orderDetail.getImage());
            pointsProductSnapshot.setSkuInfo(orderDetail.getSpec());
            pointsProductSnapshot.setUnitPrice(BigDecimal.valueOf(orderDetail.getPrice()));
            pointsProductSnapshot.setQuantity(orderDetail.getNum());
            pointsProductSnapshot.setCreatedAt(LocalDateTime.now());

            pointsProductSnapshotService.save(pointsProductSnapshot);
        });

        UserPointsSummary userPointsSummary = userPointsSummaryService.getOne(new LambdaQueryWrapper<UserPointsSummary>()
                .eq(UserPointsSummary::getUserId, userId)
        );

        if (!ObjectUtil.isEmpty(userPointsSummary)) {
            // 用户存在
            userPointsSummaryService.update(new LambdaUpdateWrapper<UserPointsSummary>()
                    .set(UserPointsSummary::getTotalPoints, userPointsSummary.getTotalPoints() + 2)
                            .set(UserPointsSummary::getUpdatedAt, LocalDateTime.now())
                    .eq(UserPointsSummary::getUserId, userId)
            );
        } else {
            // 用户不存在
            UserPointsSummary userPointsSummary1 = new UserPointsSummary();
            userPointsSummary1.setUserId(userId);
            userPointsSummary1.setTotalPoints(userPointsSummary.getTotalPoints() + 2);
            userPointsSummary1.setUpdatedAt(LocalDateTime.now());
            userPointsSummaryService.save(userPointsSummary1);
        }


    }
}
