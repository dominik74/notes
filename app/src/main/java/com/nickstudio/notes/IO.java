package com.nickstudio.notes;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class IO {
    public static void writeFile(String path, String contents) {
        try {
            FileOutputStream writer = new FileOutputStream(path);
            writer.write(contents.getBytes());
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String readFile(String path) {
        File file = new File(path);

        int length = (int) file.length();

        byte[] bytes = new byte[length];

        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
            in.read(bytes);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new String(bytes);
    }
}
