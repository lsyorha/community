package com.nowcoder.demo1.service;

import com.nowcoder.demo1.dao.UserMapper;
import com.nowcoder.demo1.entity.User;
import com.nowcoder.demo1.util.CommunityConstant;
import com.nowcoder.demo1.util.CommunityUtil;
import com.nowcoder.demo1.util.MailUtil;
import io.micrometer.common.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class UserService implements CommunityConstant {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailUtil mailUtil;
    @Autowired
    private TemplateEngine templateEngine;
    @Value("${server.servlet.context-path}")
    private String contextPath;
    @Value("${community.path.domain}")
    private String domain;

    /**
     * 查询用户id
     * @param id
     * @return
     */
    public User findUserById(int id) {
        return userMapper.selectById(id);
    }

    /**
     * 注册功能，接收用户注册信息，包括名称，密码，邮箱
     * 默认用户类型、状态、创建日期以及对密码进行加密处理，同时发送邮件到用户填写的邮箱地址等待用户激活
     * 注册成功返回的map值为null
     * @param user
     * @return
     */
    public Map<String,Object> register(User user){
        Map<String, Object> map = new HashMap<>();
//        空值处理
        if (user==null){
            throw new IllegalArgumentException("参数不能为空！");
        }
        if (StringUtils.isBlank(user.getUsername())){
            map.put("usernameMsg","用户名不能为空");
            return map;
        }
        if (StringUtils.isBlank(user.getPassword())){
            map.put("passwordMsg","密码不能为空");
            return map;
        }
        if (StringUtils.isBlank(user.getEmail())){
            map.put("emailMsg","邮箱不能为空");
            return map;
        }
//        验证数据库中是否存在账号,注意数据库要是主键冲突也会出现报错
        User u = userMapper.selectByName(user.getUsername());
        if (u != null){
            map.put("usernameMsg","用户已存在");
            return map;
        }
//        验证邮箱是否已被注册
        u = userMapper.selectByEmail(user.getEmail());
        if (u != null){
            map.put("emailMsg","邮箱已被注册");
            return map;
        }
//        如果验证都通过则进行用户注册
//        前台用户只需填写用户名，密码，邮箱，其他的需要后台实现
//        调用自定义的无序字符串，拼接到加密后的密码后保证密码可靠性
        user.setSalt(CommunityUtil.generateUUID().substring(0,5));
        user.setPassword(CommunityUtil.md5(user.getPassword()+user.getSalt()));
//        默认普通用户，未激活
        user.setType(0);
        user.setStatus(0);
//        激活码也是调用自定义方法实现
        user.setActivationCode(CommunityUtil.generateUUID());
//        头像随机牛客论坛的url
        user.setHeaderUrl(String.format("https://images.nowcoder.com/head/%dt.png",new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

//        激活邮件
        Context context = new Context();
        context.setVariable("email",user.getEmail());
//        http://localhost:8080/community/activation/用户id/code
        String url = domain + contextPath
                + "/activation/" +
                user.getId()+"/"+user.getActivationCode();
        context.setVariable("url",url);
        String content = templateEngine.process("/mail/activation", context);
        mailUtil.sentMail(user.getEmail(),"激活邮件",content);

        return map;
    }

    /**
     * 用户激活，返回0表示成功激活，1,表示重复激活，2表示激活失败
     * @param userId
     * @param code
     * @return
     */
    public int activation(int userId,String code){
        User user = userMapper.selectById(userId);
        if (user.getStatus() == 1){
            return ACTIVATION_REPEAT;
        } else if (user.getActivationCode().equals(code)) {
            userMapper.updateStatus(userId,1);
            return ACTIVATION_SUCCESS;
        }else {
            return ACTIVATION_FAILURE;
        }
    }

}
