package org.dimamir999;

import org.dimamir999.config.Config;
import org.dimamir999.network.SocketServer;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.IOException;

public class Starter {
    public static void main(String[] args) throws IOException {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(Config.class);

        SocketServer socketServer = context.getBean("socketServer", SocketServer.class);
        socketServer.start();
    }
}