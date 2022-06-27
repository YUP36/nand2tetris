package MyVM;

import java.io.*;

public class VirtualMachine{

    public static void main(String[] args) throws IOException{
        String folder = "";
        String fileName = "";

        String fileIn = folder + "\\" + fileName + ".vm";
        String fileOut = folder + "\\" + fileName + ".asm";

        Parser parser = new Parser(fileIn);
        CodeWriter codeWriter = new CodeWriter(fileOut);

        while(parser.hasMoreCommands()){
            String command = parser.advance();
            if(parser.commandType().equals("C_ARITHMETIC")){
                codeWriter.writeArithmetic(command);
            } else if(parser.commandType().equals("C_PUSH") || parser.commandType().equals("C_POP")){
                codeWriter.writePushPop(parser.commandType(), parser.arg1(), parser.arg2());
            }
        }
        codeWriter.close();
        
    }   
}
