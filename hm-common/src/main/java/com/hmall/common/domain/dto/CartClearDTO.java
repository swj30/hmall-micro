package com.hmall.common.domain.dto;

import java.util.Collection;

public class CartClearDTO {
    private Long userId;
    private Collection<Long> itemIds;

    public CartClearDTO() {}

    public CartClearDTO(Long userId, Collection<Long> itemIds) {
        this.userId = userId;
        this.itemIds = itemIds;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Collection<Long> getItemIds() {
        return itemIds;
    }

    public void setItemIds(Collection<Long> itemIds) {
        this.itemIds = itemIds;
    }
}
