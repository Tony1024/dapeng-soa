package com.github.dapeng.message.event.task;

import com.github.dapeng.message.event.SoaKafkaProducer;
import com.github.dapeng.message.event.dao.IMessageDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 描述:
 *
 * @author maple.lei
 * @date 2018年02月23日 下午9:21
 */
@Transactional(rollbackFor = Exception.class)
public class MessageScheduled {
    private Logger logger = LoggerFactory.getLogger(MessageScheduled.class);

    @Autowired
    private IMessageDao messageDao;

    @Autowired
    private SoaKafkaProducer producer;


    public void fetchMessage() {
        List<EventInfo> eventInfos = messageDao.listMessages();
        if (!eventInfos.isEmpty()) {
            eventInfos.forEach(eventInfo -> {
                producer.send(eventInfo.getId(), eventInfo.getEventBinary());
                logger.info("send message to kafka success eventInfo:  {}", eventInfo.getEventType());
                doDeleteMessage(eventInfo);
            });
        } else {
            logger.info("no event to send");
        }

    }

    //todo 事务会失效？
    private void doDeleteMessage(EventInfo eventInfo) {
        //fixme 便于测试。。。
//        messageDao.deleteMessage(eventInfo.getId());
        logger.info("消息发送kafka broker 成功，删除message，id: {}", eventInfo.getId());
    }

}
