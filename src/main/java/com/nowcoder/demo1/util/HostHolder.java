package com.nowcoder.demo1.util;

import com.nowcoder.demo1.entity.User;
import org.springframework.stereotype.Component;

/**
 * 代替Session储存用户信息，ThreadLocal采用线程隔离的方式存数据，可以避免多线程之间发生数据访问冲突
 */
@Component
public class HostHolder {
//    使用范式方便管理
    private ThreadLocal<User> users = new ThreadLocal<>();

    public void setUser(User user){
        users.set(user);
    }
    public User getUser(){
        return users.get();
    }
    public void clear(){
        users.remove();
    }
}
