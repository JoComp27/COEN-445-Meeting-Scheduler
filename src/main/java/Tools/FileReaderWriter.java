package Tools;

import java.io.*;
import java.util.ArrayList;

public class FileReaderWriter {

    private static final String FILE_EXTENSION = ".txt";

    public static void WriteFile(String fileName, String message, boolean append){
        File newFile = new File(fileName + FILE_EXTENSION);

        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(newFile, append);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            fileWriter.write(message);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static ArrayList<String> ReadFile(String fileName) {

        File file = new File(fileName + ".txt");

        ArrayList result = new ArrayList<String>();

        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader(fileName + FILE_EXTENSION));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        String line = null;
        try {
            line = bufferedReader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        while(line != null) {
            result.add(line);
            try {
                line = bufferedReader.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;

    }

}
