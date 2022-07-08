import java.io.*;
import java.util.*;

public class JackAnalyzer {
    public static void main(String[] args) throws IOException{
        String directoryName = "projects\\10\\ArrayTest";
        File directory = new File(directoryName);
        File[] listOfFiles = directory.listFiles();
        ArrayList<String> listOfJackFiles = new ArrayList<String>();

        for(int i = 0; i < listOfFiles.length; i++){
            String fileName = listOfFiles[i].getName();
            int nameLength = fileName.length();

            if(fileName.substring(nameLength - 5).equals(".jack")){
                listOfJackFiles.add(fileName.substring(nameLength - 5));
            }
        }

        for(String fileName : listOfJackFiles){
            JackTokenizer tokenizer = new JackTokenizer(directoryName + "\\" + fileName + ".jack");
            new CompilationEngine(tokenizer, directoryName + "\\" + fileName + "Test.xml");
        }
    }
}
