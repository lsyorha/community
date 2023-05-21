package com.nowcoder.demo1.controller;

import com.google.code.kaptcha.Producer;
import com.nowcoder.demo1.entity.User;
import com.nowcoder.demo1.service.UserService;
import com.nowcoder.demo1.util.CommunityConstant;
import com.nowcoder.demo1.util.CommunityUtil;
import com.nowcoder.demo1.util.RedisKeyUtil;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
public class LoginController implements CommunityConstant {
    @Autowired
    private UserService userService;
    @Autowired
    private Producer kaptcha;
    @Autowired
    private RedisTemplate redisTemplate;
//    管理SecurityRepository内保存信息
    @Autowired
    private SecurityContextLogoutHandler securityContextLogoutHandler;
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
//    服务器路径
    @Value("${server.servlet.context-path}")
    private String contextPath;

    @RequestMapping(value = "/register",method = RequestMethod.GET)
    public String getRegister(){
        return "/site/register";
    }

    @RequestMapping(value = "/login",method = RequestMethod.GET)
    public String getLoginPage(){
        return "site/login";
    }

    @RequestMapping(value = "/register",method = RequestMethod.POST)
    public String register(Model model, User user){
        Map<String, Object> map = userService.register(user);
        if (map == null || map.isEmpty()){
            model.addAttribute("msg","注册成功，我们已发送一封邮件到你所填写的邮箱，请尽快激活");
            model.addAttribute("target","/index");
            return "site/operate-result";
        }else {
            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            model.addAttribute("emailMsg",map.get("emailMsg"));
            return "site/register";
        }
    }

//    返回路径格式 http://localhost:8080/community/activation/用户id/code
    @RequestMapping(value = "/activation/{userId}/{code}",method = RequestMethod.GET)
    public String activation(Model model, @PathVariable("userId") int userId,@PathVariable("code") String code){
        int result = userService.activation(userId, code);

        if (result == ACTIVATION_SUCCESS){
            model.addAttribute("msg","激活成功，当前账号可以正常使用");
            model.addAttribute("target","/login");
        } else if (result == ACTIVATION_REPEAT) {
            model.addAttribute("msg","当前用户已经激活，请勿重复操作");
            model.addAttribute("target","/login");
        } else {
            model.addAttribute("msg","激活码错误，激活失败");
            model.addAttribute("target","/index");
        }
        return "/site/operate-result";
    }
//返回验证码
    @RequestMapping(path = "/kaptcha",method = RequestMethod.GET)
    public void kaptcha(HttpServletResponse response, HttpSession session){
//        生成验证码
        String text = kaptcha.createText();
        BufferedImage image = kaptcha.createImage(text);
//        将验证码存入session，保证安全性,浪费资源，存Redis丢Cookie中去
//        session.setAttribute("kaptcha",text);
//        随机生成验证码的归属者
        String kaptchaOwner = CommunityUtil.generateUUID();
        Cookie cookie = new Cookie("kaptchaOwner", kaptchaOwner);
        cookie.setMaxAge(60);
        cookie.setPath(contextPath);
        response.addCookie(cookie);
//        将验证码存到Redis
        String kaptchaKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
//        验证码生效时间60秒
        redisTemplate.opsForValue().set(kaptchaKey,text,60, TimeUnit.SECONDS);
//        向浏览器返回验证码图片
        response.setContentType("image/png");
        try {
            ServletOutputStream outputStream = response.getOutputStream();
            ImageIO.write(image,"png",outputStream);
        } catch (IOException e) {
            logger.error("验证码响应失败"+e.getMessage());
        }
    }

    /**
     * 相同路径可以使用不同的方法实现跳转，登录失败则回到登录页面，同时返回错误信息，反之则重定向到主页面
     * @param username
     * @param password
     * @param rememberme
     * @param model
     * @param kaptchaOwner
     * @param response
     * @return
     */
    @RequestMapping(path = "/login",method = RequestMethod.POST)
    public String login(String username,String password,String code,boolean rememberme,
                        Model model/*HttpSession session*/,HttpServletResponse response
                        ,@CookieValue("kaptchaOwner") String kaptchaOwner){
//        验证码效验
//        String kaptcha =(String) session.getAttribute("kaptcha");
        String kaptcha = null;
        if (StringUtils.isNotBlank(kaptchaOwner)){
            String kaptchaKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
            kaptcha = (String) redisTemplate.opsForValue().get(kaptchaKey);
        }
//        isBlank检查字符串是否为空或只有空格
        if (StringUtils.isBlank(kaptcha)||StringUtils.isBlank(code)||!kaptcha.equalsIgnoreCase(code)){
            model.addAttribute("codeMsg","验证码错误");
            return "/site/login";
        }

//        账号验证和处理是否保留长期登录
        int expiredSeconds = (rememberme) ? REMEMBER_EXPIRED_SECONDS : DEFAULT_EXPIRED_SECONDS;
        Map<String, Object> map = userService.login(username, password, expiredSeconds);
        if (map.containsKey("ticket")){
            Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
            cookie.setPath(contextPath);
            cookie.setMaxAge(expiredSeconds);
            response.addCookie(cookie);
            return "redirect:/index";
        }else {
            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            return "/site/login";
        }
    }
//    注销
    @RequestMapping(path = "/logout",method = RequestMethod.GET)
    public  String logout(@CookieValue("ticket") String ticket, HttpServletRequest request, HttpServletResponse response, Authentication authentication){
        userService.logout(ticket);
        securityContextLogoutHandler.logout(request,response,authentication);
        return "redirect:/login";
    }
}
