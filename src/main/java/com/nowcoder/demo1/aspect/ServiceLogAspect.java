package com.nowcoder.demo1.aspect;

import com.nowcoder.demo1.entity.User;
import com.nowcoder.demo1.util.HostHolder;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * AOP面向切面，在用户对业务层进行操作前执行前添加日志
 */
@Component
@Aspect
public class ServiceLogAspect {
    private static final Logger logger = LoggerFactory.getLogger(ServiceLogAspect.class);

    @Autowired
    private HostHolder hostHolder;

    @Pointcut("execution(* com.nowcoder.demo1.service.*.*(..))")
    public void pointcut(){}

    @Before("pointcut()")
    public void before(JoinPoint joinPoint){
//        用户[ip地址]在[时间]访问了[类]
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

//        kafka消费时没走Controller，但有经过service
        if (attributes == null){
            return;
        }

        HttpServletRequest request = attributes.getRequest();

        String ip = request.getRemoteHost();
        String now = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date());
        String target = joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName();

        User user = hostHolder.getUser();
        if (user!=null){
            logger.info(String.format("%s于[%s],在[%s],访问了[%s]。",user.getUsername(),ip,now,target));
        }else {
            logger.info(String.format("未登录用户于[%s],在[%s],访问了[%s]。",ip,now,target));
        }
    }
}
