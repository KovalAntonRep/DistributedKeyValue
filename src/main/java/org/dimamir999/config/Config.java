package org.dimamir999.config;

import org.dimamir999.controller.CommandController;
import org.dimamir999.dao.FileDao;
import org.dimamir999.network.InboundMessageHandler;
import org.dimamir999.network.SocketServer;
import org.dimamir999.service.FileMerger;
import org.dimamir999.service.OperationService;
import org.dimamir999.service.StringKeyValueConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("distributed-key-value.properties")
public class Config {
    @Value("${name.datafile}") private String dataFile;
    @Value("${name.tempfile}") private String tempFile;
    @Value("${merging.timeout}") private int timeout;

    @Bean(name = "fileMerger")
    public FileMerger fileMerger() {
        return new FileMerger(dataFile, tempFile, timeout);
    }

    @Bean(name = "fileDao")
    public FileDao fileDao() {
        return new FileDao();
    }

    @Bean(name = "stringKeyValueConverter")
    public StringKeyValueConverter stringKeyValueConverter() {
        return new StringKeyValueConverter();
    }

    @Bean(name = "operationService")
    public OperationService operationService() {
        return new OperationService();
    }

    @Bean(name = "commandController")
    public CommandController commandController() {
        return new CommandController();
    }

    @Bean(name = "inboundMessageHandler")
    public InboundMessageHandler inboundMessageHandler() {
        return new InboundMessageHandler();
    }

    @Bean(name = "socketServer")
    public SocketServer socketServer() {
        return new SocketServer();
    }
}