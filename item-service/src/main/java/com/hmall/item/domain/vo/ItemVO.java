package com.hmall.item.domain.vo;

import com.hmall.item.domain.po.Item;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 商品视图对象，用于前端展示
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 商品ID */
    private Long id;

    /** 商品名称 */
    private String name;

    /** 商品价格（单位：分） */
    private Integer price;

    /** 库存数量 */
    private Integer stock;

    /** 商品图片URL */
    private String image;

    /** 分类名称 */
    private String category;

    /** 品牌名称 */
    private String brand;

    /** 规格描述 */
    private String spec;

    /** 已售数量 */
    private Integer sold;

    /** 评论数 */
    private Integer commentCount;

    /** 商品状态：1-上架，2-下架 */
    private Integer status;

    public ItemVO(Item item) {
        this.id = item.getId();
        this.name = item.getName();
        this.price = item.getPrice();
        this.stock = item.getStock();
        this.image = item.getImage();
        this.category = item.getCategory();
        this.brand = item.getBrand();
        this.spec = item.getSpec();
        this.sold = item.getSold();
        this.commentCount = item.getCommentCount();
        this.status = item.getStatus();
    }
}