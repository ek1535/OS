package ek1535_lab3;

import java.io.*;
import java.lang.reflect.Array;
import java.util.*;

/**
 * Created by Aedo on 4/6/16.
 *
 */
public class ResourceAllocation {
    /**
     * History of a Resource is sequence of:
     * 1) Request
     * 2) Allocate
     * 3) Use
     * 4) Release
     */
    static int tasks; //# of tasks
    static int resourceTypes; //# of resource types
    static int resourceUnits; //# of resource units for each type
    //HashMap stores available resources, key:resourceType value:resourceUnit
    //static HashMap<Integer, Integer> rMap = new HashMap<>();
    static LinkedHashMap<Integer,Integer> rMap = new LinkedHashMap<>();
    // list of tasks(object) ordered by task number, each task object is an array(?) of activities
    static ArrayList<Task> taskList = new ArrayList<>();
    // list of activity(object) ordered by input(All activities)
        //put into each Task object in TaskList
    static ArrayList<Activity> activityList = new ArrayList<>();

    static ArrayList<Activity> fifoAList; //activityList deep cloned for fifo algorithm
    static ArrayList<Activity> bankerAList; //activityList deep cloned for banker algorithm
    static ArrayList<Task> fifoTaskList = new ArrayList<>();
    static ArrayList<Task> bankerTaskList = new ArrayList<>();
    //static Map taskMap = new HashMap<String, ArrayList<Activity>>();

    public static void main(String[] args) {
        Activity currentActivity;
        Task currentTask;
        //ArrayList<Task> fifoTaskList = new ArrayList<>(); //deep cloned TaskList for Optimisitic Resource Manager
        //ArrayList<Task> bankerTaskList = new ArrayList<>(); //deep cloned TaskList for Banker
        //taskList = new ArrayList<>();

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
            //activityList now contains all activities object in order
            /*activityList = */readFile(file);
        }


        /** Sort activity list by task number w/ idComparator **/
        Comparator<Activity> idComparator = new idComparator();
        Collections.sort(activityList, idComparator);

        /** Add Activity in activityList to correct(by task number) Task(each Task's array) in taskList **/
        for (int i = 0; i < taskList.size(); i++) {
            currentTask = taskList.get(i);
            for (int j = 0; j < activityList.size(); j++) {
                currentActivity = activityList.get(j);
                if (currentActivity.taskNumber == currentTask.id) {
                    currentTask.addActivity(currentActivity);
                    //if(currentActivity.initialClaim )
                }
            }
        }
        //printTaskList(taskList);
        //printArray(activityList);

        /** Deep Clone Activity List, Task List and Resource Map for each algorithm: 2 Step **/

        /** 1) deep clone(create new object) activityList first (stores all activities regardless of Task) **/
        fifoAList = cloneAList(activityList);
        bankerAList = cloneAList(activityList);
        /** now all activities are in each activity list for algorithm **/

        /** 2) clone taskList (then put each activity into Task object(ArrayList of activities object) **/
        /** 2a) initiailize each algorithms task list**/
        for (int i = 1; i < tasks+1; i++) {
            fifoTaskList.add(new Task(i));
            bankerTaskList.add(new Task(i));
        }

        /** 2b) add Activity in activityList to correct(by task number) Task(each Task's array) in each algorithm's task list **/
        for (int i = 0; i < fifoTaskList.size(); i++) {
            currentTask = fifoTaskList.get(i);
            for (int j = 0; j < fifoAList.size(); j++) {
                currentActivity = fifoAList.get(j);
                if (currentActivity.taskNumber == currentTask.id) {
                    currentTask.addActivity(currentActivity);
                }
            }
        }
        for (int i = 0; i < bankerTaskList.size(); i++) {
            currentTask = bankerTaskList.get(i);
            for (int j = 0; j < bankerAList.size(); j++) {
                currentActivity = bankerAList.get(j);
                if (currentActivity.taskNumber == currentTask.id) {
                    currentTask.addActivity(currentActivity);
                }
            }
        }
        /** Cloning done **/
        OptimisticResourceManager orm = new OptimisticResourceManager();
        ArrayList<Task> fifoDone = orm.fifo(fifoTaskList, tasks,rMap);

