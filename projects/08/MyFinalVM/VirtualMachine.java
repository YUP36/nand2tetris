import java.io.*;

public class VirtualMachine{

    public static void main(String[] args) throws IOException{
        String folder = "projects\\08\\ProgramFlow";
        String fileName = "BasicLoop\\BasicLoop";

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
        codeWriter.close();
        
    }   
}
