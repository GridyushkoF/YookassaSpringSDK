package com.yookassa.spring;


import com.yookassa.spring.autoconfigure.YooKassaAutoConfiguration;
import com.yookassa.spring.client.YooKassaClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = YooKassaAutoConfiguration.class)
@TestPropertySource(properties = {
        "yookassa.shop-id=test-shop",
        "yookassa.secret-key=test-secret"
})
class YooKassaAutoConfigurationTest {

    @Autowired
    private ApplicationContext context;

    @Test
    void contextLoads() {
        assertThat(context).isNotNull();
    }

    @Test
    void yooKassaServiceBeanExists() {
        assertThat(context.getBean(YooKassaService.class)).isNotNull();
    }

    @Test
    void yooKassaClientBeanExists() {
        assertThat(context.getBean(YooKassaClient.class)).isNotNull();
    }
}
