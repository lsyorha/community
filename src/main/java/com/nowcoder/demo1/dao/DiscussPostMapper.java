package com.nowcoder.demo1.dao;

import com.nowcoder.demo1.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


@Mapper
public interface DiscussPostMapper {

    List<DiscussPost> selectDiscussPosts(@Param("userId") int userId,@Param("offset") int offset,
                                         @Param("limit") int limit);
//    List<DiscussPost> selectDiscussPosts( int userId, int offset, int limit);

    // @Param注解用于给参数取别名,
    // 如果有多个参数,不加别名会报错，单个参数的话可以不加
//    int selectDiscussPostRows(@Param("userId") int userId);
    int selectDiscussPostRows(int userId);

}
