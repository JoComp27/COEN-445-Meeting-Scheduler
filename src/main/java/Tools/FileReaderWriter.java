package Tools;

import java.io.*;
import java.util.ArrayList;

public class FileReaderWriter {

    private static final String FILE_EXTENSION = ".txt";

    public static void WriteFile(String fileName, String message, boolean append) throws IOException {
        File newFile = new File(fileName + FILE_EXTENSION);

        FileWriter fileWriter = new FileWriter(newFile, append);

        fileWriter.write(message);

        fileWriter.close();

    }

    public static ArrayList<String> ReadFile(String fileName) throws IOException {

        File file = new File(fileName + ".txt");

        ArrayList result = new ArrayList<String>();

        BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName + FILE_EXTENSION));

        String line = bufferedReader.readLine();

        while(line != null) {
            result.add(line);
            line = bufferedReader.readLine();
        }

        return result;

    }

}
