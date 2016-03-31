/**
 * Created by Aedo on 2/6/16.
 */
package ek1535_lab01;
import java.awt.image.AreaAveragingScaleFilter;
import java.util.*;
import java.io.*;

public class TwoPassLinker {

    static List<Integer> baseAddress = new ArrayList<Integer>(); // Module #; baseAddres of module;
    // size of module is # of text entries
    static Map symbolTable = new HashMap<String, Integer>(); // hash to store (symbol, absolute address)
   //static Map<Integer, Integer> moduleAddress = new HashMap<Integer, Integer>(); // has to store (module#, module's base address);
    //static List wordList = new ArrayList(); // word list
    //List<Integer> wordList = new ArrayList<Integer>(); //list of words
    //////// map only stores one value for each
    static Map useMap = new HashMap<String, Integer>(); //map of use symbols and address
    static Map wordMap = new HashMap<String, Integer>(); //map of type and word
    static List<Integer> modules= new ArrayList<Integer>(); //module# and its length


    public static void firstPass(Scanner input) {

        int rows = 0; // # of lines in each file; 1 def, 2 use, 3 prog txt
        int columns = 0; // # of things in each line

        String symbol = "";
        int relativeAddress = 0; // relative address
        int absoluteAddress; // absolute address

        int moduleNumber = -1;
        int moduleLength = 0; //offset address

        List useList = new ArrayList(); // list of use symbols
        List definitionList = new ArrayList(); // list of definition symbols

        List moduleSymbol = new ArrayList(); // list of symbol for current module
        List moduleAddress = new ArrayList(); // list of address for current module


        // traverse entire file
        while(input.hasNext()) {
            rows++; // 1
            columns = input.nextInt(); // 1

            // figure out base address of current module
            // check for beginning of new module
            if((rows-1)%3 == 0) {
                moduleNumber++;
                baseAddress.add(moduleLength); //first module length/offset address is 0
            }

            // traverse entire line
            for(int i = 0; i < columns; i++) { // skips line if column=0
                symbol = input.next(); // xy
                relativeAddress = input.nextInt(); // 2

                // definition list
                if((rows-1)%3 == 0) {
                    //check for multiple def'n
                    if(!symbolTable.containsKey(symbol)) {
                        //calculate absolute address for symbol
                        absoluteAddress = relativeAddress + baseAddress.get(moduleNumber);
                        symbolTable.put(symbol, absoluteAddress); //store symbol and address
                        //change def list to hash-map to also set which module its defined in
                        definitionList.add(symbol); //store symbol for comparison to use list
                    } else {
                        System.out.printf("Error: multiple symbol definition for %s\n", symbol);
                    }
                }

                // use list
                if((rows-2)%3 == 0) {
                    //
                    if(!useList.contains(symbol)) {
                        useList.add(symbol);
                        //useMap.put(symbol, relativeAddress);
                    }
                }

                // program text
                if((rows-3)%3 == 0) {
                    //wordMap.put(symbol, relativeAddress);
                    // if moduleNumb > 0
                    moduleLength++;// = columns;
                    //moduleLength = input.nextInt();
                    // else moduleLength = columns
                }
            }  // end of line

            if((rows-3)%3 == 0) {
                modules.add(columns);
            }
        } // end of file

        //check for used but not defined
        //check for defined but not used
        for(int i = 0; i < definitionList.size(); i++) {
            if(!useList.contains(definitionList.get(i))) {
                System.out.printf("Warning: %s is defined in module %d but never used\n", definitionList.get(i), i);
            }/*
            if(!definitionList.contains(useList.get(i))) {
                System.out.printf("Warning: %s is used but not defined\n", useList.get(i));
            }*/
        }
        for(int i = 0; i < useList.size(); i++) {
            if(!definitionList.contains(useList.get(i))) {
                System.out.printf("Warning: %s is used in module but not defined\n", useList.get(i));
            }
        }
    }

