package myAssembler;

import java.util.*;

public class SymbolTable {
    
    private ArrayList <String> symbolArray = new ArrayList<String>(Arrays.asList("R0","R1","R2","R3","R4","R5","R6","R7","R8","R9","R10","R11","R12","R13","R14","R15","SCREEN","KBD","SP","LCL","ARG","THIS","THAT"));
    private ArrayList <Integer> addressArray = new ArrayList<Integer>(Arrays.asList(0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16384,24576,0,1,2,3,4));

    public void addEntry(String symbol, int address){
        symbolArray.add(symbol);
        addressArray.add(address);
        return;
    }

    public boolean contains(String symbol){
        return symbolArray.contains(symbol);
    }

    public int getAddress(String symbol){
        int index = symbolArray.indexOf(symbol);
        if(index >= 0){
            return addressArray.get(index);
        }
        return -1;
    }
}
