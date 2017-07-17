package org.dimamir999.controller;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dimamir999.model.KeyValue;
import org.dimamir999.service.OperationService;
import org.dimamir999.service.OperationServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component(value = "commandController")
@ComponentScan
public class CommandController {
    private static final Logger LOG = LogManager.getLogger(CommandController.class);
    @Autowired
    private OperationService operationService;

    public String read(String key) throws OperationServiceException {
        try {
            return operationService.get(key).getValue();
        } catch (IOException e) {
            LOG.error("Can't perform read operation", e);
            throw new OperationServiceException("Can't perform read operation", e);
        }
    }

    public void create(String key, String value) throws OperationServiceException {
        try {
            operationService.create(new KeyValue<>(key, value));
        } catch (IOException e) {
            LOG.error("Can't perform create operation", e);
            throw new OperationServiceException("Can't perform create operation", e);
        }
    }

    public void update(String key, String value) throws OperationServiceException {
        try {
            operationService.update(new KeyValue<>(key, value));
        } catch (IOException e) {
            LOG.error("Can't perform update operation", e);
            throw new OperationServiceException("Can't perform update operations", e);
        }
    }

    public void delete(String key) throws OperationServiceException {
        try {
            operationService.delete(key);
        } catch (IOException e) {
            LOG.error("Can't perform delete operation", e);
            throw new OperationServiceException("Can't perform delete operations", e);
        }
    }
}
