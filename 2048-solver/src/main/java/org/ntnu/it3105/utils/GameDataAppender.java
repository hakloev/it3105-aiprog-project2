package org.ntnu.it3105.utils;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * Created by hakloev on 15/11/2015.
 */
public class GameDataAppender {

    private static File file = new File("./game_data.txt");

    public static void appendToFile(String msg) {
        try {
            // 3rd parameter boolean append = true
            FileUtils.writeStringToFile(file, msg, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

