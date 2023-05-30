package com.nowcoder.demo1.actuator;

import com.nowcoder.demo1.util.CommunityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Component
// id内容就是设置的访问路径
@Endpoint(id = "database")
public class DatabaseEndPoint {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseEndPoint.class);

    @Autowired
    private DataSource dataSource;

//    通过get访问，相反WriteOperation是通过post访问
    @ReadOperation
    public String checkConnection(){
        try(
//                在括号里默认加载时会添加final，免去人为关闭
                Connection conn = dataSource.getConnection();
                ) {
            return CommunityUtil.getJsonString(0,"获取连接成功");
        } catch (SQLException e) {
            logger.error("获取连接失败：" + e.getMessage());
           return CommunityUtil.getJsonString(1,"获取连接失败");
        }
    }

}
