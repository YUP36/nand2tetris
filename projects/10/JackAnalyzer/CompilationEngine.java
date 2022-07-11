import java.io.*;
import java.util.*;

public class CompilationEngine {

    private FileWriter writer;
    private String indent = "";
    JackTokenizer tokenizer;

    private ArrayList<String> statementTokens = new ArrayList<String>(Arrays.asList("let", "if", "while", "do", "return"));
    private ArrayList<String> endingTokens = new ArrayList<String>(Arrays.asList("]", "}", ")", ";"));
    private ArrayList<String> startingTokens = new ArrayList<String>(Arrays.asList("[", "{", "(", "."));
    private ArrayList<String> sfTokens = new ArrayList<String>(Arrays.asList("static", "field"));
    private ArrayList<String> cfmTokens = new ArrayList<String>(Arrays.asList("constructor", "function", "method"));
    private ArrayList<String> opTokens = new ArrayList<String>(Arrays.asList("+", "-", "*", "/", "&", "|", "<", ">", "="));
    private ArrayList<String> unaryOpTokens = new ArrayList<String>(Arrays.asList("-", "~"));
    private ArrayList<String> keywordConstantTokens = new ArrayList<String>(Arrays.asList("true", "false", "null", "this"));
    private ArrayList<String> diffOutputs = new ArrayList<String>(Arrays.asList("<", ">", "\"", "&"));
    private ArrayList<String> diffOutputsValues = new ArrayList<String>(Arrays.asList("&lt;", "&gt;", "&quot;", "&amp;"));

    CompilationEngine(JackTokenizer inTokenizer, String outFile) throws IOException{
        tokenizer = inTokenizer;
        writer = new FileWriter(outFile);
        compileClass();
        writer.close();
    }

    public void writeStartingOuterTag(String body) throws IOException{
        // System.out.println(indent + "<" + body + ">");
        writer.write(indent + "<" + body + ">\n");
        indent += "  ";
    }

    public void writeEndingOuterTag(String body) throws IOException{
        indent = indent.substring(0, indent.length() - 2);
        writer.write(indent + "</" + body + ">\n");
        // System.out.println(indent + "</" + body + ">");
    }

    public void writeToken() throws IOException{
        String token = tokenizer.getToken();
        if(diffOutputs.contains(token)){
            token = diffOutputsValues.get(diffOutputs.indexOf(token));
        }

        String terminalElement = tokenizer.tokenType();
        writer.write(indent + "<" + terminalElement + "> ");
        writer.write(token);
        writer.write(" </" + terminalElement + ">\n");
        // System.out.println(indent + "<" + terminalElement + "> " + token + " </" + terminalElement + ">");
        tokenizer.advance();
    }

    public boolean isEnding(){
        return endingTokens.contains(tokenizer.getToken());
    }

    public boolean isStarting(){
        return startingTokens.contains(tokenizer.getToken());
    }

    public boolean staticField(){
        return sfTokens.contains(tokenizer.getToken());
    }

    public boolean constructorFunctionMethod(){
        return cfmTokens.contains(tokenizer.getToken());
    }

    // class className {classVarDec* subroutineDec*}
    public void compileClass() throws IOException{
        writeStartingOuterTag("class");

        // write class keyword
        writeToken();
        // write className
        writeToken();
        // write {classVarDec* subroutineDec*}
        writeToken();
        while(staticField()){
            compileClassVarDec();
        }
        while(constructorFunctionMethod()){
            compileSubroutineDec();
        }
        writeToken();

        writeEndingOuterTag("class");
    }

    // (static|field) type varName (',' varName)*;
    public void compileClassVarDec() throws IOException{
        writeStartingOuterTag("classVarDec");

        // write static|field type varName
        writeToken();
        writeToken();
        writeToken();
        while(!isEnding()){
            // write ,varName
            writeToken();
            writeToken();
        }
        writeToken();

        writeEndingOuterTag("classVarDec");
    }

    // construction|function|method void|type subroutineName (parameterList) subroutineBody
    public void compileSubroutineDec() throws IOException{
        writeStartingOuterTag("subroutineDec");

        // write cfm vt subroutineName
        writeToken();
        writeToken();
        writeToken();
        // write (parameterList) subroutineBody
        writeToken();
        compileParameterList();
        writeToken();
        compileSubroutineBody();

        writeEndingOuterTag("subroutineDec");
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

        writeToken();
        while(tokenizer.getToken().equals("var")){
            compileVarDec();
        }
        compileStatements();
        writeToken();

        writeEndingOuterTag("subroutineBody");
    }
    
