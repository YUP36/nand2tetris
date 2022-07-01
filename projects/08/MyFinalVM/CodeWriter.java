import java.io.*;
import java.util.*;

public class CodeWriter {

    private FileWriter writer;

    private final ArrayList<String> ASAO = new ArrayList<String>(Arrays.asList("add", "sub", "and", "or"));
    private final ArrayList<String> ASAOSymbols = new ArrayList<String>(Arrays.asList("+", "-", "&", "|"));
    
    private final ArrayList<String> NN = new ArrayList<String>(Arrays.asList("neg", "not"));
    private final ArrayList<String> NNSymbols = new ArrayList<String>(Arrays.asList("-", "!"));

    private final ArrayList<String> EGL = new ArrayList<String>(Arrays.asList("eq", "gt", "lt"));
    private final ArrayList<String> EGLSymbols = new ArrayList<String>(Arrays.asList("JEQ", "JGT", "JLT"));

    // SP -> constant
    // LCL -> local
    // ARG -> argument
    // THIS -> this
    // THAT -> that
    // Foo.{} -> static {}
    // 5+i -> temp
    // -> pointer
    private final ArrayList<String> segmentLATT = new ArrayList<String>(Arrays.asList("local", "argument", "this", "that"));
    private final ArrayList<String> LATT = new ArrayList<String>(Arrays.asList("LCL", "ARG", "THIS", "THAT"));
    private final String[] thisThat = {"THIS", "THAT"};

    private int skipIndex = 0;
    private int ralIndex = 0;

    CodeWriter(String writeFile) throws IOException{
        writer = new FileWriter(writeFile);
    }

    public void setFileName(String fileName){

    }

    public void writeInit(){

    }

    public void writeArithmetic(String command) throws IOException{
        writer.write("// " + command + "\n");
        writer.write("@SP\n");
        writer.write("AM=M-1\n");    // SP--

        if(ASAO.contains(command)){
            String symbol = ASAOSymbols.get(ASAO.indexOf(command));

            writer.write("D=M\n");              // D=*SP
            writer.write("@SP\n");
            writer.write("AM=M-1\n");           // SP--
            writer.write("M=M" + symbol + "D\n");   // D=x+y
            
        } else if(NN.contains(command)){
            String symbol = NNSymbols.get(NN.indexOf(command));

            writer.write("D=" + symbol + "M\n");    // D=-*SP=-y
            writer.write("@SP\n");
            writer.write("A=M\n");
            writer.write("M=D\n");              // *SP=-y
            
        } else if(EGL.contains(command)){
            String symbol = EGLSymbols.get(EGL.indexOf(command));

            writer.write("D=M\n");          // D=*SP
            writer.write("@SP\n");
            writer.write("AM=M-1\n");       // AM=SP--
            writer.write("D=M-D\n");        // D=x-y
            
            writer.write("@SP\n");
            writer.write("A=M\n");
            writer.write("M=-1\n");          // *SP=false although im pretty sure 0 is false...
            writer.write("@SKIP" + Integer.toString(skipIndex) + "\n");
            writer.write("D; " + symbol + "\n");// if (x-y)==0, go SKIP
            writer.write("@SP\n");
            writer.write("A=M\n");
            writer.write("M=0\n");          // *SP=true although im pretty sure 1 is true...
            writer.write("(SKIP" + Integer.toString(skipIndex) + ")\n");   

            skipIndex++;

        }
        writer.write("@SP\n");
        writer.write("M=M+1\n");  // SP++
        return;
    }

    public void writePushPop(String command, String segment, int index) throws IOException{
        if(command.equals("C_PUSH")){
            writer.write("// push " + segment + " " + index + "\n");

            if(segmentLATT.contains(segment)){
                String assemblySymbol = LATT.get(segmentLATT.indexOf(segment));

                writer.write("@" + assemblySymbol + "\n");      // @LCL
                writer.write("D=M\n");                      // D=M      // D=LCL
                writer.write("@"+Integer.toString(index)+"\n"); // @i
                writer.write("A=D+A\n");                    // A=D+A    // A=LCL+i
                writer.write("D=M\n");                      // D=M      // D=[A]

            } else if(segment.equals("constant")){
                writer.write("@" + Integer.toString(index) + "\n"); // @i
                writer.write("D=A\n");                          // D=A

            } else if(segment.equals("static")){
                writer.write("@Foo." + Integer.toString(index) + "\n"); // @Foo.i
                writer.write("D=M\n");                              // D=M

            } else if(segment.equals("temp")){

                writer.write("@5\n");
                writer.write("D=A\n");
                writer.write("@"+Integer.toString(index)+"\n");
                writer.write("A=D+A\n");
                writer.write("D=M\n");

            } else if(segment.equals("pointer")){
                String tT = thisThat[index];
                writer.write("@" + tT + "\n");
                writer.write("D=M\n");

            }
            writer.write("@SP\n");  // @SP
            writer.write("A=M\n");  // A=M
            writer.write("M=D\n");  // M=D
            writer.write("@SP\n");  // @SP 
            writer.write("M=M+1\n");  // M=M+1 

        } else if(command.equals("C_POP")){

            writer.write("// pop " + segment + " " + index + "\n");
            writer.write("@SP\n");                      // @SP 
            writer.write("M=M-1\n");                    // M=M-1    // SP--

            if(segmentLATT.contains(segment)){
                String assemblySymbol = LATT.get(segmentLATT.indexOf(segment));

                writer.write("@" + assemblySymbol + "\n");
                writer.write("D=M\n");                      // D=LCL
                writer.write("@"+Integer.toString(index)+"\n");
                writer.write("D=D+A\n");                    // D=LCL+i
                writer.write("@R13\n");
                writer.write("M=D\n");                      // addr=LCL+i  
                writer.write("@SP\n");                    
                writer.write("A=M\n");
                writer.write("D=M\n");                      // D=SP
                writer.write("@R13\n");                  
                writer.write("A=M\n");                      //A=LCL+i

            } else if(segment.equals("static")){
                writer.write("A=M\n");
                writer.write("D=M\n");
                writer.write("@Foo." + Integer.toString(index) + "\n");

            } else if(segment.equals("temp")){
                writer.write("@5\n");
                writer.write("D=A\n");
                writer.write("@"+Integer.toString(index)+"\n");
                writer.write("D=D+A\n");
                writer.write("@R13\n");
                writer.write("M=D\n");
                writer.write("@SP\n");
                writer.write("A=M\n");
                writer.write("D=M\n");
                writer.write("@R13\n");
                writer.write("A=M\n");

            } else if(segment.equals("pointer")){
                String tT = thisThat[index];
                writer.write("@SP\n");
                writer.write("A=M\n");
                writer.write("D=M\n");
                writer.write("@" + tT + "\n");
            }
            writer.write("M=D\n");    // M=D      //[LCL+i]=SP
        }
        return;
    }

