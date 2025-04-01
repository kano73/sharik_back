package com.mary.sharik.config;

import com.mary.sharik.model.enums.KafkaTopicEnum;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;

import java.time.Duration;

@Configuration
public class KafkaConfig {
//product
    @Bean
    public NewTopic productsByFilter() {
        return TopicBuilder.name(KafkaTopicEnum.PRODUCT_BY_FILTER_TOPIC.name())
                .partitions(1)
                .replicas(1)
                .config("retention.ms", "3600000") // 1 час
                .build();
    }

    @Bean
    public NewTopic productById() {
        return TopicBuilder.name(KafkaTopicEnum.PRODUCT_BY_ID_TOPIC.name())
                .partitions(1)
                .replicas(1)
                .config("retention.ms", "3600000") // 1 час
                .build();
    }

    @Bean
    public NewTopic setProductStatus() {
        return TopicBuilder.name(KafkaTopicEnum.PRODUCT_SET_STATUS_TOPIC.name())
                .partitions(1)
                .replicas(2)
                .config("retention.ms", "7200000") // 2 часа
                .build();
    }

    @Bean
    public NewTopic createProduct() {
        return TopicBuilder.name(KafkaTopicEnum.PRODUCT_CREATE_TOPIC.name())
                .partitions(1)
                .replicas(2)
                .config("retention.ms", "7200000") // 2 часа
                .build();
    }

//history + cart

    @Bean
    public NewTopic cartEmptyTopic() {
        return TopicBuilder.name(KafkaTopicEnum.CART_EMPTY_TOPIC.name())
                .partitions(1)
                .replicas(1)
                .config("retention.ms", "3600000") // 1 час
                .build();
    }

    @Bean
    public NewTopic cartAddTopic() {
        return TopicBuilder.name(KafkaTopicEnum.CART_ADD_TOPIC.name())
                .partitions(1)
                .replicas(1)
                .config("retention.ms", "3600000") // 1 час
                .build();
    }

    @Bean
    public NewTopic cartChangeAmountTopic() {
        return TopicBuilder.name(KafkaTopicEnum.CART_CHANGE_AMOUNT_TOPIC.name())
                .partitions(1)
                .replicas(1)
                .config("retention.ms", "3600000") // 1 час
                .build();
    }

    @Bean
    public NewTopic cartOrderTopic() {
        return TopicBuilder.name(KafkaTopicEnum.CART_ORDER_TOPIC.name())
                .partitions(1)
                .replicas(2)
                .config("retention.ms", "7200000") // 2 часа
                .build();
    }

    @Bean
    public NewTopic cartViewTopic() {
        return TopicBuilder.name(KafkaTopicEnum.CART_VIEW_TOPIC.name())
                .partitions(1)
                .replicas(1)
                .config("retention.ms", "3600000")
                .build();
    }

    @Bean
    public NewTopic historyViewTopic() {
        return TopicBuilder.name(KafkaTopicEnum.HISTORY_VIEW_TOPIC.name())
                .partitions(1)
                .replicas(1)
                .config("retention.ms", "3600000")
                .build();
    }

    @Bean
    public NewTopic historyAllTopic() {
        return TopicBuilder.name(KafkaTopicEnum.HISTORY_ALL_TOPIC.name())
                .partitions(1)
                .replicas(1)
                .config("retention.ms", "7200000")
                .build();
    }

    @Bean
    public ReplyingKafkaTemplate<String, String, String>
    replyingKafkaTemplate(
            ProducerFactory<String, String> pf,
            KafkaMessageListenerContainer<String, String> container)
    {
        ReplyingKafkaTemplate<String, String, String> template = new ReplyingKafkaTemplate<>(pf, container);
        template.setDefaultReplyTimeout(Duration.ofSeconds(10));
        return template;
    }

    @Bean
    public KafkaMessageListenerContainer<String, String>
    replyContainer(ConsumerFactory<String, String> cf)
    {
        ContainerProperties containerProperties = new ContainerProperties(KafkaTopicEnum.REPLY_TOPIC.name());

        return new KafkaMessageListenerContainer<>(cf, containerProperties);
    }
}