        Banker banker = new Banker();
        ArrayList<Task> bankerDone = banker.banker(bankerTaskList, tasks, rMap);
        orm.printStats(fifoDone);
        banker.printStats(bankerDone);

    }
    /** Main DONE**/

    /**
     *         METHODS
     *
     */

    public static void printAList(ArrayList<Activity> array) {
        for(Activity a : array) {
            a.print();
        }
    }

    public static void printTaskList(ArrayList<Task> array) {
        for (Task t : array) {
            t.print();
            //System.out.printf("Task %d\n", t.id);
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
    public static void printMap(LinkedHashMap<?,?> map) {
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
    public static void printAvResources(LinkedHashMap<Integer, Integer> rMap) {
        for(Map.Entry<Integer,Integer> entry : rMap.entrySet()) {
            Integer avType = entry.getKey();
            Integer avUnit = entry.getValue();
            System.out.printf("avType: %d, avUnit: %d\n", avType, avUnit);
            if (!avType.equals("null")) { //empty key is initialized to 0;
                if(avUnit == 0) {
                    System.out.printf("Resource: Type %d is unavailable\n", avType);
                } else {
                    System.out.printf("Resource: Type %d Unit %d is available\n", avType, avUnit);
                }
            }
        }
    }

    public static void readFile(File file) {
        //ArrayList<Activity> activityList = new ArrayList();
        try {
            Scanner input = new Scanner(file);
            //store # of task, resourceTypes, resourceUnits
            String firstLine = input.nextLine();
            String tokens[] = firstLine.split(" ");

            tasks = Integer.parseInt(tokens[0]);

            /** Initialize taskList w/ # of tasks **/
            for (int i = 1; i < tasks+1; i++) {
                taskList.add(new Task(i));
            }

            resourceTypes = Integer.parseInt(tokens[1]);

            /** Initialize rMap w/ resources **/
            if (resourceTypes > 1) { // multiple resource types
                for (int i = 2; i < tokens.length; i++) {
                    // resource units for current type
                    resourceUnits = Integer.parseInt(tokens[i]);
                    rMap.put(i-1, resourceUnits);
                }
            } else { // only 1 resource type
                resourceUnits = Integer.parseInt(tokens[2]);
                rMap.put(1, resourceUnits);
            }

            resourceUnits = Integer.parseInt(tokens[2]);

            /** Initialize activityList **/
            while (input.hasNext()) {
                String activity = input.next();
                int taskNumber = input.nextInt();
                int delay = input.nextInt();
                int resourceType = input.nextInt();
                int request = input.nextInt();
                //create an activity object for each activity
                    //and store each activity object in array list by input order
                activityList.add(new Activity(activity, taskNumber, delay, resourceType, request));
            }
        } catch (FileNotFoundException e) {
            System.err.printf("Error: cannot read file %s\n\n", file);
            System.out.printf("Absolute path: %s\n", file.getAbsolutePath());
            System.exit(1);
        }
        //return activityList;
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
                    //taskMap.put(i, new Task(i, activityList));
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

    /**
     * Method deep clones from an ArrayList
     * @param ogAList
     * @return clonedList
     */
    public static ArrayList<Activity> cloneAList(ArrayList<Activity> ogAList) {
        ArrayList<Activity> clonedList = new ArrayList<>(ogAList.size());
        for (Activity a : ogAList) {
            clonedList.add(new Activity((a)));
        }
        return  clonedList;
    }

    /**
     * Method deep clones TaskList
     * doesnt work when used as method works
     * @param ogTaskList
     * @param ogAList
     * @return
     */
    public static ArrayList<Task> cloneTList(ArrayList<Task> ogTaskList, ArrayList<Activity> ogAList) {


        ArrayList<Task> clonedList = new ArrayList<>(ogTaskList.size());
        for (Task task : ogTaskList) {
            for (Activity activity : ogAList) {
                if (activity.taskNumber == task.id) {
                    task.addActivity(activity);
                }
            }
        }
        return  clonedList;
        /**
        ArrayList<Task> clonedList = new ArrayList<>(ogTaskList.size());
        Task currentTask;
        Activity currentActivity;
        for (int i = 0; i < ogTaskList.size(); i++) {
            currentTask = ogTaskList.get(i);
            for (int j = 0; j < ogAList.size(); j++) {
                currentActivity = ogAList.get(j);
                if (currentActivity.taskNumber == currentTask.id) {
                    currentTask.addActivity((currentActivity));
                }
            }
        }
         return clonedList;**/

    }

}
