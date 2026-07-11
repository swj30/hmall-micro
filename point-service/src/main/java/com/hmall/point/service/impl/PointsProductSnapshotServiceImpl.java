package com.hmall.point.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmall.point.domain.PointsProductSnapshot;
import com.hmall.point.mapper.PointsProductSnapshotMapper;
import com.hmall.point.service.IPointsProductSnapshotService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PointsProductSnapshotServiceImpl extends ServiceImpl<PointsProductSnapshotMapper, PointsProductSnapshot> implements IPointsProductSnapshotService {
}
