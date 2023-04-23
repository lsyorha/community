package com.nowcoder.demo1.util;

/**
 * 定义常数
 */
public interface CommunityConstant {
    /**
     * 激活成功
     */
    int ACTIVATION_SUCCESS = 0;
    /**
     * 重复激活
     */
    int ACTIVATION_REPEAT = 1;
    /**
     * 激活失败
     */
    int ACTIVATION_FAILURE = 2;
    /**
     * 默认登录信息保留12小时
     */
    int DEFAULT_EXPIRED_SECONDS = 3600 * 12;
    /**
     * 勾选记住登录信息保留30天
     */
    int REMEMBER_EXPIRED_SECONDS = 3600 * 24 * 30;
}
