package com.nowcoder.demo1.dao;

import com.nowcoder.demo1.entity.LoginTicket;
import org.apache.ibatis.annotations.*;
//  登录凭证访问程度很频繁，所以考虑存到redis实现，这样就可以把login_ticket表舍弃了
@Mapper
@Deprecated
public interface LoginTicketMapper {
//
    @Insert({
            "insert into community.login_ticket(user_id,ticket,status,expired) ",
            "values(#{userId},#{ticket},#{status},#{expired})"
    })
//    id自增
    @Options(useGeneratedKeys = true,keyProperty = "id")
    int insertLoginTicket(LoginTicket loginTicket);

    @Select({
            "select id,user_id,ticket,status,expired ",
            "from community.login_ticket ",
            "where ticket = #{ticket}"
    })
    LoginTicket selectByTicket(String ticket);

    //    动态查询
/*    @Update({
            "<script>",
            "update community.login_ticket set status = #{status} ",
            "where ticket = #{ticket} ",
            "<if test=\"ticket!=null\"> ",
            "and 1=1 ",
            "</if> ",
            "</script>"
    })*/
    @Update({
            "update community.login_ticket set status = #{status} ",
            "where ticket = #{ticket}"
    })
//    插入时要指定参数名，否则会找不到
    int updateStatus(@Param("ticket") String ticket,@Param("status") int status);

}
