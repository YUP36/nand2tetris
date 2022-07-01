import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Parser {

    public String filename;
    private String currentCommand;
    private String[] commandArr;
    private File vmCode;
    private Scanner reader;
    // private final String[] DIGITS = {"0","1","2","3","4","5","6","7","8","9"};

    Parser(String filename) throws FileNotFoundException {
        vmCode = new File(filename);
        reader = new Scanner(vmCode);
    }

    public boolean hasMoreCommands(){
        return reader.hasNextLine();
    }

    public String advance(){
        do {
            currentCommand = reader.nextLine().strip();
            int doubleSlashLoc = currentCommand.indexOf("//");
            if(doubleSlashLoc >= 0){
                currentCommand = currentCommand.substring(0, doubleSlashLoc).strip();
            } 
        } while (currentCommand.equals(""));

        commandArr = currentCommand.split(" ");
        return currentCommand;
    }

    public String commandType(){
        String command = commandArr[0];
        if(command.equals("push")){
            return "C_PUSH";
        } else if(command.equals("pop")){
            return "C_POP";
        } else if(command.equals("label")){
            return "C_LABEL";
        } else if(command.equals("goto")){
            return "C_GOTO";
        } else if(command.equals("if-goto")){
            return "C_IF";
        } else if(command.equals("function")){
            return "C_FUNCTION";
        } else if(command.equals("call")){
            return "C_CALL";
        } else if(command.equals("return")){
            return "C_RETURN";
        } else {
            return "C_ARITHMETIC";
        }
    }

    public String arg1(){
        return commandArr[1];
    }

    public int arg2(){
        return Integer.parseInt(commandArr[2]);
    }
}
