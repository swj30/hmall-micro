package com.hmall.common.domain.vo;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
public class OrderDetailVO implements Serializable {
    private Long id;
    private Long orderId;
    private Long itemId;
    private Integer num;
    private String name;
    private String spec;
    private Integer price;
    private String image;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
