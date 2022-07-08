import java.io.*;
import java.util.*;

public class CompilationEngine {

    private FileWriter writer;
    private String indent = "";
    JackTokenizer tokenizer;

    private ArrayList<String> statementTokens = new ArrayList<String>(Arrays.asList("let", "if", "while", "do", "return"));
    private ArrayList<String> endingTokens = new ArrayList<String>(Arrays.asList("]", "}", ")", ";"));
    private ArrayList<String> startingTokens = new ArrayList<String>(Arrays.asList("[", "{", "("));

    CompilationEngine(JackTokenizer inTokenizer, String outFile) throws IOException{
        tokenizer = inTokenizer;
        writer = new FileWriter (outFile);
    }


    public void writeStartingOuterTag(String body) throws IOException{
        writer.write(indent + "<" + body + ">\n");
        indent += "\t";
    }

    public void writeEndingOuterTag(String body) throws IOException{
        indent = indent.substring(0, indent.length() - 2);
        writer.write(indent + "</" + body + ">\n");
    }

    public void writeToken() throws IOException{
        String terminalElement = tokenizer.tokenType();
        String token = "";
        switch(terminalElement){
            case "keyword":
                token = tokenizer.keyword();
                break;
            case "symbol": 
                token = tokenizer.symbol();
                break;
            case "integerConstant":
                token = tokenizer.intVal();
                break;
            case "stringConstant":
                token = tokenizer.stringVal();
                break;
            case "identifier":
                token = tokenizer.identifier();
                break;
        }
        writer.write(indent + "<" + terminalElement + "> ");
        writer.write(token);
        writer.write(" <" + terminalElement + ">\n");
        tokenizer.advance();
    }

    public boolean isEnding(){
        return tokenizer.tokenType().equals("symbol") && endingTokens.contains(tokenizer.symbol());
    }

    public boolean isStarting(){
        return tokenizer.tokenType().equals("symbol") && startingTokens.contains(tokenizer.symbol());
    }

    // class className {classVarDec subroutineDec}
    public void compileClass() throws IOException{
        writeStartingOuterTag("class");

        // write class keyword
        writeToken();
        // write className
        writeToken();
        // write {classVarDec subroutineDec}
        writeToken();
        compileClassVarDec();
        compileSubroutineDec();
        writeToken();

        writeEndingOuterTag("class");
    }

    // (static | field) type varName (',' varName);
    public void compileClassVarDec() throws IOException{
        
    }

    // construction|function|method void|type subroutineName (parameterList) subroutineBody
    public void compileSubroutineDec() throws IOException{
        
    }

    // type varName, ...
    public void compileParameterList() throws IOException{
        writeStartingOuterTag("parameterList");

        while(!isEnding()){
            // write type varName
            writeToken();
            writeToken();
        }

        writeEndingOuterTag("parameterList");
    }

    // {varDec* statements}
    public void compileSubroutineBody() throws IOException{
        writeStartingOuterTag("subroutineBody");

        

        writeEndingOuterTag("subroutineBody");
    }
    
    // var type varName (,varName)*;
    public void compileVarDec() throws IOException{
        
    }

    public void compileStatements() throws IOException{
        while(!isEnding()){
            if(tokenizer.tokenType().equals("KEYWORD") && statementTokens.contains(tokenizer.keyword())){
                if(tokenizer.keyword().equals("LET")){
                    compileLet();
                } else if(tokenizer.keyword().equals("IF")){
                    compileIf();
                } else if(tokenizer.keyword().equals("WHILE")){
                    compileWhile();
                } else if(tokenizer.keyword().equals("DO")){
                    compileDo();
                } else if(tokenizer.keyword().equals("RETURN")){
                    compileReturn();
                }
            }
        }
    }
    
    // let varName [expression]? = expression;
    public void compileLet() throws IOException{
        writeStartingOuterTag("letStatement");

        // write let keyword
        writeToken();
        // write varName identifier
        writeToken();
        // checks for [expression]
        if(isStarting()){
            // write [expression]
            writeToken();
            compileExpression();
            writeToken();
        }
        // write = expression;
        writeToken();
        compileExpression();
        writeToken();

        writeEndingOuterTag("letStatement");
    }
    
    // if (expression){statements}else{statements}?
    public void compileIf() throws IOException{
        writeStartingOuterTag("ifStatement");

        // write if keyword
        writeToken();
        // write (expression)
        writeToken();
        compileExpression();
        writeToken();
        // write {statements}
        writeToken();
        compileStatements();
        writeToken();
        // check if there's an else
        if(tokenizer.tokenType().equals("keyword") && tokenizer.keyword().equals("else")){
            // write else{statements}
            writeToken();
            compileStatements();
            writeToken();
        }

        writeEndingOuterTag("ifStatement");
    }

    // while (expression){statements}
    public void compileWhile() throws IOException{
        writeStartingOuterTag("whileStatement");

        // write while keyword
        writeToken();
        // write (expression)
        writeToken();
        compileExpression();
        writeToken();
        // write {statements}
        writeToken();
        compileStatements();
        writeToken();

        writeEndingOuterTag("whileStatement");
    }

    // do subroutineCall;
    public void compileDo() throws IOException{
        writeStartingOuterTag("doStatement");

        // write do keyword
        writeToken();
        // write subroutine call
        while(!isEnding()){
            writer.write("bleh bleh subroutine call");
            tokenizer.advance();
        }
        // write ; symbol
        writeToken();

        writeEndingOuterTag("doStatement");
    }

    // return expression?;
    public void compileReturn() throws IOException{
        writeStartingOuterTag("returnStatement");

        // write return keyword
        writeToken();
        // checks if returning an expression
        if(!isEnding()){
            compileExpression();
        }
        // write ; symbol
        writeToken();

        writeEndingOuterTag("returnStatement");
    }

    public void compileExpression() throws IOException{
        writer.write("blah de blah de blah this is an expression u dumbo");
        while(!isEnding()){
            tokenizer.advance();
        }
    }

    public void compileTerm() throws IOException{
        
    }

    public void compileExpressionList() throws IOException{
        
    }

}
