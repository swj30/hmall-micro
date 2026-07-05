package com.hmall.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hmall.user.domain.po.User;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface UserMapper extends BaseMapper<User> {
    @Update("update user set balance = balance - #{totalFee} where id = #{userId} and balance >= #{totalFee}")
    int updateMoney(@Param("userId") Long userId, @Param("totalFee") Integer totalFee);
}