    public static void secondPass(Scanner input) {

        int rows = 0; // # of lines in each file; 1 def, 2 use, 3 prog txt
        int columns; // # of things in each line

        String symbol;
        int word;
        int relativeAddress = 0; // relative address
        int absoluteAddress; // absolute address


        int moduleNumber = -1; //module # initialization
        int moduleLength = 0;

        List useSymbol = new ArrayList(); // list of use symbols
        List useAddress = new ArrayList(); // list of use addresses

        List programType = new ArrayList();
        List programWord = new ArrayList();

        List finalList = new ArrayList<>();
        int numUses = 0;


        while (input.hasNext()) {
            rows++;
            columns = input.nextInt();

            //check for beginning of new module
            if ((rows - 1) % 3 == 0) {
                moduleNumber++;
            }

            //System.out.printf("Module#: %d\n", moduleNumber);

            for (int i = 0; i < columns; i++) {
                symbol = input.next();
                word = input.nextInt();


                // use list
                if ((rows - 2) % 3 == 0) {
                    numUses = columns;
                    if (symbolTable.containsKey(symbol)) {
                        useSymbol.add(symbol);
                        useAddress.add(word);
                    } else {
                        useSymbol.add(symbol);
                        useAddress.add(word);
                        symbolTable.put(symbol, 0); //use zero if not defined
                        System.out.printf("Error: %s is used in module but not defined, using 0", symbol);
                    }
                }


                // program text
                if ((rows - 3) % 3 == 0) {
                    programType.add(symbol);
                    //System.out.printf("%s\n", symbol);
                    //System.out.printf("%4d\n", word);

                    if (symbol.equals("R")) {
                        //Relative, add base address
                        word = word + baseAddress.get(moduleNumber);
                        programWord.add(word);
                    } else {
                        programWord.add(word);
                    }

                    if (symbol.equals("E")) {
                        word = word - word%1000 + (int)(symbolTable.get(useSymbol.get(moduleNumber)));
                        programWord.add(word);
                    }
                    moduleLength++;
                }
            } // end of column
            /*
            if (rows % 3 == 0 && rows > 0) { // check end of module
                //driver(useAddress, programType, useSymbol, programWord, (int)useAddress.get(0));

                resolveExternalReference(useAddress, programType, useSymbol, programWord, (int)useAddress.get(0)); // (int)useAddress.get(0));
                useAddress.clear();
                useSymbol.clear();
                programType.clear();
                for(int i = 0; i < programWord.size(); i++) {
                    finalList.add(programWord.get(i));
                }
                programWord.clear();
            }*/
        }
        System.out.println();
        printList(programWord);
    }

/*
    public static List driver(List useAddress, List programType, List useSymbol, List programWord) {

        for(int i = 0; i < useAddress.size(); i++) {
            resolveExternalReference(List programType,)

    }*/