    // var type varName (,varName)*;
    public void compileVarDec() throws IOException{
        writeStartingOuterTag("varDec");

        // write var type varName
        writeToken();
        writeToken();
        writeToken();
        // checks if there's a ';'
        while(!isEnding()){
            // writes ,varName
            writeToken();
            writeToken();
        }
        writeToken();

        writeEndingOuterTag("varDec");
    }

    // statement*
    public void compileStatements() throws IOException{
        writeStartingOuterTag("statements");

        // letStatement|ifStatement|whileStatement...
        while(statementTokens.contains(tokenizer.getToken())){
            if(tokenizer.getToken().equals("let")){
                compileLet();
            } else if(tokenizer.getToken().equals("if")){
                compileIf();
            } else if(tokenizer.getToken().equals("while")){
                compileWhile();
            } else if(tokenizer.getToken().equals("do")){
                compileDo();
            } else if(tokenizer.getToken().equals("return")){
                compileReturn();
            }
        }

        writeEndingOuterTag("statements");
    }
    
    // let varName [expression]? = expression;
    public void compileLet() throws IOException{
        writeStartingOuterTag("letStatement");

        writeToken();
        writeToken();
        // checks for '['
        if(isStarting()){
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

        // write if(expression){statements}
        writeToken();
        writeToken();
        compileExpression();
        writeToken();
        writeToken();
        compileStatements();
        writeToken();
        // check if there's an else
        if(tokenizer.getToken().equals("else")){
            writeToken();
            writeToken();
            compileStatements();
            writeToken();
        }

        writeEndingOuterTag("ifStatement");
    }

    // while (expression){statements}
    public void compileWhile() throws IOException{
        writeStartingOuterTag("whileStatement");

        writeToken();
        writeToken();
        compileExpression();
        writeToken();
        writeToken();
        compileStatements();
        writeToken();

        writeEndingOuterTag("whileStatement");
    }

    // do subroutineCall;
    public void compileDo() throws IOException{
        writeStartingOuterTag("doStatement");

        writeToken();
        // write subroutine call
        while(!isEnding()){
            subroutineCall();
        }
        writeToken();

        writeEndingOuterTag("doStatement");
    }

    // return expression?;
    public void compileReturn() throws IOException{
        writeStartingOuterTag("returnStatement");

        writeToken();
        if(!isEnding()){
            compileExpression();
        }
        writeToken();

        writeEndingOuterTag("returnStatement");
    }

    // term (op term)*
    public void compileExpression() throws IOException{
        writeStartingOuterTag("expression");
        
        compileTerm();
        while(opTokens.contains(tokenizer.getToken())){
            writeToken();
            compileTerm();
        }

        writeEndingOuterTag("expression");
    }

    // integerConst|stringConst|keywordConst|varName|varName[expression]|subroutineCall|(expression)|unaryOp term
    public void compileTerm() throws IOException{
        writeStartingOuterTag("term");
        
        if(tokenizer.tokenType().equals("integerConstant")){
            writeToken();
        } else if(tokenizer.tokenType().equals("stringConstant")){
            writeToken();
        } else if(keywordConstantTokens.contains(tokenizer.getToken())){
            writeToken();
        } else if(unaryOpTokens.contains(tokenizer.getToken())){
            writeToken();
            compileTerm();
        } else if(tokenizer.getToken().equals("(")){
            writeToken();
            compileExpression();
            writeToken();
        } else {
            tokenizer.advance();
            if(isStarting()){
                if(tokenizer.getToken().equals("[")){
                    tokenizer.back();
                    writeToken();
                    writeToken();
                    compileExpression();
                    writeToken();
                } else{
                    tokenizer.back();
                    subroutineCall();
                }
            } else{
                tokenizer.back();
                writeToken();
            }
        }
        
        writeEndingOuterTag("term");
    }

    public void subroutineCall() throws IOException{
        tokenizer.advance();
        // subroutineName(expressionList) | (className|varName).subroutineName(expressionList)
        if(tokenizer.getToken().equals("(")){
            tokenizer.back();
            // subroutineName(expressionList)
            writeToken();
            writeToken();
            compileExpressionList();
            writeToken();
        } else if(tokenizer.getToken().equals(".")){
            tokenizer.back();
            // (className|varName).subroutineName(expressionList)
            writeToken();
            writeToken();
            writeToken();
            writeToken();
            compileExpressionList();
            writeToken();
        } else{
            writer.write("SUBROUTINE CALL ERROR");
        }
    }

    public void compileExpressionList() throws IOException{
        writeStartingOuterTag("expressionList");
        if(!isEnding()){
            compileExpression();
            while(!isEnding()){
                writeToken();
                compileExpression();
            }
        }
        writeEndingOuterTag("expressionList");
    }

}
