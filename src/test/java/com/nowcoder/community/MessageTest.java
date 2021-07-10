package com.nowcoder.community;

import com.nowcoder.community.entity.Message;
import com.nowcoder.community.service.MessageService;
import com.nowcoder.community.util.CommunityConstant;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MessageTest implements CommunityConstant {

    @Autowired
    MessageService messageService;

    @Test
    public void systemMessage() {
        Message message = messageService.findLatestNotice(111, TOPIC_COMMENT);
        System.out.println(message);
    }

}
