package com.nowcoder.demo1.service;

import com.nowcoder.demo1.dao.CommentMapper;
import com.nowcoder.demo1.entity.Comment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentService {
    @Autowired
    private CommentMapper commentMapper;

    public List<Comment>  findCommentsByEntity(int entityType, int entityId, int offset, int limit){
        return commentMapper.selectCommentsByEntity(entityType, entityId, offset, limit);
    }

    public int findCommentCountByEntity(int entityType, int entityId){
        return commentMapper.selectCommentCountByEntity(entityType, entityId);
    }
}
