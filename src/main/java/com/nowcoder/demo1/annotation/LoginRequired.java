package com.nowcoder.demo1.annotation;

import java.lang.annotation.*;

/**
 * 标识在方法上，含该标记的方法需先登录才能访问
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LoginRequired {
}
