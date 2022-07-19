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
        tokenizer.advance();
        System.out.print(tokenizer.getToken() + " ");
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
        advanceToken();
        symbolTable.setClassName(tokenizer.getToken());
        advanceToken();
        advanceToken();
        while(staticField()){
            compileClassVarDec();
        }
        while(constructorFunctionMethod()){
            compileSubroutineDec();
        }
        advanceToken();
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

    String currentFunctionType;
    String currentFunctionName;
    // constructor|function|method void|type subroutineName (parameterList) subroutineBody
    public void compileSubroutineDec() throws IOException{
        symbolTable.startSubroutine();
        // write cfm vt subroutineName
        String cfm = tokenizer.getToken();
        advanceToken();
        currentFunctionType = tokenizer.getToken();
        advanceToken();
        String subroutineName = tokenizer.getToken();
        advanceToken();
        // write (parameterList) subroutineBody
        advanceToken();
        compileParameterList();
        advanceToken();
        currentFunctionName = symbolTable.getClassName() + "." + subroutineName;
        // if(cfm.equals("method"))
        compileSubroutineBody();
    }

    // type varName, ...
    public void compileParameterList() throws IOException{
        String type, name;
        if(!isEnding()){
            type = tokenizer.getToken();
            advanceToken();
            name = tokenizer.getToken();
            advanceToken();
            symbolTable.define(name, type, "argument");
        }
        while(!isEnding()){
            advanceToken();
            type = tokenizer.getToken();
            advanceToken();
            name = tokenizer.getToken();
            advanceToken();
            symbolTable.define(name, type, "argument");
            System.out.println("\n" + name + "     " + type);
        }
    }

    int varDecCount = 0;
    // {varDec* statements}
    public void compileSubroutineBody() throws IOException{
        advanceToken();
        varDecCount = 0;
        while(tokenizer.getToken().equals("var")){
            compileVarDec();
        }
        vmWriter.writeFunction(currentFunctionName, varDecCount);
        compileStatements();
        advanceToken();
    }
    
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
        // checks for '['
        if(isStarting()){
            advanceToken();
            compileExpression();
            advanceToken();
            vmWriter.writeArithmetic("add");
        }
        // write = expression;
        advanceToken();
        compileExpression();
        advanceToken();
        vmWriter.writePop(symbolTable.kindOf(name), symbolTable.indexOf(name));
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
        vmWriter.writeIf(L2);
        advanceToken();
        advanceToken();
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
        vmWriter.writePop("temp", 0);
    }

    // return expression?;
    public void compileReturn() throws IOException{
        advanceToken();
        if(!isEnding()){
            compileExpression();
        }
        if(currentFunctionType.equals("void")){
            vmWriter.writePush("constant", 0);
        }
        vmWriter.writeReturn();
        advanceToken();
    }

    // term (op term)*
    public void compileExpression() throws IOException{
        compileTerm();
        while(opTokens.contains(tokenizer.getToken())){
            String op = tokenizer.getToken();
            advanceToken();
            compileTerm();
            // "+", "-", "*", "/", "&", "|", "<", ">", "="
            if(op.equals("+")){
                vmWriter.writeArithmetic("add");
            } else if(op.equals("-")){
                vmWriter.writeArithmetic("sub");
            } else if(op.equals("*")){
                vmWriter.writeCall("Math.multiply", 2);
            } else if(op.equals("/")){
                vmWriter.writeCall("Math.divide", 2);
            } else if(op.equals("&")){
                vmWriter.writeArithmetic("and");
            } else if(op.equals("|")){
                vmWriter.writeArithmetic("or");
            } else if(op.equals("<")){
                vmWriter.writeArithmetic("lt");
            } else if(op.equals(">")){
                vmWriter.writeArithmetic("gt");
            } else if(op.equals("=")){
                vmWriter.writeArithmetic("eq");
            }
        }
    }

    // integerConst|stringConst|keywordConst|varName|varName[expression]|subroutineCall|(expression)|unaryOp term
    public void compileTerm() throws IOException{
        if(tokenizer.tokenType().equals("integerConstant")){
            // integerConst
            vmWriter.writePush("constant", Integer.parseInt(tokenizer.getToken()));
            advanceToken();
        } else if(tokenizer.tokenType().equals("stringConstant")){
            // stringConst
            String str = tokenizer.getToken();
            advanceToken();
            vmWriter.writePush("constant", str.length());
            vmWriter.writeCall("String.new", 1);
            for(int i = 0; i < str.length(); i++){
                vmWriter.writePush("constant", Character.getNumericValue(str.charAt(i)));
                vmWriter.writeCall("String.appendChar", 2);
            }
        } else if(keywordConstantTokens.contains(tokenizer.getToken())){
            // keywordConst
            String keyword = tokenizer.getToken();
            if(keyword.equals("null") || keyword.equals("false")){
                vmWriter.writePush("constant", 0);
            } else if(keyword.equals("true")){
                // true is -1; this could also be accomplished by push 0, not
                vmWriter.writePush("constant", 1);
                vmWriter.writeArithmetic("neg");
            } else{
                vmWriter.writePush("this", 0);
            }
            advanceToken();
        } else if(unaryOpTokens.contains(tokenizer.getToken())){
            String unaryOp = "";
            if(tokenizer.getToken().equals("-")){
                unaryOp = "neg";
            } else if(tokenizer.getToken().equals("~")){
                unaryOp = "not";
            }
            // unaryOp term
            advanceToken();
            compileTerm();
            vmWriter.writeArithmetic(unaryOp);
        } else if(tokenizer.getToken().equals("(")){
            // (expression)
            advanceToken();
            compileExpression();
            advanceToken();
        } else{
            // starts with varName
            tokenizer.advance();
            if(isStarting()){
                if(tokenizer.getToken().equals("[")){
                    // varName[expression]
                    tokenizer.back();
                    String name = tokenizer.getToken();
                    vmWriter.writePush(symbolTable.kindOf(name), symbolTable.indexOf(name));
                    advanceToken();
                    advanceToken();
                    compileExpression();
                    advanceToken();
                    vmWriter.writeArithmetic("add");
                    vmWriter.writePop("pointer", 1);
                } else{
                    // subroutineCall
                    tokenizer.back();
                    subroutineCall();
                }
            } else{
                // varName
                tokenizer.back();
                String name = tokenizer.getToken();
                vmWriter.writePush(symbolTable.kindOf(name), symbolTable.indexOf(name));
                advanceToken();
            }
        }
    }

    public void subroutineCall() throws IOException{
        tokenizer.advance();
        // subroutineName(expressionList) | (className|varName).subroutineName(expressionList)
        if(tokenizer.getToken().equals("(")){
            tokenizer.back();
            // subroutineName(expressionList)
            String subroutineName = tokenizer.getToken();
            advanceToken();
            advanceToken();
            compileExpressionList();
            advanceToken();
            vmWriter.writeCall(subroutineName, expressionListCount);
        } else if(tokenizer.getToken().equals(".")){
            tokenizer.back();
            // (className|varName).subroutineName(expressionList)
            String cvName = tokenizer.getToken();
            advanceToken();
            advanceToken();
            String subroutineName = tokenizer.getToken();
            advanceToken();
            advanceToken();
            compileExpressionList();
            advanceToken();
            String fullName = cvName + "." + subroutineName;
            vmWriter.writeCall(fullName, expressionListCount);
        } else{
            vmWriter.write("SUBROUTINE CALL ERROR");
        }
    }

    int expressionListCount = 0;
    // (expression (',' expression)* )?
    public void compileExpressionList() throws IOException{
        expressionListCount = 0;
        if(!isEnding()){
            expressionListCount = 1;
            compileExpression();
            while(!isEnding()){
                expressionListCount++;
                advanceToken();
                compileExpression();
            }
        }
    }

}
