package org.dimamir999.config;

import org.dimamir999.network.SocketServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("org.dimamir999")
public class Config {
    @Bean(name = "socketServer")
    public SocketServer socketServer() {
        return new SocketServer();
    }
}