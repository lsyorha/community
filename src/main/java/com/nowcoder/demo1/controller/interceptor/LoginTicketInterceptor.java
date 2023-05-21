package com.nowcoder.demo1.controller.interceptor;

import com.nowcoder.demo1.entity.LoginTicket;
import com.nowcoder.demo1.entity.User;
import com.nowcoder.demo1.service.UserService;
import com.nowcoder.demo1.util.CookieUtil;
import com.nowcoder.demo1.util.HostHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.context.SecurityContextRepository;
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
    // 在LoginTicketInterceptor中注入这个Bean
    @Autowired
    private SecurityContextRepository securityContextRepository;
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//        从cookie中获取凭证
        String ticket = CookieUtil.getValue(request, "ticket");

        if (ticket!=null){
//            查询凭证
            LoginTicket loginTicket = userService.findLoginTicket(ticket);
//            验证凭证是否有效
            if (loginTicket != null && loginTicket.getStatus() == 0 && loginTicket.getExpired().after(new Date())){
//                根据凭证查询用户
                User user = userService.findUserById(loginTicket.getUserId());
//                本次请求发起的用户
                hostHolder.setUser(user);
//                构建用户认证结果，存入SecurityContext，便于Security进行授权
                Authentication authentication =
                        new UsernamePasswordAuthenticationToken(user, user.getPassword(), userService.getAuthorities(user.getId()));
                SecurityContextHolder.setContext(new SecurityContextImpl(authentication));
//                增加保存securityContext
                securityContextRepository.saveContext(SecurityContextHolder.getContext(),request,response);
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
//    preHandle执行成功，在呈现视图后调用
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        hostHolder.clear();

//        SecurityContextHolder.clearContext();
    }
}
