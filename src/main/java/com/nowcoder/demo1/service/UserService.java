package com.nowcoder.demo1.service;

//import com.nowcoder.demo1.dao.LoginTicketMapper;
import com.nowcoder.demo1.dao.UserMapper;
import com.nowcoder.demo1.entity.LoginTicket;
import com.nowcoder.demo1.entity.User;
import com.nowcoder.demo1.util.CommunityConstant;
import com.nowcoder.demo1.util.CommunityUtil;
import com.nowcoder.demo1.util.MailUtil;
import com.nowcoder.demo1.util.RedisKeyUtil;
import io.micrometer.common.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
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
    //调用mvc类
    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
//    private LoginTicketMapper loginTicketMapper;
    private RedisTemplate redisTemplate;
    @Value("${server.servlet.context-path}")
    private String contextPath;
    @Value("${community.path.domain}")
    private String domain;

    /**
     * 通过id查询用户
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

    /**
     * 成功则返回ticket
     * @param username
     * @param password
     * @param expiredSeconds 过期日期
     * @return
     */
    public Map<String, Object> login(String username, String password, int expiredSeconds){
        Map<String, Object> map = new HashMap<>();
//        空值处理
        if (StringUtils.isBlank(username)){
            map.put("usernameMsg","账号不能为空");
            return map;
        }
        if (StringUtils.isBlank(password)){
            map.put("passwordMsg","密码不能为空");
            return  map;
        }
//        账号验证
        User user = userMapper.selectByName(username);
        if (user == null){
            map.put("usernameMsg","该账号不存在");
            return map;
        }
//        验证密码这里要先进行加密后才能跟数据库存储的做对比
        password = CommunityUtil.md5(password + user.getSalt());
        if (!user.getPassword().equals(password)){
            map.put("passwordMsg","密码错误，请重新输入");
            return map;
        }

//        生成登录凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
//        随机生成
        loginTicket.setTicket(CommunityUtil.generateUUID());
//        验证成功
        loginTicket.setStatus(0);
//        保留时间等于当前系统时间加设定的保留时间
//        注意！！！这里不添加l的话运算会被当作int类型进行相加，结果很可能溢出（亲测保留一个月时溢出）
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000L));
//        loginTicket的id值从redis中获取
        String ticketKeyId = RedisKeyUtil.getTicketKeyId();
        redisTemplate.opsForValue().increment(ticketKeyId);
        int id = (int) redisTemplate.opsForValue().get(ticketKeyId);
        loginTicket.setId(id);
//        loginTicketMapper.insertLoginTicket(loginTicket);
//        登录凭证存入Redis
        String redisKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
        redisTemplate.opsForValue().set(redisKey,loginTicket);

        map.put("ticket",loginTicket.getTicket());
        return map;
    }
//用户注销
    public void logout(String ticket){
//        loginTicketMapper.updateStatus(ticket,1);
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(redisKey);
//        注销时更新状态
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(redisKey,loginTicket);
    }

    public LoginTicket findLoginTicket(String ticket){
//        return loginTicketMapper.selectByTicket(ticket);
//        获取登录凭证的请求经常调用
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        return (LoginTicket) redisTemplate.opsForValue().get(redisKey);
    }

    public int updateHeader(int id,String headerUrl){
        return userMapper.updateHeader(id, headerUrl);
    }

    public int updatePassword(int id,String password){
        return userMapper.updatePassword(id,password);
    }

    public User findUserByName(String userName){
        return userMapper.selectByName(userName);
    }

}
