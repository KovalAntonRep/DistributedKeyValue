package org.dimamir999.network;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

@Repository(value = "socketServer")
@PropertySource("distributed-key-value.properties")
@ComponentScan
public class SocketServer {
    private final Logger LOG = LogManager.getLogger(SocketServer.class);
    @Value("${client.port}") private int port;
    @Autowired
    private InboundMessageHandler handler;

    public void start() throws IOException{
        ServerSocket serverSocket = new ServerSocket(port);
        LOG.info("Server started. Port: " + port);
        while (true) {
            Socket socket = serverSocket.accept();
            handler.addNewConnection(new Connection(socket));
            LOG.info("New connection was created successfully");
        }
    }
}
