package com.nowcoder.demo1.dao.elasticsearch;

import com.nowcoder.demo1.entity.DiscussPost;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;
/*
* ElasticsearchRepository<DiscussPost, Integer>中的
* DiscussPost是接口要处理的实体类
* Integer是实体中的主键类型
* ElasticsearchRepository父接口，实现定义了对es服务器的增删改查，springboot会自动实现该接口
 */
@Repository
public interface DiscussPostRepository extends ElasticsearchRepository<DiscussPost, Integer> {

}
