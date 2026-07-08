package com.hmall.common.interceptor;

import com.hmall.common.utils.UserContext;
import feign.RequestInterceptor;
import feign.RequestTemplate;

public class UserInfoFeignInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        Long userId = UserContext.getUser();
        if (userId != null) {
            template.header("user-info", userId.toString());
        }
    }
}
