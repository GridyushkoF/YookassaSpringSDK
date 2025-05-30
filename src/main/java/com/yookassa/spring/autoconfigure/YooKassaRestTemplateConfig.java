package com.yookassa.spring.autoconfigure;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.yookassa.spring.client.LoggingInterceptor;
import com.yookassa.spring.exception.YooKassaErrorHandler;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

@Configuration
public class YooKassaRestTemplateConfig {

    @Bean
    @ConditionalOnMissingBean(name = "yooKassaObjectMapper")
    public ObjectMapper yooKassaObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // Настройки специально для YooKassa API
        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.registerModule(new JavaTimeModule());

        return mapper;
    }

    @Bean
    @ConditionalOnMissingBean(name = "yooKassaRestTemplate")
    public RestTemplate yooKassaRestTemplate(@Qualifier("yooKassaObjectMapper") ObjectMapper objectMapper) {
        // Создаем кастомный конвертер с нашим ObjectMapper
        MappingJackson2HttpMessageConverter converter =
                new MappingJackson2HttpMessageConverter(objectMapper);

        RestTemplate restTemplate = new RestTemplate(
                new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory())
        );

        // Заменяем стандартный Jackson конвертер на наш
        restTemplate.getMessageConverters().removeIf(
                c -> c instanceof MappingJackson2HttpMessageConverter
        );
        restTemplate.getMessageConverters().add(converter);

        // Добавляем интерцепторы
        restTemplate.setErrorHandler(new YooKassaErrorHandler());
        restTemplate.getInterceptors().add(new LoggingInterceptor());

        return restTemplate;
    }
}
