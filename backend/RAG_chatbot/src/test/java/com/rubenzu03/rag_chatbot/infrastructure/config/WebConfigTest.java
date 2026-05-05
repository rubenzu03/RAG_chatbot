package com.rubenzu03.rag_chatbot.infrastructure.config;

import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class WebConfigTest {

    private WebConfig webConfig = new WebConfig();

    @Test
    void testCorsConfigurerBeanCreation() {
        WebMvcConfigurer configurer = webConfig.corsConfigurer();
        assertThat(configurer).isNotNull();
    }

    @Test
    void testCorsConfigurerNotNull() {
        WebMvcConfigurer configurer = webConfig.corsConfigurer();
        assertThat(configurer).isNotNull();
    }

    @Test
    void testCorsConfigurerIsWebMvcConfigurer() {
        WebMvcConfigurer configurer = webConfig.corsConfigurer();
        assertThat(configurer).isInstanceOf(WebMvcConfigurer.class);
    }
}
