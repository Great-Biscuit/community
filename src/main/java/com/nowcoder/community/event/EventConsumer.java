package com.nowcoder.community.event;

import com.alibaba.fastjson.JSONObject;
import com.nowcoder.community.entity.Event;
import com.nowcoder.community.entity.Message;
import com.nowcoder.community.service.MessageService;
import com.nowcoder.community.util.CommunityConstant;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 消费者
 */
@Component
public class EventConsumer implements CommunityConstant {
    //记录日志
    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);
    //消息
    @Autowired
    private MessageService messageService;

    //监听(公共) 被动触发
    @KafkaListener(topics = {TOPIC_COMMENT, TOPIC_FOLLOW, TOPIC_LIKE})
    private void handleCommentMessage(ConsumerRecord record) {
        if (record == null || record.value() == null) {
            logger.error("消息内容为空");
            return;
        }

        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            logger.error("消息格式错误");
            return;
        }

        //发送通知(往message表里插入内容)
        Message message = new Message();
        //消息发送者
        message.setFromId(SYSTEM_USER_ID);
        //消息接收者
        message.setToId(event.getEntityUserId());
        //处理标题(此处存主题)
        message.setConversationId(event.getTopic());
        //时间
        message.setCreateTime(new Date());
        //设置内容
        Map<String, Object> content = new HashMap<>();
        content.put("userId", event.getUserId());//事件触发者
        content.put("entityType", event.getEntityTpye());//操作类型
        content.put("entityId", event.getEntityId());//实体类型

        if (!event.getData().isEmpty()) {
            for (Map.Entry<String, Object> entry : event.getData().entrySet()) {
                content.put(entry.getKey(), entry.getValue());
            }
        }
        //内容
        message.setContent(JSONObject.toJSONString(content));

        //存入
        messageService.addMessage(message);
    }

}
