package org.dimamir999.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dimamir999.dao.FileDao;
import org.dimamir999.model.KeyValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.management.RuntimeErrorException;
import java.io.File;
import java.io.IOException;

@Service(value = "operationService")
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

    boolean filesPrepared = false;

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

        filesPrepared = true;

        Thread fileMergerThread = new Thread(fileMerger);
        fileMergerThread.start();
    }

    private boolean cantCreateInFile(String fileName, KeyValue<String, String> keyValue) throws IOException {
        String allData = fileDao.read(fileName);

        if (fileIsNotEmpty(allData)) {
            for (String line : allData.split("\n")) {
                KeyValue<String, String> lineKeyValue = stringKeyValueConverter.decode(line);
                if ((lineKeyValue.getKey()).equals(keyValue.getKey())) {
                    LOG.info("Object not added - key: '" + keyValue.getKey() + "' is already occupied, value: '" + lineKeyValue.getValue() + "'");
                    return true;
                }
            }
        }

        return false;
    }

    public KeyValue<String, String> create(KeyValue<String, String> keyValue) throws IOException {
        if (!filesPrepared) {
            prepareFiles();
        }

        if (cantCreateInFile(dataFile, keyValue) || cantCreateInFile(tempFile, keyValue)) {
            return null;
        }

        fileDao.append(stringKeyValueConverter.encode(keyValue), tempFile);
        LOG.info("Object added - key: '" + keyValue.getKey() + "', value:'" + keyValue.getValue() + "'");
        return keyValue;
    }

    private KeyValue<String, String> getFromFile(String fileName, String key) throws IOException {
        String allData = fileDao.read(fileName);

        if (fileIsNotEmpty(allData)) {
            for (String line : allData.split("\n")) {
                KeyValue<String, String> keyValue = stringKeyValueConverter.decode(line);
                if ((keyValue.getKey()).equals(key)) {
                    LOG.info("Object returned - key: '" + key + "', value:'" + keyValue.getValue() + "'");
                    return keyValue;
                }
            }
        }

        return null;
    }

    public KeyValue<String, String> get(String key) throws IOException {
        if (!filesPrepared) {
            prepareFiles();
        }

        KeyValue<String, String> keyValue = getFromFile(tempFile, key);
        if (keyValue != null) {
            return keyValue;
        }

        keyValue = getFromFile(dataFile, key);
        if (keyValue != null) {
            return keyValue;
        }

        return null;
    }

    private void changeValue(KeyValue<String, String> oldKeyValue, KeyValue<String, String> keyValue, String data, String file) throws IOException {
        String oldLine = stringKeyValueConverter.encode(oldKeyValue);
        String newLine = stringKeyValueConverter.encode(keyValue);
        String newData = data.replace(oldLine, newLine);
        fileDao.write(newData, file);
    }

    private KeyValue<String, String> updateInFile(String fileName, KeyValue<String, String> keyValue) throws IOException {
        String key = keyValue.getKey();
        String allData = fileDao.read(fileName);

        if (fileIsNotEmpty(allData)) {
            for (String line : allData.split("\n")) {
                KeyValue<String, String> oldKeyValue = stringKeyValueConverter.decode(line);
                if ((oldKeyValue.getKey()).equals(key)) {
                    changeValue(oldKeyValue, keyValue, allData, fileName);
                    LOG.info("Object updated - key: '" + key + "', old value:'" + oldKeyValue.getValue() + "', new value:'" + keyValue.getValue() + "'");
                    return oldKeyValue;
                }
            }
        }

        return null;
    }

    public KeyValue<String, String> update(KeyValue<String, String> keyValue) throws IOException {
        if (!filesPrepared) {
            prepareFiles();
        }

        KeyValue<String, String> oldKeyValue = updateInFile(tempFile, keyValue);
        if (oldKeyValue != null) {
            return oldKeyValue;
        }

        oldKeyValue = updateInFile(dataFile, keyValue);
        if (oldKeyValue != null) {
            return oldKeyValue;
        }

        return null;
    }

    private void deleteValue(KeyValue<String, String> oldKeyValue, String data, String file) throws IOException {
        String oldLine = stringKeyValueConverter.encode(oldKeyValue) + "\n";
        String newData = data.replace(oldLine, "");
        fileDao.write(newData, file);
    }

    private KeyValue<String, String> deleteFromFile(String fileName, String key) throws IOException {
        String allData = fileDao.read(fileName);

        if (fileIsNotEmpty(allData)) {
            for (String line : allData.split("\n")) {
                KeyValue<String, String> oldKeyValue = stringKeyValueConverter.decode(line);
                if ((oldKeyValue.getKey()).equals(key)) {
                    deleteValue(oldKeyValue, allData, fileName);
                    LOG.info("Object deleted - key: '" + key + "', value:'" + oldKeyValue.getValue() + "'");
                    return oldKeyValue;
                }
            }
        }

        return null;
    }

    public KeyValue<String, String> delete(String key) throws IOException {
        if (!filesPrepared) {
            prepareFiles();
        }

        KeyValue<String, String> oldKeyValue = deleteFromFile(tempFile, key);
        if (oldKeyValue != null) {
            return oldKeyValue;
        }

        oldKeyValue = deleteFromFile(dataFile, key);
        if (oldKeyValue != null) {
            return oldKeyValue;
        }

        return null;
    }
}
