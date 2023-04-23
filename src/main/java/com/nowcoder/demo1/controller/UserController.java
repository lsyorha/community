package com.nowcoder.demo1.controller;

import com.nowcoder.demo1.entity.User;
import com.nowcoder.demo1.service.UserService;
import com.nowcoder.demo1.util.CommunityUtil;
import com.nowcoder.demo1.util.CookieUtil;
import com.nowcoder.demo1.util.HostHolder;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;

@Controller
@RequestMapping("/user")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    @Value("${community.path.domain}")
    private String domain;
    @Value("${server.servlet.context-path}")
    private String contextPath;
    @Value("${community.upload.path}")
    private String uploadPath;
    @Autowired
    private UserService userService;
    @Autowired
    private HostHolder hostHolder;
    @RequestMapping(value = "/setting",method = RequestMethod.GET)
    public String getSettingPage(){
        return "/site/setting";
    }

    @RequestMapping(value = "/upload",method = RequestMethod.POST)
//    MultipartFile接口继承了InputStreamSource接口
    public String uploadHeader(MultipartFile headerImage, Model model){
        if (headerImage == null){
            model.addAttribute("headerMsg","头像不能为空");
            return "/site/setting";
        }
//        获取返回文件类型
        String filename = headerImage.getOriginalFilename();
//        获取文件格式
        String suffix = filename.substring(filename.lastIndexOf("."));
        if (StringUtils.isBlank(suffix)){
            model.addAttribute("error","文件格式错误！");
            return "/site/setting";
        }
//        生成随机文件名
        filename = CommunityUtil.generateUUID() + suffix;
//        确定头像存放路径
        File dest = new File(uploadPath + "/" + filename);
        try {
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("文件上传失败:"+e.getMessage());
            throw new RuntimeException("文件上传失败，服务器发生异常"+e);
        }
//        更新当前用户头像路径
//        http://localhost:8080/community/user/header/xxx.png
        User user = hostHolder.getUser();
        String headerUrl = domain + contextPath + "/user/header/" + filename;
        userService.updateHeader(user.getId(),headerUrl);

        return "redirect:/index";
    }

    @RequestMapping(path = "/header/{fileName}",method = RequestMethod.GET)
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response){
//        服务器存放路径
        fileName = uploadPath + "/" +fileName;
//        文件后缀
        String suffix = fileName.substring(fileName.lastIndexOf("."));
//        响应图片
        response.setContentType("image/" + suffix);
        try (
                FileInputStream fis = new FileInputStream(fileName);
                OutputStream os =response.getOutputStream();
        ) {
            byte[] buffer = new byte[1024];
            int b = 0;
            while ((b = fis.read(buffer))!=-1){
                os.write(buffer,0,b);
            }
        }catch (IOException e){
            logger.error("头像读取失败："+e.getMessage());
        }
    }

    /**
     * 用户修改密码，修改成功则跳转到登录页面,同时把登录凭证的状态设为1
     * @param oldPassword
     * @param newPassword
     * @param confirmPassword
     * @param model
     * @param request
     * @return
     */
    @RequestMapping(path = "/updatePassword",method = RequestMethod.POST)
    public String updatePassword(String oldPassword, String newPassword, String confirmPassword,
                                 Model model, HttpServletRequest request){
        User user = hostHolder.getUser();
        if (oldPassword==null){
            model.addAttribute("oldPasswordMsg","密码不能为空");
            return "/site/setting";
        }
        if (newPassword == null || confirmPassword == null){
            model.addAttribute("newPasswordMsg","密码不能为空");
            return "/site/setting";
        }
        if (!newPassword.equals(confirmPassword)){
            model.addAttribute("newPasswordMsg","两次输入的密码不一致!");
            return "/site/setting";
        }

        oldPassword = CommunityUtil.md5(oldPassword + user.getSalt());
        if (!user.getPassword().equals(oldPassword)){
            model.addAttribute("oldPasswordMsg","密码错误");
            return "/site/setting";
        }

        newPassword = CommunityUtil.md5(newPassword + user.getSalt());
        userService.updatePassword(user.getId(),newPassword);
        String ticket = CookieUtil.getValue(request, "ticket");
        userService.logout(ticket);
        return "redirect:/login";
    }

}
