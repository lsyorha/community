package com.nowcoder.demo1.service;

import com.nowcoder.demo1.dao.DiscussPostMapper;
import com.nowcoder.demo1.entity.DiscussPost;
import com.nowcoder.demo1.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class DiscussPostService {
    @Autowired
    private DiscussPostMapper discussPostMapper;
    @Autowired
    private SensitiveFilter sensitiveFilter;

    public List<DiscussPost> findDiscussPosts(int userId, int offset, int limit){
        return discussPostMapper.selectDiscussPosts(userId,offset,limit);
    }
    public int findDiscussPostRows(int userId){
        return discussPostMapper.selectDiscussPostRows(userId);
    }

    public int addDiscussPost(DiscussPost post){
        //        敏感词处理
        post.setTitle(sensitiveFilter.filter(post.getTitle()));
        post.setContent(sensitiveFilter.filter(post.getContent()));

//        防html注入进行转义处理
        post.setTitle(HtmlUtils.htmlEscape(post.getTitle()));
        post.setContent(HtmlUtils.htmlEscape(post.getContent()));
        return discussPostMapper.addDiscussPost(post);
    }

    public DiscussPost findDiscussPost(int id){
        return discussPostMapper.selectDiscussPost(id);
    }
}
