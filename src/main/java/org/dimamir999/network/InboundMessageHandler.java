package org.dimamir999.network;

import org.dimamir999.controller.CommandController;
import org.dimamir999.model.Command;
import org.dimamir999.service.CommandParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component(value = "inboundMessageHandler")
@ComponentScan
public class InboundMessageHandler {

    private List<Connection> connections = new ArrayList<>();
    @Autowired
    private CommandController commandController;
    private CommandParser parser = new CommandParser();

    public void start(){}

    public void addNewConnection(Connection connection){
        connection.startHandleInputMessages(commandController, parser);
        connections.add(connection);
    }
}
