import java.io.*;
import java.util.*;

public class CompilationEngine {

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

    public void advanceToken(int num) throws IOException{
        for(int i = 0; i < num; i++){
            tokenizer.advance();
        }
    }
    
    public void advanceToken() throws IOException{
        advanceToken(1);
    }

    private ArrayList<String> endingTokens = new ArrayList<String>(Arrays.asList("]", "}", ")", ";"));
    public boolean isEnding(){
        return endingTokens.contains(tokenizer.getToken());
    }

    private ArrayList<String> startingTokens = new ArrayList<String>(Arrays.asList("[", "{", "(", "."));
    public boolean isStarting(){
        return startingTokens.contains(tokenizer.getToken());
    }

    private ArrayList<String> sfTokens = new ArrayList<String>(Arrays.asList("static", "field"));
    public boolean staticField(){
        return sfTokens.contains(tokenizer.getToken());
    }

    private ArrayList<String> cfmTokens = new ArrayList<String>(Arrays.asList("constructor", "function", "method"));
    public boolean constructorFunctionMethod(){
        return cfmTokens.contains(tokenizer.getToken());
    }

    public String getKind(String name){
        if(symbolTable.kindOf(name).equals("field")){
            return "this";
        } else{
            return symbolTable.kindOf(name);
        }
    }

    // class className {classVarDec* subroutineDec*}
    public void compileClass() throws IOException{
        advanceToken();
        symbolTable.setClassName(tokenizer.getToken());
        advanceToken(2);
        while(staticField()){
            compileClassVarDec();
        }
        while(constructorFunctionMethod()){
            compileSubroutineDec();
        }
        advanceToken();
    }

    private int classVarDecCount = 0;
    // (static|field) type varName (',' varName)*;
    public void compileClassVarDec() throws IOException{
        classVarDecCount++;
        String kind = tokenizer.getToken();
        advanceToken();
        String type = tokenizer.getToken();
        advanceToken();
        String name = tokenizer.getToken();
        advanceToken();
        symbolTable.define(name, type, kind);
        while(!isEnding()){
            classVarDecCount++;
            advanceToken();
            name = tokenizer.getToken();
            advanceToken();
            symbolTable.define(name, type, kind);
        }
        advanceToken();
    }

    String currentFunctionType;
    String currentFunctionName;
    String cfm;
    // constructor|function|method void|type subroutineName (parameterList) subroutineBody
    public void compileSubroutineDec() throws IOException{
        symbolTable.startSubroutine();
        // write cfm vt subroutineName
        cfm = tokenizer.getToken();
        advanceToken();
        currentFunctionType = tokenizer.getToken();
        advanceToken();
        currentFunctionName = symbolTable.getClassName() + "." + tokenizer.getToken();
        advanceToken(2);
        // write (parameterList) subroutineBody
        compileParameterList();
        advanceToken(); 
        compileSubroutineBody();
    }


    // type varName, ...
    public void compileParameterList() throws IOException{
        if(cfm.equals("method")){
            symbolTable.define(currentFunctionName, "object", "argument");
        }
        String type, name;
        while(!isEnding()){
            type = tokenizer.getToken();
            advanceToken();
            name = tokenizer.getToken();
            advanceToken();
            symbolTable.define(name, type, "argument");
            if(!isEnding()){
                advanceToken();
            }
        }
    }
 
    // {varDec* statements}
    public void compileSubroutineBody() throws IOException{
        advanceToken();
        varDecCount = 0;
        while(tokenizer.getToken().equals("var")){
            compileVarDec();
        }
        vmWriter.writeFunction(currentFunctionName, varDecCount);
        if(cfm.equals("constructor")){
            vmWriter.writePush("constant", classVarDecCount);
            vmWriter.writeCall("Memory.alloc", 1);
            vmWriter.writePop("pointer", 0);
        } else if(cfm.equals("method")){
            vmWriter.writePush("argument", 0);
            vmWriter.writePop("pointer", 0);
        }
        compileStatements();
        advanceToken();
    }
    
