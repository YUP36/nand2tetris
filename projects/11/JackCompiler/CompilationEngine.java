import java.io.*;
import java.util.*;

public class CompilationEngine {

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

    private VMWriter vmWriter;
    private JackTokenizer tokenizer;
    private SymbolTable symbolTable;

    CompilationEngine(JackTokenizer inTokenizer, String outFile) throws IOException{
        tokenizer = inTokenizer;
        symbolTable = new SymbolTable();
        vmWriter = new VMWriter(outFile);
        compileClass();
        vmWriter.close();
    }

    public void advanceToken() throws IOException{
        String token = tokenizer.getToken();
        if(diffOutputs.contains(token)){
            token = diffOutputsValues.get(diffOutputs.indexOf(token));
        }

        String terminalElement = tokenizer.tokenType();
        
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
        advanceToken();
        // write className
        advanceToken();
        // write {classVarDec* subroutineDec*}
        advanceToken();
        while(staticField()){
            compileClassVarDec();
        }
        while(constructorFunctionMethod()){
            compileSubroutineDec();
        }
        advanceToken();

        writeEndingOuterTag("class");
    }

    // (static|field) type varName (',' varName)*;
    public void compileClassVarDec() throws IOException{
        String kind = tokenizer.getToken();
        advanceToken();
        String type = tokenizer.getToken();
        advanceToken();
        String name = tokenizer.getToken();
        advanceToken();
        symbolTable.define(name, type, kind);
        while(!isEnding()){
            advanceToken();
            name = tokenizer.getToken();
            advanceToken();
            symbolTable.define(name, type, kind);
        }
        advanceToken();
    }

    // constructor|function|method void|type subroutineName (parameterList) subroutineBody
    public void compileSubroutineDec() throws IOException{
        writeStartingOuterTag("subroutineDec");

        // write cfm vt subroutineName
        advanceToken();
        advanceToken();
        advanceToken();
        // write (parameterList) subroutineBody
        advanceToken();
        compileParameterList();
        advanceToken();
        compileSubroutineBody();

        writeEndingOuterTag("subroutineDec");
    }

    // type varName, ...
    public void compileParameterList() throws IOException{
        while(!isEnding()){
            String type = tokenizer.getToken();
            advanceToken();
            String name = tokenizer.getToken();
            advanceToken();
            symbolTable.define(name, type, "argument");
        }
    }

    // {varDec* statements}
    public void compileSubroutineBody() throws IOException{
        writeStartingOuterTag("subroutineBody");

        advanceToken();
        while(tokenizer.getToken().equals("var")){
            compileVarDec();
        }
        compileStatements();
        advanceToken();

        writeEndingOuterTag("subroutineBody");
    }
    
    // var type varName (,varName)*;
    public void compileVarDec() throws IOException{
        advanceToken();
        String type = tokenizer.getToken();
        advanceToken();
        String name = tokenizer.getToken();
        advanceToken();
        symbolTable.define(name, type, "local");
        // checks if there's a ';'
        while(!isEnding()){
            // writes ,varName
            advanceToken();
            name = tokenizer.getToken();
            advanceToken();
            symbolTable.define(name, type, "local");
        }
        advanceToken();
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

        advanceToken();
        advanceToken();
        // checks for '['
        if(isStarting()){
            advanceToken();
            compileExpression();
            advanceToken();
        }
        // write = expression;
        advanceToken();
        compileExpression();
        advanceToken();

        writeEndingOuterTag("letStatement");
    }
    
    private int ifIndex = 0;
    // if (expression){statements}else{statements}?
    public void compileIf() throws IOException{
        String L1 = "ifLabel." + Integer.toString(ifIndex);
        String L2 = "elseLabel." + Integer.toString(ifIndex);
        ifIndex++;

        advanceToken();
        advanceToken();
        compileExpression();
        vmWriter.writeArithmetic("not");
        vmWriter.writeIf(L1);
        advanceToken();
        advanceToken();
        compileStatements();
        advanceToken();
        if(tokenizer.getToken().equals("else")){
            vmWriter.writeGoto(L2);
            vmWriter.writeLabel(L1);
            advanceToken();
            advanceToken();
            compileStatements();
            advanceToken();
            vmWriter.writeLabel(L2);
        } else{
            vmWriter.writeLabel(L1);
        }
    }

    private int whileIndex = 0;
    // while (expression){statements}
    public void compileWhile() throws IOException{
        String L1 = "whileLoop." + Integer.toString(whileIndex);
        String L2 = "breakWhileLoop." + Integer.toString(whileIndex);
        whileIndex++;

        advanceToken();
        advanceToken();
        vmWriter.writeLabel(L1);
        compileExpression();
        vmWriter.writeArithmetic("not");
        vmWriter.writeGoto(L2);
        advanceToken();
        advanceToken();
        vmWriter.writeGoto(L1);
        compileStatements();
        vmWriter.writeLabel(L2);
        advanceToken();
    }

    // do subroutineCall;
    public void compileDo() throws IOException{
        writeStartingOuterTag("doStatement");

        advanceToken();
        // write subroutine call
        while(!isEnding()){
            subroutineCall();
        }
        advanceToken();

        writeEndingOuterTag("doStatement");
    }

    // return expression?;
    public void compileReturn() throws IOException{
        advanceToken();
        if(!isEnding()){
            compileExpression();
        }
        vmWriter.writeReturn();
        advanceToken();
    }

    // term (op term)*
    public void compileExpression() throws IOException{
        writeStartingOuterTag("expression");
        
        compileTerm();
        while(opTokens.contains(tokenizer.getToken())){
            advanceToken();
            compileTerm();
        }

        writeEndingOuterTag("expression");
    }

    // integerConst|stringConst|keywordConst|varName|varName[expression]|subroutineCall|(expression)|unaryOp term
    public void compileTerm() throws IOException{
        writeStartingOuterTag("term");
        
        if(tokenizer.tokenType().equals("integerConstant")){
            advanceToken();
        } else if(tokenizer.tokenType().equals("stringConstant")){
            advanceToken();
        } else if(keywordConstantTokens.contains(tokenizer.getToken())){
            advanceToken();
        } else if(unaryOpTokens.contains(tokenizer.getToken())){
            advanceToken();
            compileTerm();
        } else if(tokenizer.getToken().equals("(")){
            advanceToken();
            compileExpression();
            advanceToken();
        } else {
            tokenizer.advance();
            if(isStarting()){
                if(tokenizer.getToken().equals("[")){
                    tokenizer.back();
                    advanceToken();
                    advanceToken();
                    compileExpression();
                    advanceToken();
                } else{
                    tokenizer.back();
                    subroutineCall();
                }
            } else{
                tokenizer.back();
                advanceToken();
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
            advanceToken();
            advanceToken();
            compileExpressionList();
            advanceToken();
        } else if(tokenizer.getToken().equals(".")){
            tokenizer.back();
            // (className|varName).subroutineName(expressionList)
            advanceToken();
            advanceToken();
            advanceToken();
            advanceToken();
            compileExpressionList();
            advanceToken();
        } else{
            vmWriter.write("SUBROUTINE CALL ERROR");
        }
    }

    // (expression (',' expression)* )?
    public void compileExpressionList() throws IOException{
        writeStartingOuterTag("expressionList");
        if(!isEnding()){
            compileExpression();
            while(!isEnding()){
                advanceToken();
                compileExpression();
            }
        }
        writeEndingOuterTag("expressionList");
    }

}
