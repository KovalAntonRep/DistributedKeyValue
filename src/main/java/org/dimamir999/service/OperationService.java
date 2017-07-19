package org.dimamir999.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dimamir999.dao.FileDao;
import org.dimamir999.model.KeyValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import javax.management.RuntimeErrorException;
import java.io.File;
import java.io.IOException;

@Service(value = "operationService")
@PropertySource("distributed-key-value.properties")
@ComponentScan
public class OperationService {
    private static final Logger LOG = LogManager.getLogger(OperationService.class);

    @Value("${name.tempfile}")
    private String tempFile;

    @Value("${name.datafile}")
    private String dataFile;

    @Autowired
    private FileDao fileDao;

    @Autowired
    private StringKeyValueConverter stringKeyValueConverter;

    @Autowired
    private FileMerger fileMerger;

    private boolean fileIsNotEmpty(String fileString) throws IOException {
        return !fileString.equals("");
    }

    public OperationService() {

    }

    public void setFileDao(FileDao fileDao) {
        this.fileDao = fileDao;
    }

    public void setStringKeyValueConverter(StringKeyValueConverter stringKeyValueConverter) {
        this.stringKeyValueConverter = stringKeyValueConverter;
    }

    private void prepareFiles() {
        File file = new File(System.getProperty("user.dir") + "/" + tempFile);

        if(!file.exists()) {
            file.getParentFile().mkdirs();

            try {
                file.createNewFile();
            } catch (IOException e) {
                LOG.error("New file creating error", e);
                throw new RuntimeErrorException(null);
            }
        }

        Thread fileMergerThread = new Thread(fileMerger);
        fileMergerThread.start();
    }

    public KeyValue<String, String> create(KeyValue<String, String> keyValue) throws IOException {
        prepareFiles();

        String allData = fileDao.read(dataFile);

        if (fileIsNotEmpty(allData)) {
            for (String line : allData.split("\n")) {
                KeyValue<String, String> lineKeyValue = stringKeyValueConverter.decode(line);
                if ((lineKeyValue.getKey()).equals(keyValue.getKey())) {
                    LOG.info("Object not added - key: '" + keyValue.getKey() + "' is already occupied, value: '" + lineKeyValue.getValue() + "'");
                    return null;
                }
            }
        }

        String tempData = fileDao.read(tempFile);

        if (fileIsNotEmpty(tempData)) {
            for (String line : tempData.split("\n")) {
                KeyValue<String, String> lineKeyValue = stringKeyValueConverter.decode(line);
                if ((lineKeyValue.getKey()).equals(keyValue.getKey())) {
                    LOG.info("Object not added - key: '" + keyValue.getKey() + "' is already occupied, value: '" + lineKeyValue.getValue() + "'");
                    return null;
                }
            }
        }

        fileDao.append(stringKeyValueConverter.encode(keyValue), tempFile);
        LOG.info("Object added - key: '" + keyValue.getKey() + "', value:'" + keyValue.getValue() + "'");
        return keyValue;
    }

    public KeyValue<String, String> get(String key) throws IOException {
        prepareFiles();
        String allData = fileDao.read(dataFile);

        if (fileIsNotEmpty(allData)) {
            for (String line : allData.split("\n")) {
                KeyValue<String, String> keyValue = stringKeyValueConverter.decode(line);
                if ((keyValue.getKey()).equals(key)) {
                    LOG.info("Object returned - key: '" + key + "', value:'" + keyValue.getValue() + "'");
                    return keyValue;
                }
            }
        }

        String tempData = fileDao.read(tempFile);

        if (fileIsNotEmpty(tempData)) {
            for (String line : tempData.split("\n")) {
                KeyValue<String, String> keyValue = stringKeyValueConverter.decode(line);
                if ((keyValue.getKey()).equals(key)) {
                    LOG.info("Object returned - key: '" + key + "', value:'" + keyValue.getValue() + "'");
                    return keyValue;
                }
            }
        }

        return null;
    }

    private void changeValue(KeyValue<String, String> oldKeyValue, KeyValue<String, String> keyValue, String data, String file) throws IOException {
        String oldLine = stringKeyValueConverter.encode(oldKeyValue);
        String newLine = stringKeyValueConverter.encode(keyValue);
        String newData = data.replace(oldLine, newLine);
        fileDao.write(newData, file);
    }

    public KeyValue<String, String> update(KeyValue<String, String> keyValue) throws IOException {
        prepareFiles();

        String key = keyValue.getKey();

        String allData = fileDao.read(dataFile);

        if (fileIsNotEmpty(allData)) {
            for (String line : allData.split("\n")) {
                KeyValue<String, String> oldKeyValue = stringKeyValueConverter.decode(line);
                if ((oldKeyValue.getKey()).equals(key)) {
                    changeValue(oldKeyValue, keyValue, allData, dataFile);
                    LOG.info("Object updated - key: '" + key + "', old value:'" + oldKeyValue.getValue() + "', new value:'" + keyValue.getValue() + "'");
                    return oldKeyValue;
                }
            }
        }

        String tempData = fileDao.read(tempFile);

        if (fileIsNotEmpty(tempData)) {
            for (String line : tempData.split("\n")) {
                KeyValue<String, String> oldKeyValue = stringKeyValueConverter.decode(line);
                if ((oldKeyValue.getKey()).equals(key)) {
                    changeValue(oldKeyValue, keyValue, tempData, tempFile);
                    LOG.info("Object updated - key: '" + key + "', old value:'" + oldKeyValue.getValue() + "', new value:'" + keyValue.getValue() + "'");
                    return oldKeyValue;
                }
            }
        }

        return null;
    }

    private void deleteValue(KeyValue<String, String> oldKeyValue, String data, String file) throws IOException {
        String oldLine = stringKeyValueConverter.encode(oldKeyValue) + "\n";
        String newData = data.replace(oldLine, "");
        fileDao.write(newData, file);
    }

    public KeyValue<String, String> delete(String key) throws IOException {
        prepareFiles();

        String allData = fileDao.read(dataFile);

        if (fileIsNotEmpty(allData)) {
            for (String line : allData.split("\n")) {
                KeyValue<String, String> oldKeyValue = stringKeyValueConverter.decode(line);
                if ((oldKeyValue.getKey()).equals(key)) {
                    deleteValue(oldKeyValue, allData, dataFile);
                    LOG.info("Object deleted - key: '" + key + "', value:'" + oldKeyValue.getValue() + "'");
                    return oldKeyValue;
                }
            }
        }

        String tempData = fileDao.read(tempFile);

        if (fileIsNotEmpty(tempData)) {
            for (String line : tempData.split("\n")) {
                KeyValue<String, String> oldKeyValue = stringKeyValueConverter.decode(line);
                if ((oldKeyValue.getKey()).equals(key)) {
                    deleteValue(oldKeyValue, tempData, tempFile);
                    LOG.info("Object deleted - key: '" + key + "', value:'" + oldKeyValue.getValue() + "'");
                    return oldKeyValue;
                }
            }
        }

        return null;
    }
}
