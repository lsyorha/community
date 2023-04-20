package com.nowcoder.demo1.controller;

import com.nowcoder.demo1.entity.User;
import com.nowcoder.demo1.service.UserService;
import com.nowcoder.demo1.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Map;

@Controller
public class LoginController implements CommunityConstant {
    @Autowired
    private UserService userService;

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

}
