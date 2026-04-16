package ai.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
/**
 * RabbitMQ配置类
 * 生产者端
 */
@Configuration
public class RabbitMqConfig {
    // 交换机名称
    public static final String EXCHANGE = "ai.classify.exchange";
    // 队列名称
    public static final String QUEUE = "ai.classify.queue";
    // 路由键
    public static final String ROUTING_KEY = "ai.classify";

    @Bean
    public DirectExchange aiClassifyExchange() {
        // 创建交换机 durable = true 持久化 a
        return new DirectExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue aiClassifyQueue() {
        // 创建队列 durable = true 持久化
        return new Queue(QUEUE, true);
    }

    @Bean
    public Binding aiClassifyBinding() {
        // 绑定队列到交换机
        return BindingBuilder.bind(aiClassifyQueue())
                .to(aiClassifyExchange())
                .with(ROUTING_KEY);
    }
}