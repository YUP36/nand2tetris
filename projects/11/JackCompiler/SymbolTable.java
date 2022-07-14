import java.util.*;

public class SymbolTable {

    private ArrayList<String[]> argumentTable;
    private ArrayList<String[]> localTable;
    private ArrayList<String[]> fieldTable;
    private ArrayList<String[]> staticTable;

    private ArrayList<ArrayList<String[]>> listOfTables;
    private final ArrayList<String> tableOrder = 
        new ArrayList<String>(Arrays.asList("argument", "local", "field", "static"));

    SymbolTable(){
        argumentTable = new ArrayList<String[]>();
        localTable = new ArrayList<String[]>();
        fieldTable = new ArrayList<String[]>();
        staticTable = new ArrayList<String[]>();

        listOfTables = new ArrayList<ArrayList<String[]>>();
        listOfTables.add(argumentTable);
        listOfTables.add(localTable);
        listOfTables.add(fieldTable);
        listOfTables.add(staticTable);
    }

    public void startSubroutine(){
        argumentTable = new ArrayList<String[]>();
        localTable = new ArrayList<String[]>();

        listOfTables.set(0, argumentTable);
        listOfTables.set(1, localTable);
    }

    public void define(String name, String type, String kind){
        listOfTables.get(tableOrder.indexOf(kind)).add(new String[] {name, type, kind});
    }

    public int varCount(String kind){
        return listOfTables.get(tableOrder.indexOf(kind)).size();
    }

    private String[] find(String name){
        for(ArrayList<String[]> table : listOfTables){
            for(String[] array : table){
                if(array[0].equals(name)){
                    return array;
                }
            }
        }
        return new String[] {"name", "error", "none"};
    }

    public String kindOf(String name){
        String[] variable = find(name);
        return variable[2];
    }

    public String typeOf(String name){
        String[] variable = find(name);
        return variable[1];
    }

    public int indexOf(String name){
        for(ArrayList<String[]> table : listOfTables){
            for(int i = 0; i < table.size(); i++){
                if(table.get(i)[0].equals(name)){
                    return i;
                }
            }
        }
        return 0;
    }
}
