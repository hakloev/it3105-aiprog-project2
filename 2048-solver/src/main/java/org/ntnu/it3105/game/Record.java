package org.ntnu.it3105.game;

import org.apache.log4j.Logger;

import java.io.*;
import java.util.Properties;

/**
 * Created by Håkon Ødegård Løvdal (hakloev) on 15/10/15.
 * <p>
 */
public class Record {

    private Logger log = Logger.getLogger(Record.class);

    private Properties properties = new Properties();
    private static String PROPERTIES_FILE_NAME = "record.properties";

    private Record() {
        //Private constructor to restrict new instances
        createRecordFileIfNotExists();
        InputStream in;
        try {
            in = new FileInputStream(PROPERTIES_FILE_NAME);
            properties.load(in);
            in.close();
        } catch (IOException e) {
            log.debug("Unable to initialize properties object with content of " + PROPERTIES_FILE_NAME);
        }
    }

    // Singleton pattern;
    private static class RecordManager {
        private static final Record INSTANCE = new Record();
    }

    public static Record getInstance()  {
        return RecordManager.INSTANCE;
    }

    public String getProperty(String key){
        return properties.getProperty(key);
    }

    public boolean containsKey(String key){
        return properties.containsKey(key);
    }

    public void saveRecord(int newRecord) {
        Integer oldRecord = getRecord();

        if (oldRecord > newRecord) {
            return;
        }
        
        OutputStream output = null;
        try {
            properties.setProperty("record", Integer.toString(Math.max(oldRecord, newRecord)));
            output = new FileOutputStream(PROPERTIES_FILE_NAME);
            properties.store(output, null);
        } catch (IOException io) {
            io.printStackTrace();
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    log.error("Unable to close " + PROPERTIES_FILE_NAME + " file");
                }
            }
        }
    }

    public int getRecord() {
        String oldRecord = properties.getProperty("record");
        if (oldRecord != null) {
            return new Integer(oldRecord);
        }
        return 0;
    }

    private void createRecordFileIfNotExists() {
        File f = new File(PROPERTIES_FILE_NAME);
        try {
            if (!f.exists()) {
                f.createNewFile();
            }
        } catch (IOException e) {
            log.error("Unable to create " + PROPERTIES_FILE_NAME);
        }
    }
}
