package myAssembler;

import java.io.File;  // Import the File class
import java.io.FileNotFoundException;  // Import this class to handle errors
import java.util.Scanner; // Import the Scanner class to read text files

public class Parser {

    public String filename;
    private String currentInstruction;
    private String type;
    private File assembly;
    private Scanner reader;
    private final String ASTRUCTION = "A_INSTRUCTION";
    private final String LSTRUCTION = "L_INSTRUCTION";
    private final String CSTRUCTION = "C_INSTRUCTION";
    private final String[] DIGITS = {"0","1","2","3","4","5","6","7","8","9"};

    Parser(String filename) throws FileNotFoundException {
        assembly = new File(filename);
        reader = new Scanner(assembly);
    }

    public boolean hasMoreLines(){
        return reader.hasNextLine();
    }

    public String advance(){
        do {
            currentInstruction = reader.nextLine().strip();
            int doubleSlashLoc = currentInstruction.indexOf("//");
            if(doubleSlashLoc >= 0){
                currentInstruction = currentInstruction.substring(0, doubleSlashLoc).strip();
            } 
        } while (currentInstruction.equals(""));

        instructionType();
        return currentInstruction;

    }

    public String instructionType() {
        if (currentInstruction.substring(0, 1).equals("@")){
            type = ASTRUCTION;
            return ASTRUCTION;
        } else if (currentInstruction.substring(0, 1).equals("(")) {
            type = LSTRUCTION;
            return LSTRUCTION;
        } else{
            type = CSTRUCTION;
            return CSTRUCTION;
        }
    }

    public String symbol() {
        if (type.equals(ASTRUCTION)){
            String symbol = currentInstruction.substring(1);
            if (! isInteger(symbol)){
                return symbol;
            }
        } else if (type.equals(LSTRUCTION)){
            String symbol = currentInstruction.substring(1, currentInstruction.length()-1);
            return symbol;
        }
        return "null";
    }

    public String dest() {
        int equalsLoc = currentInstruction.indexOf("=");
        if (type.equals(CSTRUCTION) && equalsLoc >= 0){
            String dest = currentInstruction.substring(0, equalsLoc);
            // if (firstLetter.equals("D") || firstLetter.equals("M") || firstLetter.equals("A")){}
            return dest;
        }
        return "null";
    }

    public String comp() {
        int locEquals = currentInstruction.indexOf("=");
        int locSemi = currentInstruction.indexOf(";");
        if (locEquals >= 0){
            if (locSemi >= 0){
                return currentInstruction.substring(locEquals + 1, locSemi);
            } else {
                return currentInstruction.substring(locEquals + 1);
            }
        } else if (locSemi >= 0){
            return currentInstruction.substring(0, locSemi);
        }
        // System.out.println(currentInstruction);
        return "null";
    }

    public String jump() {
        int loc = currentInstruction.indexOf(";");
        if (loc >= 0){
            return currentInstruction.substring(loc + 1);
        }
        return "null";
    }

    public boolean isInteger(String input){
    String firstDigit = input.substring(0, 1);
        for(String digit : DIGITS){
            if (digit.equals(firstDigit)){
                return true;
            }
        }
        return false;
    }
}
