package ek1535_lab2;
/**
 * Created by Aedo on 3/17/16.
 */

import java.lang.*;
import java.util.*;
import java.io.*;

public class ProcessScheduling {

    /**
     * main method for scheduling
     *
     * @param args
     */
    //Process p;
    private HashMap<Integer, Process> processHashMap;
    private ArrayList<Process> processList;
    public static ArrayList<Integer> randNumList;
    private static boolean verbose;

    public static void main(String[] args) {
        //Store random numbers in array
        String currentDir = System.getProperty("user.dir");
        //file is in current directory
        File numFile = new File("random-numbers.txt");
        randNumList = readNumFile(numFile);
        //printArray(randNumList);

        /****************************************************/

        if (args.length == 0) {
            System.err.printf("Error: invalid number of argument. \n");
            System.exit(1);
        }

        //HashMap<Integer, Process> processHashMap;
        ArrayList<Process> processList;
        if (args.length == 2) {
            File file = new File(args[1]);
            if (!file.canRead()) {
                System.err.printf("Error: cannot read file %s\n\n", args[0]);
                System.out.printf("Absolute path: %s\n", file.getAbsolutePath());
                System.exit(1);
            }
            processList = readFile(file);
            verbose = true;
        } else {
            File file = new File(args[0]);
            if (!file.canRead()) {
                System.err.printf("Error: cannot read file %s\n\n", args[0]);
                System.out.printf("Absolute path: %s\n", file.getAbsolutePath());
                System.exit(1);
            }
            processList =  readFile(file);
            verbose = false;
        }
        //printHashMap(processHashMap);

        ArrayList<Process> fcfsList = cloneList(processList);
        ArrayList<Process> rrList = cloneList(processList);
        ArrayList<Process> lcfsList = cloneList(processList);
        ArrayList<Process> hprnList = cloneList(processList);

        //first come first served returns new queue after scheduling
        FirstComeFirstServed fcfs = new FirstComeFirstServed();
        Queue<Process> doneFCFS = fcfs.fcfs(fcfsList, randNumList, verbose);


        RoundRobin rr = new RoundRobin();
        Queue<Process> doneRR = rr.rr(rrList, randNumList, verbose);

        LastComeFirstServed lcfs = new LastComeFirstServed();
        lcfs.lcfs(lcfsList, randNumList, verbose);

        HighestPenaltyRatioNext hprn = new HighestPenaltyRatioNext();
        hprn.hprn(hprnList, randNumList, verbose);




        /* SHALLOW COPY/CLONE reference to same object, need to clone object itself
        //Collections.copy(fcfsList, processList);
        //for(Process p : processList) {
        //    fcfsList.add(p);
        //}
        //ArrayList<Process> fcfsList = (ArrayList<Process>)processList.clone();
        //ArrayList<Process> fcfsList = new ArrayList<Process>(processList);

        //for(int i = 0; i < processList.size(); i++) {
          //  rrList.add(processList.get(i));
        //

        //priority queue is in order of arrival time and input time


        //for(Process s : arrivalQfcfs)
          //  System.out.printf("\t" + s.toString());
          */


    }


    /*
    /* reads and stores random-numbers.txt into array list
     */
    public static ArrayList<Integer> readNumFile(File numFile) {
        ArrayList<Integer> randNumList = new ArrayList<>();
        Scanner input = null;
        try {
            input = new Scanner(numFile);
            while (input.hasNext()) {
                int line = input.nextInt();
                randNumList.add(line);
            }
        } catch (FileNotFoundException e) {
            System.err.printf("Error: cannot read file %s\n\n", numFile);
            System.out.printf("Absolute path: %s\n", numFile.getAbsolutePath());
            System.exit(1);
        }
        return randNumList;
    }

    /*
    ** reads and stores processes into Array List
    * cannot be referenced from static, need to put in main instead of method
     */
    public static ArrayList<Process> readFile(File file) {
        //linked hashmap iterates in order of entries submitted vs hashmap doesnt
        //HashMap<Integer, Process> processHashMap = new HashMap<>();
        ArrayList<Process> processList = new ArrayList<>();
        try {
            Scanner input = new Scanner(file);
            //traverse the file
            while (input.hasNextInt()) {

                //first number tells the # of processes
                int numProcesses = input.nextInt();
                //create Process and add to list
                for (int i = 0; i < numProcesses; i++) {
                    int arrivalTime = input.nextInt();
                    int cpuBurst = input.nextInt();
                    int totalCPUTime = input.nextInt();
                    int ioBurst = input.nextInt();
                    processList.add(new Process(i, arrivalTime, cpuBurst, totalCPUTime, ioBurst));
                    //processHashMap.put(i, new Process(arrivalTime, cpuBurst, totalCPUTime, ioBurst));
                }
            }
        } catch (FileNotFoundException e) {
            System.err.printf("Error: cannot read file %s\n\n", file);
            System.out.printf("Absolute path: %s\n", file.getAbsolutePath());
            System.exit(1);
        }
        return processList;
    }

    public static void printArray(ArrayList<Process> list) {
        System.out.printf("The original input was: %2d\t", list.size());
        for (int i = 0; i < list.size(); i++) {
            System.out.printf(list.get(i).toString());
            //list.get(i).toString();
        }
        System.out.println();
    }

    public static void printHashMap(HashMap<Integer, Process> processHashMap) {
        System.out.printf("The original input was: ");
        for (Map.Entry<Integer, Process> entry : processHashMap.entrySet()) {
            System.out.printf(entry.getKey() + " : " + entry.getValue());
        }
        System.out.println();
    }

    //put everything in main here and run for each scheduling algorithm
    public static void main() {

    }

    public static ArrayList<Process> cloneList(ArrayList<Process> ogList) {
        ArrayList<Process> clonedList = new ArrayList<Process>(ogList.size());
        for(Process p : ogList) {
            clonedList.add(new Process(p));
        }
        return clonedList;
    }


}
