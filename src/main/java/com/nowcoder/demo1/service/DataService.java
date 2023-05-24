package com.nowcoder.demo1.service;

import com.nowcoder.demo1.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class DataService {
    @Autowired
    private RedisTemplate redisTemplate;

//    格式化日期 "年月日"
    private SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");

//    将指定的ip计入Redis的UV(每日访客)中
    public void recordUV(String ip){
        String redisKey = RedisKeyUtil.getUVKey(df.format(new Date()));
        redisTemplate.opsForHyperLogLog().add(redisKey, ip);
    }

//    统计指定日期的UV数
    public long calculateUV(Date start, Date end){
        if (start == null || end == null){
            throw new IllegalArgumentException("参数不能为空");
        }
        if (start.after(end)){
            throw new IllegalArgumentException("开始日期不能大于结束日期");
        }
//        整理指定日期范围内的UV（访客）数
        List<String> keyList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();

        calendar.setTime(start);
        while (!calendar.getTime().after(end)){
            String key = RedisKeyUtil.getUVKey(df.format(calendar.getTime()));
            keyList.add(key);
            calendar.add(Calendar.DATE, 1);
        }

//        合并上述数据
        String redisKey = RedisKeyUtil.getUVKey(df.format(start), df.format(end));
        redisTemplate.opsForHyperLogLog().union(redisKey,keyList.toArray());

//        返回统计数据
        return redisTemplate.opsForHyperLogLog().size(redisKey);
    }

//    将指定用户计入DAU(日活跃用户)
    public void recordDAU(int userId){
        String redisKey = RedisKeyUtil.getDAUKey(df.format(new Date()));
        redisTemplate.opsForValue().setBit(redisKey, userId , true);
    }

//    统计指定日期范围内的DAU数
    public long calculateDAU(Date start, Date end){

        if (start == null || end == null){
            throw new IllegalArgumentException("参数不能为空");
        }
        if (start.after(end)){
            throw new IllegalArgumentException("开始日期不能大于结束日期");
        }

//        整理该日期范围内的key，bitmap按字节流读取
        List<byte[]> keyList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        while (!calendar.getTime().after(end)){
            String key = RedisKeyUtil.getDAUKey(df.format(calendar.getTime()));
            keyList.add(key.getBytes());
            calendar.add(Calendar.DATE,1);
        }

//        进行或运算
        return (long) redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                String redisKey = RedisKeyUtil.getDAUKey(df.format(start), df.format(end));
                connection.stringCommands().bitOp(RedisStringCommands.BitOperation.OR,
                        redisKey.getBytes(), keyList.toArray(new byte[0][0]));
                return connection.stringCommands().bitCount(redisKey.getBytes());
            }
        });
    }
}
