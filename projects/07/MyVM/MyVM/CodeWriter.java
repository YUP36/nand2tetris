package MyVM;

import java.io.*;
import java.util.*;

public class CodeWriter {

    private FileWriter writer;
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

    CodeWriter(String writeFile) throws IOException{
        writer = new FileWriter(writeFile);
    }

    public void writeArithmetic(String command) throws IOException{
        writer.write("\n// " + command + "\n");
        if(command.equals("add")){
            writer.write("@SP\n");
            writer.write("M=M-1\n");    // SP--
            writer.write("A=M\n");
            writer.write("D=M\n");      // D=*SP
            writer.write("@y\n");
            writer.write("M=D\n");      // y=D
            writer.write("@SP\n");
            writer.write("M=M-1\n");    // SP--
            writer.write("A=M\n");
            writer.write("D=M\n");      // D=*SP
            writer.write("@y\n");
            writer.write("D=D+M\n");    // D=x+y
            writer.write("@SP\n");
            writer.write("M=M+1\n");    // SP++
            writer.write("A=M\n");
            writer.write("M=D\n");      // *SP=D=x+y
            
        } else if(command.equals("add")){
            
        } else if(command.equals("add")){
            
        } else if(command.equals("add")){
            
        } else if(command.equals("add")){
            
        } else if(command.equals("add")){
            
        }
    }

    public void writePushPop(String command, String segment, int index) throws IOException{
        writer.write("\n// " + command + " " + segment + " " + index + "\n");
        if(command.equals("C_PUSH")){
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
            writer.write("M=M+1");  // M=M+1 

        } else if(command.equals("C_POP")){

            writer.write("@SP\n");                      // @SP 
            writer.write("M=M-1\n");                    // M=M-1    // SP--

            if(segmentLATT.contains(segment)){
                String assemblySymbol = LATT.get(segmentLATT.indexOf(segment));

                writer.write("@" + assemblySymbol + "\n");      // @LCL
                writer.write("D=M\n");                      // D=M      // D=LCL
                writer.write("@"+Integer.toString(index)+"\n"); // @i
                writer.write("D=D+A\n");                    // D=D+A    // D=LCL+i
                writer.write("@addr\n");                    // @addr
                writer.write("M=D\n");                      // M=D      // addr=LCL+i  
                writer.write("@SP\n");                      // @SP
                writer.write("D=M\n");                      // D=M      // D=SP
                writer.write("@addr\n");                    // @addr
                writer.write("A=M\n");                      // A=M      //A=LCL+i
                writer.write("M=D");                        // M=D      //[LCL+i]=SP

            } else if(segment.equals("static")){
                writer.write("A=M\n");
                writer.write("D=M\n");
                writer.write("@Foo." + Integer.toString(index) + "\n");
                writer.write("M=D");

            } else if(segment.equals("temp")){
                writer.write("@5\n");
                writer.write("D=A\n");
                writer.write("@"+Integer.toString(index)+"\n");
                writer.write("D=D+A\n");
                writer.write("@addr\n");
                writer.write("M=D\n");
                writer.write("@SP\n");
                writer.write("D=M\n");
                writer.write("@addr\n");
                writer.write("A=M\n");
                writer.write("M=D");

            } else if(segment.equals("pointer")){
                String tT = thisThat[index];
                writer.write("@SP\n");
                writer.write("A=M\n");
                writer.write("D=M\n");
                writer.write("@" + tT + "\n");
                writer.write("M=D");
            }
        }
        return;
    }

    public void close() throws IOException{
        writer.write("@END\n0;JMP");
        writer.close();
    }
}
