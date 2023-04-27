package com.nowcoder.demo1.dao;

import com.nowcoder.demo1.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CommentMapper {
    /**
     * 查询当前类型帖子，根据分页要求返回
     * @param entityType
     * @param entityId
     * @param offset
     * @param limit
     * @return
     */
    List<Comment> selectCommentsByEntity(
            @Param("entityType") int entityType,@Param("entityId") int entityId,@Param("offset") int offset,@Param("limit") int limit);

    int selectCommentCountByEntity(@Param("entityType") int entityType,@Param("entityId") int entityId);
}
