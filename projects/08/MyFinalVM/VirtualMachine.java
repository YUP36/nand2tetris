import java.io.*;
import java.util.*;

public class VirtualMachine{
    public static void main(String[] args) throws IOException{

        String directoryName = "projects\\08\\FunctionCalls\\FibonacciElement";
        File directory = new File(directoryName);
        File[] listOfFiles = directory.listFiles();
        ArrayList<String> listOfVMFiles = new ArrayList<String>();

        for(int i = 0; i < listOfFiles.length; i++){
            String fileName = listOfFiles[i].getName();
            int nameLength = fileName.length();
            
            if(fileName.substring(nameLength - 2).equals("vm")){
                listOfVMFiles.add(fileName);
            }
        }

        CodeWriter codeWriter = new CodeWriter(directoryName + "\\" + directory.getName() + ".asm");

        if(listOfVMFiles.contains("Sys.vm")){
            codeWriter.writeInit();
        }

        for(String fileIn : listOfVMFiles){
            Parser parser = new Parser(directoryName + "\\" + fileIn);
            int nameLength = fileIn.length();
            codeWriter.setFileName(fileIn.substring(0, nameLength-3));
            
            while(parser.hasMoreCommands()){
                String command = parser.advance();
                if(parser.commandType().equals("C_ARITHMETIC")){
                    codeWriter.writeArithmetic(command);
                } else if(parser.commandType().equals("C_PUSH") || parser.commandType().equals("C_POP")){
                    codeWriter.writePushPop(parser.commandType(), parser.arg1(), parser.arg2());
                } else if(parser.commandType().equals("C_LABEL")){
                    codeWriter.writeLabel(parser.arg1());
                } else if(parser.commandType().equals("C_GOTO")){
                    codeWriter.writeGoto(parser.arg1());
                } else if(parser.commandType().equals("C_IF")){
                    codeWriter.writeIf(parser.arg1());
                } else if(parser.commandType().equals("C_FUNCTION")){
                    codeWriter.writeFunction(parser.arg1(), parser.arg2());
                } else if(parser.commandType().equals("C_CALL")){
                    codeWriter.writeCall(parser.arg1(), parser.arg2());
                } else if(parser.commandType().equals("C_RETURN")){
                    codeWriter.writeReturn();
                }
            }
        }
        codeWriter.close();
    }   
}
