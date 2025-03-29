package com.mary.sharik.config;

import com.mary.sharik.model.enums.KafkaTopicEnum;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic productsByFilter() {
        return new NewTopic(KafkaTopicEnum.PRODUCT_BY_FILTER_TOPIC.name(), 1, (short) 1);
    }

    @Bean
    public NewTopic productById() {
        return new NewTopic(KafkaTopicEnum.PRODUCT_BY_ID_TOPIC.name(), 1, (short) 1);
    }

    @Bean
    public NewTopic setProductStatus() {
        return new NewTopic(KafkaTopicEnum.PRODUCT_SET_STATUS_TOPIC.name(), 1, (short) 2);
    }

    @Bean
    public NewTopic createProduct() {
        return new NewTopic(KafkaTopicEnum.PRODUCT_CREATE_TOPIC.name(), 1, (short) 2);
    }

    @Bean
    public ReplyingKafkaTemplate<String, String, String> replyingKafkaTemplate(
            ProducerFactory<String, String> pf,
            KafkaMessageListenerContainer<String, String> container) {

        return new ReplyingKafkaTemplate<>(pf, container);
    }

    @Bean
    public KafkaMessageListenerContainer<String, String>
    replyContainer(ConsumerFactory<String, String> cf) {

        ContainerProperties containerProperties = new ContainerProperties("product-replies");
        return new KafkaMessageListenerContainer<>(cf, containerProperties);
    }
}