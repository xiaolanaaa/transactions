package com.huohu.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TranConfig {
    @Bean
    public Queue tranconfig(){
        return  new Queue("tranlist");
    }
}
