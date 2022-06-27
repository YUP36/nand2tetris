package myAssembler;

import java.io.*;

public class HackAssembler {
  public static void main(String[] args) throws IOException {

    String fileIn = "pong\\Pong.asm";
    String fileOut = "pong\\Pong.hack";
    
    FileWriter writer = new FileWriter(fileOut);
    SymbolTable symbols = new SymbolTable();

    Parser parser1 = new Parser(fileIn);
    int counter = 0;
    while (parser1.hasMoreLines()){
      parser1.advance();
      if(parser1.instructionType().equals("L_INSTRUCTION")){
        symbols.addEntry(parser1.symbol(), counter);
        counter--;
      }
      counter++;
    }

    Parser parser2 = new Parser(fileIn);
    int varCounter = 16;
    while (parser2.hasMoreLines()) {
      String instruction = parser2.advance();
      String value;

      if(parser2.instructionType().equals("A_INSTRUCTION")){
        if(!parser2.symbol().equals("null")){
          if(!symbols.contains(parser2.symbol())){
            symbols.addEntry(parser2.symbol(), varCounter);
            varCounter++;
          }
          instruction = "@" + Long.toString(symbols.getAddress(parser2.symbol()));
        }
        value = Long.toString(decimalToBinary(Long.valueOf(instruction.substring(1))));
        int extraZeroes = 16 - value.length();
        for(int i = 0; i < extraZeroes; i++){
          value = "0" + value;
        }
        writer.write(value + "\n");

      } else if(parser2.instructionType().equals("C_INSTRUCTION")){
        Code decoder = new Code();

        value = "111";
        String dest = decoder.dest(parser2.dest());
        String comp = decoder.comp(parser2.comp());
        String jump = decoder.jump(parser2.jump());

        writer.write(value + comp + dest + jump + "\n");
      }
    }
    writer.close();
  }

  public static long decimalToBinary(long dec){
    String bin = "";
    do {
      bin = String.valueOf(dec % 2) + bin;
      dec = dec / 2;
    } while (dec != 0);
    return Long.parseLong(bin);
  }
}