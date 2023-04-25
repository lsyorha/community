package com.nowcoder.demo1.controller;

import com.nowcoder.demo1.annotation.LoginRequired;
import com.nowcoder.demo1.entity.DiscussPost;
import com.nowcoder.demo1.entity.User;
import com.nowcoder.demo1.service.DiscussPostService;
import com.nowcoder.demo1.util.CommunityUtil;
import com.nowcoder.demo1.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;

@Controller
@RequestMapping("/discuss")
public class DiscussPostController {
    @Autowired
    private DiscussPostService discussPostService;
    @Autowired
    private HostHolder hostHolder;

    /**
     * 接收用户传入的帖子标题和内容，调用业务层处理标题和内容，完成后返回Jason字符串
     * @param title
     * @param content
     * @return
     */
    @RequestMapping(value = "/add",method = RequestMethod.POST)
    @ResponseBody
    @LoginRequired
    public String addDiscussPost(String title,String content){
        User user = hostHolder.getUser();
        DiscussPost post = new DiscussPost();

        post.setUserId(user.getId());
        post.setTitle(title);
        post.setContent(content);
        post.setCreateTime(new Date());

        post.setType(0);
        post.setStatus(0);
        post.setScore(0);
        post.setCommentCount(0);
        discussPostService.addDiscussPost(post);

//        插入错误的情况后面通过AOP实现
        return CommunityUtil.getJsonString(0,"发布成功！");
    }
}
