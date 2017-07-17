package org.dimamir999.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dimamir999.model.Command;
import org.dimamir999.model.CommandType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component(value = "commandParser")
public class CommandParser {
    private static final Logger LOG = LogManager.getLogger(CommandParser.class);

    public Command parseCommand(String input) {
        CommandType commandType = null;
        String[] tokens = input.split(" ");
        for(CommandType possibleType : CommandType.values()){
            if(possibleType.getKeyWord().equals(tokens[0])){
                commandType = possibleType;
                break;
            }
        }

        if(commandType == null) {
            LOG.error("No such command");
            throw new IllegalArgumentException();
        }

        if(tokens.length == 1) {
            LOG.error("No key specified");
            throw new IllegalArgumentException();
        }

        ArrayList params = new ArrayList();
        for(int i = 1; i < tokens.length; ++i) {
            params.add(tokens[i]);
        }

        return new Command(commandType, params);
    }
}
