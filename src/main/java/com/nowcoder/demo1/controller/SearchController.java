package com.nowcoder.demo1.controller;

import com.nowcoder.demo1.entity.DiscussPost;
import com.nowcoder.demo1.entity.Page;
import com.nowcoder.demo1.service.ElasticsearchService;
import com.nowcoder.demo1.service.LikeService;
import com.nowcoder.demo1.service.UserService;
import com.nowcoder.demo1.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchPage;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class SearchController implements CommunityConstant {
    @Autowired
    private ElasticsearchService elasticsearchService;
    @Autowired
    private LikeService likeService;
    @Autowired
    private UserService userService;
//    search?keyword=xxx
    @RequestMapping(path = "/search", method = RequestMethod.GET)
    public String search(String keyword, Page page, Model model){
        SearchPage<DiscussPost> searchResult = elasticsearchService.searchDiscussPost(keyword, page.getCurrent() - 1, page.getLimit());
//        聚合数据
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        if (searchResult != null){
            for (SearchHit<DiscussPost> hit : searchResult) {
                Map<String, Object> map = new HashMap<>();
                DiscussPost post = hit.getContent();
//                帖子
                map.put("post",post);
//                作者
                map.put("user", userService.findUserById(post.getUserId()));
//                点赞数量
                map.put("likeCount", likeService.findEntityLikeCount(ENTITY_TYPE_POST,post.getId()));
                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts",discussPosts);
        model.addAttribute("keyword",keyword);
//        分页信息
        page.setPath("/search?keyword=" + keyword);
        page.setRows(searchResult == null ? 0 : (int) searchResult.getTotalElements());

        return "/site/search";
    }
}
