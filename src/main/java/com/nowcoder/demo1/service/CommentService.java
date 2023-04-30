package com.nowcoder.demo1.service;

import com.nowcoder.demo1.dao.CommentMapper;
import com.nowcoder.demo1.entity.Comment;
import com.nowcoder.demo1.util.CommunityConstant;
import com.nowcoder.demo1.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class CommentService implements CommunityConstant {
    @Autowired
    private CommentMapper commentMapper;
//    添加回复需要用到过滤器以及查看回复类型
    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Autowired
    private DiscussPostService discussPostService;

    public List<Comment>  findCommentsByEntity(int entityType, int entityId, int offset, int limit){
        return commentMapper.selectCommentsByEntity(entityType, entityId, offset, limit);
    }

    public int findCommentCountByEntity(int entityType, int entityId){
        return commentMapper.selectCommentCountByEntity(entityType, entityId);
    }

//    涉及俩个表操作，添加事务处理
    @Transactional(isolation = Isolation.READ_COMMITTED,propagation = Propagation.REQUIRED)
    public int addComment(Comment comment){
        if (comment == null){
            throw new IllegalArgumentException("参数不能为空");
        }

//        添加评论前先对评论进行过滤
        comment.setContent(sensitiveFilter.filter(comment.getContent()));
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        int rows = commentMapper.insertComment(comment);

//        更新帖子评论数
        if (comment.getEntityType() == ENTITY_TYPE_POST){
            int count = commentMapper.selectCommentCountByEntity(comment.getEntityType(),comment.getEntityId());
//这里的entityId是外键，对于帖子表则是主键
            discussPostService.updateCommentCount(comment.getEntityId(),count);
        }
        return rows;
    }
}
