package transaction.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import common.dto.AiClassifyMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import transaction.client.AiFeignClient;
import transaction.entity.Transaction;
import transaction.mapper.TransactionMapper;

import java.io.IOException;
import java.time.LocalDateTime;
/**
 * 交易消费者
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionConsumer {

    private final TransactionMapper transactionMapper;
    private final AiFeignClient aiFeignClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 1. 监听ai.classify.queue队列
    @RabbitListener(queues = "ai.classify.queue")
    public void handleMessage(Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();

        try {
            String body = new String(message.getBody());
            AiClassifyMessage classifyMessage = objectMapper.readValue(body, AiClassifyMessage.class);

            log.info("开始处理AI分类任务, taskId: {}, count: {}",
                    classifyMessage.getTaskId(), classifyMessage.getInputs().size());

            for (String text : classifyMessage.getInputs()) {
                try {
                    Transaction record = new Transaction();
                    record.setUserId(classifyMessage.getUserId());
                    record.setRemark(text);
                    record.setTransactionTime(LocalDateTime.now());
                    record.setCreateTime(LocalDateTime.now());
                    record.setUpdateTime(LocalDateTime.now());

                    transactionMapper.insert(record);

                    aiFeignClient.updateProgress(classifyMessage.getTaskId());

                } catch (Exception e) {
                    log.error("保存交易记录失败, text: {}", text, e);
                }
            }

            log.info("AI分类任务完成, taskId: {}", classifyMessage.getTaskId());
            channel.basicAck(deliveryTag, false);

        } catch (Exception e) {
            log.error("处理AI分类任务失败", e);
            channel.basicNack(deliveryTag, false, false);
        }
    }
}