    public static List resolveExternalReference(List useAddress, List programType, List useSymbol, List programWord, int nextAddress) { //} int nextAddress) {
        //go to use list address(relative) in module
        //for(int i = 0; i < useAddress.size(); i++) { //traver use list 0
            //for (int j = 0; j < (int) useAddress.get(j); j++) { // traverse # of uses 0

            int i = 0;
            //while(j < (int)useAddress.size() ) {
                //System.out.println(j);


                //int address = (int)useAddress.get(i); //
                int address = nextAddress;
                System.out.println("current address: " + address);

                String type = (String) (programType.get(address));

                if (type.equals("E")) {

                    nextAddress = (int) (programWord.get(address)) % 1000;
                    System.out.println("next address: " + nextAddress);

                    //if next address is 777
                    if (nextAddress == 777) { //base case
                        System.out.println("End of linked list");


                        int updatedWord = (Integer) programWord.get(address) - (Integer) nextAddress + (Integer) symbolTable.get(useSymbol.get(i));
                        programWord.set(address, updatedWord);

                        printList(programWord);
                        i++;
                        int j = i;
                        nextAddress = (int) useAddress.get(j);

                        //return programWord; //may not be the last of the use has to go to next usesymbol
                        resolveExternalReference(useAddress, programType, useSymbol, programWord, nextAddress);

                    } else {
                        int updatedWord = (Integer) programWord.get(address) - (Integer) nextAddress + (Integer) symbolTable.get(useSymbol.get(i));
                        programWord.set(address, updatedWord);

                        printList(programWord);

                        return resolveExternalReference(useAddress, programType, useSymbol, programWord, nextAddress);
                    }


                } else { //use list address type is not E
                    System.err.println("Error: type is not E, treating as E");
                    nextAddress = (int) (programWord.get(address)) % 1000;
                    System.out.println("next " + nextAddress);

                    //if next address is 777
                    if (nextAddress == 777) { //base case
                        System.out.println("End of linked list");

                        //nextAddress = (int) useAddress.get(i);
                        int updatedWord = (Integer) programWord.get(address) - (Integer) nextAddress + (Integer) symbolTable.get(useSymbol.get(i));
                        programWord.set(address, updatedWord);


                        printList(programWord);
                        return programWord;
                        //resolveExternalReference(useAddress, programType, useSymbol, programWord, nextAddress);

                    } else {
                        int updatedWord = (Integer) programWord.get(address) - (Integer) nextAddress + (Integer) symbolTable.get(useSymbol.get(i));
                        programWord.set(address, updatedWord);

                        printList(programWord);

                        return resolveExternalReference(useAddress, programType, useSymbol, programWord, nextAddress);
                    }
                }



        return programWord;
    }



            /*
            for(int i = 0; )

            if ((rows - 3)%3 == 0) {
                resolveAddress(typeList, wordList);
            }*/

            //moduleNumber++;

    public static void printMap(Map<?,?> map) {
        //System.out.println("Symbol Table");
        for(Map.Entry<?,?> entry : map.entrySet()) {
            System.out.printf("%4s: %6d\n", entry.getKey(), entry.getValue());
        }
        /*
        for(int i = 0; i < list.size; i++) {
            System.out.printf("%4d  %d\n", i, list.get(i));
        }
        */
    }

    public static void printList(List<?> list) {
        //System.out.println("Memory Map");

        for(int i = 0; i < list.size(); i++) {
            System.out.printf("%4d: %6s\n", i, list.get(i));
        }
    }
    public static void print2DList(List<List> list) {
        //System.out.println("Memory Map");

        for(int i = 0; i < list.size(); i++) {
            for(int j = 0; i < (list.get(i)).size(); j++)
            System.out.printf("%4d: %6s\n", i, list.get(i).get(j));
        }
    }

    // function sorts map by value
    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new LinkedList<>(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
            @Override
            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
                return o1.getValue().compareTo(o2.getValue());
            }
        });
        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    public static void main(String[] args) {

        if(args.length > 1) {
            System.err.printf("Error: invalid number of argument. \n");
            System.exit(1);
        }

        File file = new File(args[0]);

        if(!file.canRead()) {
            System.err.printf("Error: cannot read file %s\n\n", args[0]);
            System.out.printf("Absolute path: %s\n", file.getAbsolutePath());
            System.exit(1);
        }

        Scanner in = null;
        try {
            in = new Scanner(file);
            firstPass(in);
            System.out.println("Symbol Table");
            printMap(sortByValue(symbolTable));
            in = new Scanner(file);
            System.out.print("Memory Map");
            secondPass(in);

        } catch (FileNotFoundException e) {
            System.err.printf("Error: cannot read file %s\n\n", args[0]);
            System.out.printf("Absolute path: %s\n", file.getAbsolutePath());
            System.exit(1);
        }

        //create file to read
        //File file = new File();
        //firstPass(in);





    }

    //in.close();
}
