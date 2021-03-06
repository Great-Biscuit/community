package com.nowcoder.community.util;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
public class SensitiveFilter {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    //替换符
    private static final String REPLACEMENT = "***";

    //根节点
    private TrieNode rootNode = new TrieNode();

    //容器调用构造方法后就会调用它
    @PostConstruct
    public void init() {
        try (
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        ) {
            String keyword;
            while ((keyword = reader.readLine()) != null) {
                //添加到前缀树
                this.addKeyword(keyword);
            }
        } catch (IOException e) {
            logger.error("加载敏感词失败" + e.getMessage());
        }
    }

    //将敏感词添加进前缀树
    private void addKeyword(String keyword) {

        TrieNode tempNode = rootNode;
        int len = keyword.length();
        for (int i = 0; i < len; i++) {
            char c = keyword.charAt(i);
            TrieNode subNode = tempNode.getSubNode(c);

            //当前字没有
            if (subNode == null) {
                //初始化新的子节点
                subNode = new TrieNode();
                tempNode.addSubNode(c, subNode);
            }

            //接着往下
            tempNode = subNode;

            //最后设置结束标志
            if (i == len - 1) {
                tempNode.setKeywordEnd(true);
            }
        }

    }


    /**
     * 过滤敏感词的方法
     *
     * @param text 需要过滤的文本
     * @return 过滤后的文本
     */
    public String filter(String text) {

        if (StringUtils.isBlank(text)) {
            return null;
        }

        // 树指针
        TrieNode tempNode = new TrieNode();
        //双指针
        int begin = 0, position = 0;

        StringBuffer sb = new StringBuffer();
        while (position < text.length()) {
            char c = text.charAt(position);

            //跳过特殊符号  如：✸抢✸劫✸
            if (isSymbol(c)) {
                //若当前树指针为根，则加进sb去
                if (tempNode == rootNode) {
                    sb.append(c);
                    begin++;
                }
                //无论什么情况，它总要走
                position++;
                continue;
            }

            //检查下级结点
            tempNode = tempNode.getSubNode(c);
            //以begin开头的字符串不是敏感词
            if (tempNode == null) {
                sb.append(text.charAt(begin));
                //进入下一位置
                position = ++begin;
                //重新指向根节点
                tempNode = rootNode;
            } else if (tempNode.isKeywordEnd) {
                //发现敏感词
                sb.append(REPLACEMENT);
                //进入下一位置
                begin = ++position;
                //重新指向根节点
                tempNode = rootNode;
            } else {
                //结果还未知
                position++;
            }

        }
        //把最后剩下的补上
        sb.append(text.substring(begin));
        return sb.toString();
    }

    //判断特殊符号  后面那一段是东亚文字范围的意思
    private boolean isSymbol(Character c) {
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }


    /**
     * 前缀树
     */
    private class TrieNode {

        //关键词结束标志
        private boolean isKeywordEnd = false;

        //子节点
        private Map<Character, TrieNode> subNodes = new HashMap<>();

        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }

        public void setKeywordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
        }

        //添加子节点
        public void addSubNode(Character c, TrieNode node) {
            subNodes.put(c, node);
        }

        //获取子节点
        public TrieNode getSubNode(Character c) {
            return subNodes.get(c);
        }
    }

}