    public void writeLabel(String label) throws IOException{
        writer.write("// label " + label + "\n");
        writer.write("(" + label + ")\n");
        return;
    }

    public void writeGoto(String label) throws IOException{
        writer.write("// goto " + label + "\n");
        writer.write("@" + label + "\n");
        writer.write("0; JMP\n");
    }

    public void writeIf(String label) throws IOException{
        writer.write("// if-goto " + label + "\n");
        writer.write("@SP\n");
        writer.write("AM=M-1\n");
        writer.write("D=M\n");
        writer.write("@" + label + "\n");
        writer.write("D; JEQ\n");
    }

    public void writeFunction(String functionName, int numVars) throws IOException{
        writer.write("(" + functionName + ")\n");
        for(int i = 0; i < numVars; i++){
            writer.write("@SP\n");
            writer.write("A=M\n");
            writer.write("M=0\n");
            writer.write("@SP\n");
            writer.write("M=M+1\n");
        }
    }

    public void writeCall(String functionName, int numArgs) throws IOException{
        writer.write("// " + functionName + " " + Integer.toString(numArgs) + "\n");
        // Makes a label to return to
        writer.write("@returnAddressLabel" + Integer.toString(ralIndex) + "\n");
        writer.write("D=M");
        writer.write("@SP");
        writer.write("A=M");
        writer.write("M=D");
        writer.write("@SP\n");
        writer.write("M=M+1\n");
        // Saves LCL, ARG, THIS, THAT
        for(String symbol : LATT){
            writer.write("@" + symbol + "\n");
            writer.write("D=M\n");
            writer.write("@SP\n");
            writer.write("A=M\n");
            writer.write("M=D\n");
            writer.write("@SP\n");
            writer.write("M=M+1\n");
        }
        // Repositions ARG
        writer.write("@SP\n");
        writer.write("D=M\n");
        writer.write("@5\n");
        writer.write("D=D-A\n");
        writer.write("@" + Integer.toString(numArgs) + "\n");
        writer.write("D=D-A\n");
        writer.write("@ARG\n");
        writer.write("M=D\n");
        // Repositions LCL
        writer.write("@SP\n");
        writer.write("D=M\n");
        writer.write("@LCL\n");
        writer.write("M=D\n");
        // Jumps to the function
        writer.write("@" + functionName + "\n");
        writer.write("0; JMP\n");
        // Makes the return label to jump to
        writer.write("(returnAddressLabel" + Integer.toString(ralIndex) + ")\n");
        ralIndex++;
    }

    public void writeReturn() throws IOException{
        // R14 = LCL
        writer.write("@LCL\n");
        writer.write("D=M\n");
        writer.write("@R14\n");
        writer.write("M=D\n");
        // R15 = *(R14-5)
        writer.write("@5\n");
        writer.write("A=D-A\n");
        writer.write("D=M\n");
        writer.write("@R15\n");
        writer.write("M=D\n");
        // Puts return value on caller stack
        writer.write("@SP\n");
        writer.write("AM=M-1\n");
        writer.write("D=M\n");
        writer.write("@ARG\n");
        writer.write("A=M\n");
        writer.write("M=D\n");
        // Repositions SP to one above returned value
        writer.write("@ARG\n");
        writer.write("D=M\n");
        writer.write("@SP\n");
        writer.write("M=D+1\n");
        // Restores THAT, THIS, ARG, LCL of caller
        for(int backwardIndex = 1; backwardIndex < 5; backwardIndex++){
            writer.write("@R14\n");
            writer.write("D=M\n");
            writer.write("@" + Integer.toString(backwardIndex) + "\n");
            writer.write("A=D-A\n");
            writer.write("D=M\n");
            writer.write("@" + LATT.get(4-backwardIndex) + "\n");
            writer.write("M=D\n");
        }
        // Jump to R15 = return address
        writer.write("@R15\n");
        writer.write("0; JMP\n");
    }

    public void close() throws IOException{
        writer.write("(END)\n@END\n0;JMP");
        writer.close();
    }
}
