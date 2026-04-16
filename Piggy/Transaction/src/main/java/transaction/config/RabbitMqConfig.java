package transaction.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
/**
 * RabbitMQ 配置
 * 消费者
 */
@Configuration
public class RabbitMqConfig {

    public static final String EXCHANGE = "ai.classify.exchange";
    public static final String QUEUE = "ai.classify.queue";
    public static final String ROUTING_KEY = "ai.classify";

    @Bean
    public DirectExchange aiClassifyExchange() {
        return new DirectExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue aiClassifyQueue() {
        return new Queue(QUEUE, true);
    }

    @Bean
    public Binding aiClassifyBinding() {
        return BindingBuilder.bind(aiClassifyQueue())
                .to(aiClassifyExchange())
                .with(ROUTING_KEY);
    }
}
