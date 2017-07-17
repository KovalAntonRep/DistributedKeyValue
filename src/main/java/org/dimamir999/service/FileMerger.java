package org.dimamir999.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dimamir999.dao.FileDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import javax.management.RuntimeErrorException;
import java.io.File;
import java.io.IOException;

import static java.lang.Thread.sleep;

@Component(value = "fileMerger")
@ComponentScan
public class FileMerger implements Runnable {
    private static final Logger LOG = LogManager.getLogger(FileMerger.class);
    private String dataFile;
    private String mergedFile;
    private int timeout;
    @Autowired
    private FileDao fileDao;

    public FileMerger(String dataFile, String mergedFile, int timeout) {
        this.timeout = timeout;
        this.dataFile = dataFile;
        this.mergedFile = mergedFile;

        File file = new File(System.getProperty("user.dir") + "/" + dataFile);
        if(!file.exists()) {
            file.getParentFile().mkdirs();
            try {
                file.createNewFile();
            } catch (IOException e) {
                LOG.error("New file creating error", e);
                throw new RuntimeErrorException(null);
            }
        }
    }

    private void fileMerge() throws IOException {
        String mergeData = fileDao.read(mergedFile);

        for (String line: mergeData.split("/n")) {
            fileDao.append(line, dataFile);
        }

        LOG.info("Files are successfully merged");
    }

    private void fileClear() throws IOException {
        fileDao.write("", mergedFile);

        LOG.info("Temp file is cleared");
    }

    @Override
    public void run() {
        LOG.info("FileMerging thread has started");
        while (true) {
            try {
                sleep(timeout);
            } catch (InterruptedException e) {
                LOG.error("Sleeping thread is interrupted", e);
            }

            try {
                fileMerge();
            } catch (IOException e) {
                LOG.error("Error during merging files", e);
            }

            try {
                fileClear();
            } catch (IOException e) {
                LOG.error("Temp data file is not cleared", e);
            }
        }
    }
}
