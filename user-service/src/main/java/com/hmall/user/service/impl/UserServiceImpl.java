package com.hmall.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmall.common.exception.BadRequestException;
import com.hmall.common.exception.BizIllegalException;
import com.hmall.common.exception.ForbiddenException;
import com.hmall.user.config.JwtProperties;
import com.hmall.user.domain.dto.LoginFormDTO;
import com.hmall.user.domain.po.User;
import com.hmall.user.domain.vo.UserLoginVO;
import com.hmall.user.enums.UserStatus;
import com.hmall.user.mapper.UserMapper;
import com.hmall.user.service.IUserService;
import com.hmall.user.utils.JwtTool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    private final PasswordEncoder passwordEncoder;

    private final JwtTool jwtTool;

    private final JwtProperties jwtProperties;

    @Override
    public UserLoginVO login(LoginFormDTO loginDTO) {
        var username = loginDTO.getUsername();
        var password = loginDTO.getPassword();
        var user = lambdaQuery().eq(User::getUsername, username).one();
        Assert.notNull(user, "用户名错误");
        if (user.getStatus() == UserStatus.FROZEN) {
            throw new ForbiddenException("用户被冻结");
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadRequestException("用户名或密码错误");
        }
        var token = jwtTool.createToken(user.getId(), jwtProperties.getTokenTTL());
        var vo = new UserLoginVO();
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setBalance(user.getBalance());
        vo.setToken(token);
        return vo;
    }

    @Override
    public void deductMoney(String pw, Integer totalFee) {
        log.info("开始扣款");
        Long userId = 1L;
        var user = getById(userId);
        if (user == null || !passwordEncoder.matches(pw, user.getPassword())) {
            throw new BizIllegalException("用户密码错误");
        }
        int i = baseMapper.updateMoney(userId, totalFee);
        if (i <= 0) {
            throw new RuntimeException("扣款失败，可能是余额不足！");
        }
        log.info("扣款成功");
    }
}
