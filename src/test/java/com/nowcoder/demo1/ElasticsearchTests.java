package com.nowcoder.demo1;

import co.elastic.clients.elasticsearch._types.query_dsl.*;
import com.nowcoder.demo1.dao.DiscussPostMapper;
import com.nowcoder.demo1.dao.elasticsearch.DiscussPostRepository;
import com.nowcoder.demo1.entity.DiscussPost;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
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

import java.util.List;


@SpringBootTest
public class ElasticsearchTests {

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private DiscussPostRepository discussPostRepository;

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;
//每次插入单条数据
    @Test
    public void testInsert(){
        discussPostRepository.save(discussPostMapper.selectDiscussPost(277));
        discussPostRepository.save(discussPostMapper.selectDiscussPost(280));
        discussPostRepository.save(discussPostMapper.selectDiscussPost(282));
    }
//   一次插入多条数据
    @Test
    public void testInsertList(){
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(11,0,100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(101,0,100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(102,0,100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(103,0,100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(111,0,100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(131,0,100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(132,0,100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(133,0,100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(134,0,100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(138,0,100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(145,0,100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(146,0,100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(149,0,100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(159,0,100));
    }

//    修改elasticsearch中的数据
    @Test
    public void testUpdate(){
        DiscussPost discussPost = discussPostMapper.selectDiscussPost(217);
        discussPost.setContent("网上都是在制造焦虑，实际上根本没那么严重，我普通二本，一年经验，北京面了3家，拿了4个offer，最高32k，最低27k  32k是外包，还有个30k的自研可以考虑一下，不说了，明天还要两个面试，  寒冬年年有，年年都是计算机，结果计算机工资不是还是轻轻松松月入过2w， xdm，有一说一，现在不学JAVA，以后就没机会了，趁着现在的红利，狠狠的赚一笔");
        discussPostRepository.save(discussPost);
    }

//    删除elasticsearch中的数据
    @Test
    public void testDelete(){
//        discussPostRepository.deleteById(217);
        discussPostRepository.deleteAll();
    }

//    通过继承ElasticsearchRepository接口，搜索elasticsearch中的内容
    @Test
    public void testSearchByRepository(){

        Page<DiscussPost> page = (Page<DiscussPost>) discussPostRepository.findAll();

        System.out.println("总共有："+page.getTotalElements()+"条数据");
        System.out.println("总共有："+page.getTotalPages()+"页");
        System.out.println("当前是第："+page.getNumber()+"页");
        System.out.println("当前页有：" +page.getSize()+"条数据");

        for (DiscussPost post : page) {
            System.out.println(post.toString());
        }
    }

//  使用elasticsearchTemplate搜索elasticsearch中的内容
    @Test
    public void testSearchByTemplate(){
//        高亮实现仿的这 https://www.cnblogs.com/gdwkong/p/17331639.html
        HighlightField titleHighlightField = new HighlightField("title");
        HighlightField contentHighlightField = new HighlightField("content");

        Highlight titleHighlight = new Highlight(List.of(titleHighlightField,contentHighlightField));

        NativeQuery searchQuery =new  NativeQueryBuilder()
//                查询这里直接使用了lambda表达式，不用写实现类
                .withQuery(Query.of(q -> q.multiMatch(mq -> mq.query("互联网寒冬").fields("title","content"))))
//                倒序直接用Sort.by后面加descending()就行
                .withSort(Sort.by("type","score","createTime").descending())
                .withPageable(PageRequest.of(0, 10))
                .withHighlightQuery(
                        new HighlightQuery(titleHighlight,DiscussPost.class)
                )
                .build();

        SearchHits<DiscussPost> search = elasticsearchTemplate.search(searchQuery, DiscussPost.class);
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
                System.out.println(hit.getContent());
            }
        }

/*        SearchPage<DiscussPost> searchPage = page;
        List<Map<String, Object>> discussPosts = new ArrayList<Map<String, Object>>();
        if (searchPage != null) {
            for (SearchHit<DiscussPost> discussPostSearchHit : searchPage) {
                Map<String, Object> map = new HashMap<>();
                //帖子
                DiscussPost post = discussPostSearchHit.getContent();
                System.out.println(post);
            }
        }*/

    }

}
