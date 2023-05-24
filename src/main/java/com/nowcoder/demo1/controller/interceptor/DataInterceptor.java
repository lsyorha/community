package com.nowcoder.demo1.controller.interceptor;

import com.nowcoder.demo1.entity.User;
import com.nowcoder.demo1.service.DataService;
import com.nowcoder.demo1.util.HostHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
@Component
public class DataInterceptor implements HandlerInterceptor {
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private DataService dataService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//        统计UV（访客ip）
        String ip = request.getRemoteHost();
        dataService.recordUV(ip);

//        统计DAU（活跃用户）
        User user = hostHolder.getUser();
        if (user != null){
            dataService.recordDAU(user.getId());
        }

        return true;
    }
}
