package org.dimamir999.dao;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Repository;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

@Repository(value = "fileDao")
public class FileDao {
    private static final Logger LOG = LogManager.getLogger(FileDao.class);

    public String read(String fileName) throws IOException {
        StringBuilder data = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String currentLine = null;
            while((currentLine = br.readLine()) != null) {
                data.append(currentLine + "\n");
            }
        }

        return data.toString();
    }

    public boolean write(String data, String fileName) throws IOException {
        BufferedWriter bw = null;
        FileWriter fw = null;

        try {
            fw = new FileWriter(fileName);
            bw = new BufferedWriter(fw);
            bw.write(data);
        } catch (IOException ex) {
            LOG.error("File can't be read", ex);
        } finally {
            try {
                if(bw != null) {
                    bw.close();
                }

                if(fw != null) {
                    fw.close();
                }
            } catch (IOException ex) {
                LOG.error("File can't be closed", ex);
            }

        }

        return true;
    }

    public boolean append(String data, String fileName) throws IOException {
        String dataInFile = read(fileName);
        return write(dataInFile + data, fileName);
    }
}
