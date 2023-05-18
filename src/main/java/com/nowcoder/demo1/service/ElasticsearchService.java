package com.nowcoder.demo1.service;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.nowcoder.demo1.dao.elasticsearch.DiscussPostRepository;
import com.nowcoder.demo1.entity.DiscussPost;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHitSupport;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.SearchPage;
import org.springframework.data.elasticsearch.core.query.HighlightQuery;
import org.springframework.data.elasticsearch.core.query.highlight.Highlight;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightField;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ElasticsearchService {
    @Autowired
    private DiscussPostRepository discussRepository;
    @Autowired
    private ElasticsearchTemplate elasticTemplate;

    public void saveDiscussPost(DiscussPost discussPost){
        discussRepository.save(discussPost);
    }

    public void deleteDiscussPost(int id){
        discussRepository.deleteById(id);
    }

    public SearchPage<DiscussPost> searchDiscussPost(String keyword, int current, int limit) {

        HighlightField titleHighlightField = new HighlightField("title");
        HighlightField contentHighlightField = new HighlightField("content");

        Highlight titleHighlight = new Highlight(List.of(titleHighlightField, contentHighlightField));

        NativeQuery searchQuery = new NativeQueryBuilder()
//                查询这里直接使用了lambda表达式，不用写实现类
                .withQuery(Query.of(q -> q.multiMatch(mq -> mq.query(keyword).fields("title","content"))))
//                倒序直接用Sort.by后面加descending()就行
                .withSort(Sort.by("type", "score", "createTime").descending())
                .withPageable(PageRequest.of(current, limit))
                .withHighlightQuery(
//                        默认添加em标签，前端显示就是呈现红色
                        new HighlightQuery(titleHighlight, DiscussPost.class)
                )
                .build();

        SearchHits<DiscussPost> search = elasticTemplate.search(searchQuery, DiscussPost.class);
        SearchPage<DiscussPost> page = SearchHitSupport.searchPageFor(search, searchQuery.getPageable());

        if (!page.isEmpty()){
            for (SearchHit<DiscussPost> hit : page) {
                DiscussPost discussPost = hit.getContent();
//                获取高亮部分
                List<String> title = hit.getHighlightFields().get("title");
                if (title!=null){
                    discussPost.setTitle(title.get(0));
                }
                List<String> content = hit.getHighlightFields().get("content");
                if (content!=null){
                    discussPost.setContent(content.get(0));
                }
            }
        }
        return page;
    }

}
