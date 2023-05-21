package com.nowcoder.demo1.config;

import com.nowcoder.demo1.util.CommunityConstant;
import com.nowcoder.demo1.util.CommunityUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

import java.io.IOException;
import java.io.PrintWriter;

@Configuration
public class SecurityConfig implements CommunityConstant {
// WebSecurity配置，官网：https://spring.io/blog/2022/02/21/spring-security-without-the-websecurityconfigureradapter#ldap-authentication
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer(){
        return (web) ->web.ignoring().requestMatchers("/resources/**");
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//                .authorizeRequests()替换为authorizeHttpRequests，访问以下路径需特定权限
        http.authorizeHttpRequests((authz) -> {
                            try {
                                authz.requestMatchers(
                                        "/user/setting",
                                        "/user/upload",
                                        "/user/updatePassword",
                                        "/discuss/**",
                                        "/comment/add/**",
                                        "/letter/**",
                                        "/notice/**",
                                        "/like",
                                        "/follow",
                                        "/unfollow"
                                        )
                //                        指定用户需要多个权限中的一个。
                                        .hasAnyAuthority(
                                                AUTHORITY_USER,
                                                AUTHORITY_ADMIN,
                                                AUTHORITY_MODERATOR
                                        )
                //                        任何人都可以访问的路径
                                        .anyRequest().permitAll();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                );
//      不关闭csrf且未在js提交时返回请求头的话异步请求都会出现权限不足的情况
        http.csrf().disable();
//        权限不够时的处理
        http.exceptionHandling()
                .authenticationEntryPoint(new AuthenticationEntryPoint() {
//                    未登录
                    @Override
                    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
                        String xRequestedWith = request.getHeader("x-requested-with");
//                        收到异步请求时，比如发帖之类的
                        if ("XMLHttpRequest".equals(xRequestedWith)){
                            response.setContentType("application/plain;charset=utf-8");
                            PrintWriter writer = response.getWriter();
                            writer.write(CommunityUtil.getJsonString(403,"请先登录！"));
                        }else {
                            response.sendRedirect(request.getContextPath() + "/login");
                        }
                    }
                })
//                权限不足
                .accessDeniedHandler(new AccessDeniedHandler() {
                    @Override
                    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
                        String xRequestedWith = request.getHeader("x-requested-with");
//                        收到异步请求时，比如点赞关注等功能实现
                        if ("XMLHttpRequest".equals(xRequestedWith)){
                            response.setContentType("application/plain;charset=utf-8");
                            PrintWriter writer = response.getWriter();
                            writer.write(CommunityUtil.getJsonString(403,"权限尚未开通！"));
                        }else {
                            response.sendRedirect(request.getContextPath() + "/denied");
                        }
                    }
                });
//        security底层默认会拦截/logout请求进行注销处理，这里覆盖默认逻辑就能执行我们自定义的注销功能
        http.logout().logoutUrl("/securityLogout");
        return http.build();
    }

    // 在SecurityConfig中增加配置SecurityContextRepository，用于向SecurityRepository添加授权
    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }
//    与SecurityContextRepository相反，这里是销毁授权
    @Bean
    public SecurityContextLogoutHandler securityContextLogoutHandler() {
        return new SecurityContextLogoutHandler();
    }
}
