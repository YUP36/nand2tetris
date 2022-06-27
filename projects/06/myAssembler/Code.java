package myAssembler;

import java.util.*;

public class Code {

    private ArrayList <String> comps = new ArrayList<String>(Arrays.asList("0","1","-1","D","A","!D","!A","-D","-A","D+1","A+1","D-1","A-1","D+A","D-A","A-D","D&A","D|A"));
    private ArrayList <String> codes = new ArrayList<String>(Arrays.asList("101010","111111","111010","001100","110000","001101","110001","001111","110011","011111","110111","001110","110010","000010","010011","000111","000000","010101"));
    
    public String dest(String dest){
        if(dest.equals("null")){
            return "000";
        }

        String[] letters = {"A", "D", "M"};
        String out = "";

        for (String letter : letters){
            if(dest.indexOf(letter) >= 0){
                out = out + "1";
            } else{
                out = out + "0";
            }
        }
        return out;
    }

    public String comp(String comp){
        // System.out.println(comp);
        //comp = comp.strip();
        String a = "0";
        String newComp = comp;

        int mLoc = comp.indexOf("M");
        if(mLoc >= 0){
            newComp = comp.substring(0, mLoc) + "A" + comp.substring(mLoc+1);
            a = "1";
        }
        return a + codes.get(comps.indexOf(newComp));
    }

    public String jump(String jump){
        //jump = jump.strip();
        if(jump.equals("null")){
            return "000";
        } else if(jump.equals("JGT")){
            return "001";
        } else if(jump.equals("JEQ")){
            return "010";
        } else if(jump.equals("JGE")){
            return "011";
        } else if(jump.equals("JLT")){
            return "100";
        } else if(jump.equals("JNE")){
            return "101";
        } else if(jump.equals("JLE")){
            return "110";
        } else if(jump.equals("JMP")){
            return "111";
        } else {
            return "ERROR";
        }
    }
}