    int varDecCount = 0;
    // var type varName (,varName)*;
    public void compileVarDec() throws IOException{
        varDecCount++;
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
            varDecCount++;
        }
        advanceToken();
    }

    private ArrayList<String> statementTokens = new ArrayList<String>(Arrays.asList("let", "if", "while", "do", "return"));
    // statement*
    public void compileStatements() throws IOException{
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
    }
    
    // let varName [expression]? = expression;
    public void compileLet() throws IOException{
        advanceToken();
        String name = tokenizer.getToken();
        advanceToken();
        if(isStarting()){   // checks for '['
            advanceToken();
            compileExpression();
            advanceToken(2);
            vmWriter.writePush(getKind(name), symbolTable.indexOf(name));
            vmWriter.writeArithmetic("add");
            compileExpression();
            advanceToken();
            vmWriter.writePop("temp", 0);
            vmWriter.writePop("pointer", 1);
            vmWriter.writePush("temp", 0);
            vmWriter.writePop("that", 0);
        } else{     // write = expression;
            advanceToken();
            compileExpression();
            advanceToken();
            vmWriter.writePop(getKind(name), symbolTable.indexOf(name));
        }
    }
    
    private int ifIndex = 0;
    // if (expression){statements}else{statements}?
    public void compileIf() throws IOException{
        String L1 = "ifLabel." + Integer.toString(ifIndex);
        String L2 = "elseLabel." + Integer.toString(ifIndex);
        ifIndex++;

        advanceToken(2);
        compileExpression();
        vmWriter.writeArithmetic("not");
        vmWriter.writeIf(L1);
        advanceToken(2);
        compileStatements();
        advanceToken();
        if(tokenizer.getToken().equals("else")){
            vmWriter.writeGoto(L2);
            vmWriter.writeLabel(L1);
            advanceToken(2);
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

        advanceToken(2);
        vmWriter.writeLabel(L1);
        compileExpression();
        vmWriter.writeArithmetic("not");
        vmWriter.writeIf(L2);
        advanceToken(2);
        compileStatements();
        vmWriter.writeGoto(L1);
        vmWriter.writeLabel(L2);
        advanceToken();
    }

    // do subroutineCall;
    public void compileDo() throws IOException{
        advanceToken();
        // write subroutine call
        while(!isEnding()){
            subroutineCall();
        }
        advanceToken();
        // do is called for void methods, so we ignore the return value
        vmWriter.writePop("temp", 0);
    }

    // return expression?;
    public void compileReturn() throws IOException{
        advanceToken();
        if(!isEnding()){
            compileExpression();
        }
        if(currentFunctionType.equals("void")){
            // void methods return 0 by default
            vmWriter.writePush("constant", 0);
        }
        vmWriter.writeReturn();
        advanceToken();
    }

    private ArrayList<String> opTokens = new ArrayList<String>(Arrays.asList("+", "-", "*", "/", "&", "|", "<", ">", "="));
    private ArrayList<String> commands = new ArrayList<String>(Arrays.asList("add", "sub", "Math.multiply", "Math.divide", "and", "or", "lt", "gt", "eq"));
    private ArrayList<String> advancedOpTokens = new ArrayList<String>(Arrays.asList("*", "/"));
    private ArrayList<String> advancedCommands = new ArrayList<String>(Arrays.asList("Math.multiply", "Math.divide"));
    // term (op term)*
    public void compileExpression() throws IOException{
        compileTerm();
        while(opTokens.contains(tokenizer.getToken())){
            String op = tokenizer.getToken();
            advanceToken();
            compileTerm();
            if(advancedOpTokens.contains(op)){
                vmWriter.writeCall(advancedCommands.get(advancedOpTokens.indexOf(op)), 2);
            } else{
                vmWriter.writeArithmetic(commands.get(opTokens.indexOf(op)));
            }
        }
    }

    private ArrayList<String> unaryOpTokens = new ArrayList<String>(Arrays.asList("-", "~"));
    private ArrayList<String> unaryOpCommands = new ArrayList<String>(Arrays.asList("neg", "not"));
    private ArrayList<String> keywordConstantTokens = new ArrayList<String>(Arrays.asList("true", "false", "null", "this"));
    // integerConst|stringConst|keywordConst|varName|varName[expression]|subroutineCall|(expression)|unaryOp term
    public void compileTerm() throws IOException{
        if(tokenizer.tokenType().equals("integerConstant")){            // integerConst
            vmWriter.writePush("constant", Integer.parseInt(tokenizer.getToken()));
            advanceToken();
        } else if(tokenizer.tokenType().equals("stringConstant")){      // stringConst
            String str = tokenizer.getToken();
            advanceToken();
            vmWriter.writePush("constant", str.length());
            vmWriter.writeCall("String.new", 1);
            for(int i = 0; i < str.length(); i++){
                vmWriter.writePush("constant", (int) str.charAt(i));
                vmWriter.writeCall("String.appendChar", 2);
            }
        } else if(keywordConstantTokens.contains(tokenizer.getToken())){         // keywordConst
            String keyword = tokenizer.getToken();
            if(keyword.equals("null") || keyword.equals("false")){
                vmWriter.writePush("constant", 0);
            } else if(keyword.equals("true")){
                // true is -1; this could also be accomplished by push 0, not
                vmWriter.writePush("constant", 1);
                vmWriter.writeArithmetic("neg");
            } else{
                vmWriter.writePush("pointer", 0);
            }
            advanceToken();
        } else if(unaryOpTokens.contains(tokenizer.getToken())){                 // unaryOp term 
            String unaryOp = unaryOpCommands.get(unaryOpTokens.indexOf(tokenizer.getToken()));
            advanceToken();
            compileTerm();
            vmWriter.writeArithmetic(unaryOp);
        } else if(tokenizer.getToken().equals("(")){                    // (expression)
            advanceToken();
            compileExpression();
            advanceToken();
        } else{                                                                 // starts with varName
            tokenizer.advance();
            if(isStarting()){
                if(tokenizer.getToken().equals("[")){   // varName[expression]
                    tokenizer.back();
                    String name = tokenizer.getToken();
                    advanceToken(2);
                    compileExpression();
                    advanceToken();
                    vmWriter.writePush(getKind(name), symbolTable.indexOf(name));
                    vmWriter.writeArithmetic("add");
                    vmWriter.writePop("pointer", 1);
                    vmWriter.writePush("that", 0);
                } else{     // subroutineCall
                    tokenizer.back();
                    subroutineCall();
                }
            } else{     // varName
                tokenizer.back();
                String name = tokenizer.getToken();
                vmWriter.writePush(getKind(name), symbolTable.indexOf(name));
                advanceToken();
            }
        }
    }

    // subroutineName(expressionList) | (className|varName).subroutineName(expressionList)
    public void subroutineCall() throws IOException{
        tokenizer.advance();
        String fullName = "ERROR";
        if(tokenizer.getToken().equals("(")){   // subroutineName(expressionList)
            tokenizer.back();
            vmWriter.writePush("pointer", 0);
            String subroutineName = tokenizer.getToken();
            advanceToken(2);
            expressionListCount = 1;
            compileExpressionList();
            advanceToken();
            fullName = symbolTable.getClassName() + "." + subroutineName;
        } else if(tokenizer.getToken().equals(".")){ // (className|varName).subroutineName(expressionList)
            tokenizer.back();
            // this is a method call or a constructor
            String cvName = tokenizer.getToken();
            advanceToken(2);
            String subroutineName = tokenizer.getToken();
            if(getKind(cvName).equals("none")){ // this means it's a constructor
                fullName = cvName + "." + subroutineName;
                expressionListCount = 0;
            } else{ // otherwise it's a method call on an object, which is passed implicitly
                fullName = symbolTable.typeOf(cvName) + "." + subroutineName;
                vmWriter.writePush(getKind(cvName), symbolTable.indexOf(cvName));
                expressionListCount = 1;
            }
            advanceToken(2);
            compileExpressionList();
            advanceToken();
        }
        vmWriter.writeCall(fullName, expressionListCount);
    }

    private int expressionListCount = 0;
    // (expression (',' expression)* )?
    public void compileExpressionList() throws IOException{
        if(!isEnding()){
            expressionListCount++;
            compileExpression();
            while(!isEnding()){
                expressionListCount++;
                advanceToken();
                compileExpression();
            }
        }
    }

}
