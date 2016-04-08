package ek1535_lab3;

import java.io.*;
import java.util.*;

/**
 * Created by Aedo on 4/6/16.
 *
 */
public class ResourceAllocation {
    static int tasks;
    static int resourceTypes;
    static int resourceUnits;

    // list of tasks, each task object is an array(?) of activities
    static ArrayList<Task> taskList;
    //
    static ArrayList<Activity> activityList;
    static Map taskMap = new HashMap<String, ArrayList<Activity>>();

    public static void main(String[] args) {


        /****************************************************/
        //ArrayList taskList = new ArrayList();

        if (args.length == 0) {
            System.err.printf("Error: invalid number of argument. \n");
            System.exit(1);
        } else if (args.length > 0) {
            File file = new File(args[0]);
            if (!file.canRead()) {
                System.err.printf("Error: cannot read file %s\n\n", args[0]);
                System.out.printf("Absolute path: %s\n", file.getAbsolutePath());
                System.exit(1);
            }
            taskList = readFile(file);
        }
        /** Sort taskList by task number **/
        for (int i = 0; i < taskList.size(); i++) {
            int currentTaskNum = taskList.get(i).
        }
        printArray(taskList);
        //System.out.print(taskList.size());
        //printMap(taskMap);

    }

    public static void printArray(ArrayList<Task> array) {
        for(Task t : array) {
            t.print();
        }
    }
    public static void print2DArray(ArrayList<ArrayList<Activity>> array) {

        for (ArrayList<Activity> list : array) {
            //System.out.println(Arrays.deepToString(list.toArray()));
            for (Activity a : list) {
                System.out.printf(a.toString());
            }
        }

        /*
        for (int i = 0; i < array.size(); i++) {
            for (int j = 0; j < array.get(i).size(); j++) {
                System.out.printf(array.get(i).get(j).toString());
            }
        }*/
    }
    public static void printMap(Map<?,?> map) {
        //System.out.println("Symbol Table");
        for(Map.Entry<?,?> entry : map.entrySet()) {
            System.out.printf("%4s: " + entry.getValue().toString() + "\n", entry.getKey());
        }
        /*
        for(int i = 0; i < list.size; i++) {
            System.out.printf("%4d  %d\n", i, list.get(i));
        }
        */
    }

    public static ArrayList readFile(File file) {
        ArrayList<Activity> activityList = new ArrayList();
        try {
            Scanner input = new Scanner(file);
            tasks = input.nextInt();
            resourceTypes = input.nextInt();
            resourceUnits = input.nextInt();

            while (input.hasNext()) {
                String activity = input.next();
                int taskNumber = input.nextInt();
                int delay = input.nextInt();
                int resourceType = input.nextInt();
                int request = input.nextInt();
                //System.out.printf("request: " + request + "\n");
                activityList.add(new Activity(activity, taskNumber, delay, resourceType, request));
            }
        } catch (FileNotFoundException e) {
            System.err.printf("Error: cannot read file %s\n\n", file);
            System.out.printf("Absolute path: %s\n", file.getAbsolutePath());
            System.exit(1);
        }
        return activityList;
    }

    public static ArrayList readFile2(File file) {
        ArrayList<Activity> activityList = new ArrayList<>();
        ArrayList<Task> taskList = new ArrayList<>();
        try {
            Scanner input = new Scanner(file);
            tasks = input.nextInt();
            resourceTypes = input.nextInt();
            resourceUnits = input.nextInt();
            int i = 1;
            boolean newTask = true;
            //arbitrary number of activities for each task
            //store activities for current task
            while (input.hasNext()) { // && input.next() != "initiate" || newTask == true) {
                //read next line
                String activity = input.next();
                int taskNumber = input.nextInt();
                int delay = input.nextInt();
                int resourceType = input.nextInt();
                int request = input.nextInt();
                //store Activity
                activityList.add(new Activity(activity, taskNumber, delay, resourceType, request));
                if (activity.equals("terminate")) {
                    taskList.add(new Task(i, activityList));
                    taskMap.put(i, new Task(i, activityList));
                    activityList.clear(); //this clears task and taskList (reference)
                    i++;
                }
            }
        } catch (FileNotFoundException e) {
            System.err.printf("Error: cannot read file %s\n\n", file);
            System.out.printf("Absolute path: %s\n", file.getAbsolutePath());
            System.exit(1);
        } catch (InputMismatchException e) {
            System.out.print(e.getMessage());
        }
        return taskList;
    }
}
