import java.util.*;
import java.io.*;

class JackTokenizer{

    private final ArrayList<String> keywordTokens = new ArrayList<String>(Arrays.asList("class", "constructor", "function", "method", "field", "static", "var", "int", "char", "boolean", "void", "true", "false", "null", "this", "let", "do", "if", "else", "while", "return"));
    private final ArrayList<String> symbolTokens = new ArrayList<String>(Arrays.asList("{", "}", "(", ")", "[", "]", ".", ",", ";", "+", "-", "*", "/", "&", "|", "<", ">", "=", "~"));
    
    private File jackFile;
    private Scanner reader;
    private ArrayList<String> tokens = new ArrayList<String>();
    private int currentIndex = -1;
    private String tokenType;
    private String currentToken;

    JackTokenizer(String fileName) throws IOException{
        jackFile = new File(fileName);
        reader = new Scanner(jackFile);

        boolean commentBlock = false;
        while(reader.hasNextLine()){
            String line = reader.nextLine();

            int commentBlockStart = line.indexOf("/**");
            int commentBlockEnd = line.indexOf("*/");
            if(commentBlock){
                line = "";
            } else if(commentBlockStart >= 0 || commentBlockEnd >= 0){
                line = "";
                commentBlock = (commentBlockStart >= 0) && !(commentBlockEnd >= 0);
            }
            if(line.indexOf("//") >= 0){
                line = line.substring(0, line.indexOf("//"));
            }
            line = line.strip();
            if(line.equals("")){
                continue;
            }

            ArrayList<String> splitByQuotes = new ArrayList<String>(Arrays.asList(line.split("\"")));
            for(int i = 0; i < splitByQuotes.size(); i++){

                ArrayList<String> section = new ArrayList<String>();
                
                if(i%2 == 0){
                    String notString = splitByQuotes.get(i).strip();
                    ArrayList<String> splitBySpace = new ArrayList<String>(Arrays.asList(notString.split(" ")));
                    
                    for(String term : splitBySpace){
                        boolean lastWasSymbol = true;
                        for(int j = 0; j < term.length(); j++){
                            String letter = term.substring(j, j + 1);
                            if(symbolTokens.contains(letter)){
                                section.add(letter);
                                lastWasSymbol = true;
                            } else{
                                if(lastWasSymbol){
                                    section.add("");
                                }
                                int lastIndex = section.size() - 1;
                                section.set(lastIndex, section.get(lastIndex) + letter);
                                lastWasSymbol = false;
                            }
                        }
                    }
                } else {
                    section.add("\"" + splitByQuotes.get(i) + "\"");
                }
                tokens.addAll(section);
            }
        }
    }

    public boolean hasMoreTokens(){
        return currentIndex + 1 < tokens.size();
    }

    public void advance(){
        currentIndex += 1;
        currentToken = tokens.get(currentIndex);
        if(keywordTokens.contains(currentToken)){
            tokenType = "keyword";
        } else if(symbolTokens.contains(currentToken)){
            tokenType = "symbol";
        } else if(Character.isDigit(currentToken.charAt(0))){
            tokenType = "integerConstant";
        } else if(currentToken.charAt(0) == '\"'){
            tokenType = "stringConstant";
        } else{
            tokenType = "identifier";
        }
    }

    public String tokenType(){
        return tokenType;
    }

    public String keyword(){
        return currentToken;    // im not gonna make it all caps
    }

    public String symbol(){
        return currentToken;
    }

    public String identifier(){
        return currentToken;        
    }

    public String intVal(){
        return currentToken;
    }

    public String stringVal(){
        return currentToken.substring(1, currentToken.length() - 1);
    }

}