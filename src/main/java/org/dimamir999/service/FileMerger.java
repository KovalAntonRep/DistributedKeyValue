package org.dimamir999.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dimamir999.dao.FileDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static java.lang.Thread.sleep;

@Component(value = "fileMerger")
public class FileMerger implements Runnable {
    private static final Logger LOG = LogManager.getLogger(FileMerger.class);

    @Value("${name.datafile}")
    private String dataFile;

    @Value("${name.tempfile}")
    private String tempFile;

    @Value("${merging.timeout}")
    private int timeout;

    @Autowired
    private FileDao fileDao;

    public FileMerger() {

    }

    private void fileMerge() throws IOException {
        String mergeData = fileDao.read(tempFile);

        for (String line: mergeData.split("/n")) {
            fileDao.append(line, dataFile);
        }

        LOG.info("Files are successfully merged");
    }

    private void tempFileClear() throws IOException {
        fileDao.write("", tempFile);

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
                tempFileClear();
            } catch (IOException e) {
                LOG.error("Temp data file is not cleared", e);
            }
        }
    }
}
