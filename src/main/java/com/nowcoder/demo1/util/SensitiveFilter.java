package com.nowcoder.demo1.util;

import io.micrometer.common.util.StringUtils;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * 处理敏感字词
 * 创建一个前缀树用于存放敏感词（位于resources目录下的sensitive-words.txt文件中），使用前缀树可以提高检索时间
 * 前缀树根节点为空，子节点存放单个字符并往下延伸
 * 获取用户创建文本信息，使用二级指针遍历，若有违规字词则替换为指定内容
 */
@Component
public class SensitiveFilter {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);
//    替换符号
    private static final String REPLACEMENT = "***";
//    根节点
    private TrieNode rootNode = new TrieNode();
//    初始化前缀树，依赖注入后实施
    @PostConstruct
    public void init(){
        try (
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
                BufferedReader reader =  new BufferedReader(new InputStreamReader(is));
        ) {
            String keyword;
            while ((keyword = reader.readLine()) != null){
                //            添加到前缀树
                this.addKeyWord(keyword);
            }
        }catch (IOException e){
            logger.error("获取敏感文件失败：" + e.getMessage());
        }
    }
//    添加敏感词到前缀树中
    private void addKeyWord(String kerWord){
//        交换数据的临时节点
        TrieNode tempNode = rootNode;
        for (int i = 0; i < kerWord.length(); i++) {
            char c = kerWord.charAt(i);
            TrieNode subNode = tempNode.getSubNode(c);

            if (subNode == null){
//                初始化子节点
                subNode = new TrieNode();
                tempNode.addSubNode(c,subNode);
            }

//            指向子节点，进入下一轮循环
            tempNode = subNode;
//            设置结束标识
            if (i == kerWord.length() - 1){
                tempNode.setKeyWordEnd(true);
            }
        }
    }

    /**
     * 获取用户输入文本，敏感词将被替换为***，使用二维指针遍历
     * @param text
     * @return
     */
    public String filter(String text){
//        isBlank五种情况都会返回TRUE
//    是否为null、“”、“ ”、制表符换页符和回车以及空白
//    isEmpty则只判断是否为null或者数组长度是否为0
        if (StringUtils.isBlank(text)){
            return null;
        }
//        指针1
        TrieNode tempNode = rootNode;
//        指针2
        int begin = 0;
//        指针3
        int position = 0;
//        经过处理的文本
        StringBuilder sb = new StringBuilder();
        while (position < text.length()){
            char c = text.charAt(position);
//            忽略符号
            if (isSymbol(c)){
//                若指针1处于根节点则将符号计入结果让指针2向下走
                if (tempNode == rootNode){
                    sb.append(c);
                    begin++;
                }
//                符号无论在开头还是中间指针3都继续向下走
                position++;
                continue;
            }
//            检查下级节点
            tempNode = tempNode.getSubNode(c);
            if (tempNode == null){
//                以begin开头的字符串不是敏感词
                sb.append(text.charAt(begin));
//                进入下一个位置
                position = ++begin;
//                重新指向根节点
                tempNode = rootNode;
            }else if(tempNode.isKeyWordEnd()){
//                发现违禁词，进行替换
                sb.append(REPLACEMENT);
//                进入下一位置
                begin = ++position;
//                重新指向根节点
                tempNode = rootNode;
            }else {
//                检查下一字符
                position++;
            }
        }
//        将最后的字符计入结果
    sb.append(text.substring(begin));
    return sb.toString();
}
//判断是否为符号
    private boolean isSymbol(Character c){
//        0x2E80 ~ 0x9FFF为东亚文字范围
        return !Character.isAlphabetic(c) && (c < 0x2E80 || c > 0x9FFF);
    }

//    创建前缀树
    private class TrieNode{

    //        关键词标识，判断是否为违禁词
    private boolean isKeyWordEnd = false;
//    子节点（key为下级字符，value是下级节点）
    private Map<Character,TrieNode> subNodes = new HashMap<>();

    public boolean isKeyWordEnd() {
        return isKeyWordEnd;
    }

    public void setKeyWordEnd(boolean keyWordEnd) {
        isKeyWordEnd = keyWordEnd;
    }
//    添加子节点
    public void addSubNode(Character c, TrieNode node){
        subNodes.put(c,node);
    }
//    获取子节点
    public TrieNode getSubNode(Character c){
        return subNodes.get(c);
    }
}

}
