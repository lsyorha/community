package com.nowcoder.demo1.controller.interceptor;

import com.nowcoder.demo1.annotation.LoginRequired;
import com.nowcoder.demo1.util.HostHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.lang.reflect.Method;

@Component
public class LoginRequireInterceptor implements HandlerInterceptor {
    @Autowired
    private HostHolder hostHolder;

    /**
     *已登录用户访问登录或注册页面会重定向回首页，未登录用户访问含自定义注解方法时会跳转到登录页面
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//通过传入的 handler 参数来获取当前请求所对应的请求处理器。如果是controller类的方法则Spring MVC 的 HandlerAdapter会将处理器封装为HandlerMethod对象
        if (handler instanceof HandlerMethod){
            HandlerMethod handlerMethod =(HandlerMethod) handler;
            Method method = handlerMethod.getMethod();
            LoginRequired loginRequired = method.getAnnotation(LoginRequired.class);

/*            if ( hostHolder.getUser() != null &&
                    request.getRequestURI().contains("login") || request.getRequestURI().contains("register")) {
//              如果已登录用户访问注册或者登录页时会跳转到首页,通过前端页面隐藏即可，这里画蛇添足反倒注册链接失效
                response.sendRedirect(request.getContextPath() + "/index");
                return false;
            }*/
            if (loginRequired != null && hostHolder.getUser() == null){
//                用户未登录访问用户修改页则跳转到登录页
                request.getSession().setAttribute("loginMsg","请先登录");
                response.sendRedirect(request.getContextPath() + "/login");
                return false;
            }
        }

        return true;
    }

}
