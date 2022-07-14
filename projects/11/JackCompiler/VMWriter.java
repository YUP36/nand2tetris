import java.io.*;

public class VMWriter {

    private FileWriter writer;
    
    VMWriter(String outputFile) throws IOException{
        writer = new FileWriter(outputFile);
    }

    public void write(String input) throws IOException{
        writer.write(input + "\n");
    }

    public void writePush(String segment, int index) throws IOException{
        write("push " + segment + " " + index);
    }

    public void writePop(String segment, int index) throws IOException{
        write("pop" + segment + " " + index);
    }

    public void writeArithmetic(String command) throws IOException{
        write(command);
    }

    public void writeLabel(String label) throws IOException{
        write("label " + label);
    }

    public void writeGoto(String label) throws IOException{
        write("goto " + label);
    }

    public void writeIf(String label) throws IOException{
        write("if-goto " + label);
    }

    public void writeCall(String name, int nArgs) throws IOException{
        write("call " + name + " " + Integer.toString(nArgs));
    }

    public void writeFunction(String name, int nLocals) throws IOException{
        write("function " + name + " " + Integer.toString(nLocals));
    }

    public void writeReturn() throws IOException{
        write("return");
    }

    public void close() throws IOException{
        writer.close();
    }
}
