package com.nowcoder.demo1.controller.interceptor;

import com.nowcoder.demo1.entity.LoginTicket;
import com.nowcoder.demo1.entity.User;
import com.nowcoder.demo1.service.UserService;
import com.nowcoder.demo1.util.CookieUtil;
import com.nowcoder.demo1.util.HostHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.Date;

/**
 * 过滤器，通过查询登录凭证信息返回给HostHolder容器与凭证对应的用户
 */
@Component
public class LoginTicketInterceptor implements HandlerInterceptor {
    @Autowired
    private UserService userService;
    @Autowired
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//        从cookie中获取凭证
        String ticket = CookieUtil.getValue(request, "ticket");

        if (ticket!=null){
//            查询凭证
            LoginTicket loginTicket = userService.findLoginTicket(ticket);
//            验证凭证是否有效
            if (loginTicket != null && loginTicket.getStatus() == 0 && loginTicket.getExpired().after(new Date())){
                User user = userService.findUserById(loginTicket.getUserId());
                hostHolder.setUser(user);
            }
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User user = hostHolder.getUser();
        if (user != null && modelAndView != null){
            modelAndView.addObject("loginUser",user);
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        hostHolder.clear();
    }
}
