package com.hmall.point.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmall.point.domain.UserPointsSummary;
import com.hmall.point.mapper.UserPointsSummaryMapper;
import com.hmall.point.service.IUserPointsSummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserPointsSummaryServiceImpl extends ServiceImpl<UserPointsSummaryMapper, UserPointsSummary> implements IUserPointsSummaryService {
}
