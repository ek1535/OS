package ek1535_lab3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

/**
 * Created by Aedo on 4/6/16.
 */
public class Banker {
    ArrayList<Task> finishedList = new ArrayList<>(); //arrayList of finished tasks
    ArrayList<Task> deadList = new ArrayList<>();
    ArrayList<Task> taskList = new ArrayList<>();
    Task currentT;
    Activity currentA;


    public void banker(ArrayList<Task> taskList, int tasks, HashMap<Integer, Integer> rMap) {
        int cycle = 0;
        int nextCycle = 1;
        Integer avType; //resource type available
        Integer avUnit; //resource unit available
        int requestType; //resource type requested by current task
        int requestUnit; //resource unit requested by current task
        int releaseType; //resource type released by current task
        int releaseUnit; //resource unit released by current task
        int heldType; //resource type held by current task;
        int heldUnit; //resource unit held by current task
        int delay; //delay of current process
        int totalWaitTime = 0;
        ArrayList<Task> finishedList = new ArrayList<>(); //arrayList of finished tasks
        Queue<Task> taskQ;


        // while not all processes are finished
        while (finishedList.size() < tasks) {
            System.out.printf("During %d - %d\n", cycle, nextCycle);
            // for each task in taskList
            for (Task currenT : taskList) {
                Activity currentA = currenT.activityList.get(currenT.current);

                /** Initiate **/
                //ignore claim for optimistic manager
                if (currentA.name.equals("initiate")) {
                    System.out.printf("\tTask %d initiates\n", currenT.id);
                    currenT.current++; //increment activity counter for the task
                    currenT.next++; //increment next activity counter for the task
                }

            }
        }



    }

    public ArrayList<Task> initiate() {

    }

    public void printAvResources(HashMap<Integer, Integer> rMap, int nextCycle) {
        for(Map.Entry<Integer,Integer> entry : rMap.entrySet()) {
            Integer avType = entry.getKey();
            Integer avUnit = entry.getValue();
            if (avUnit == 0) {
                System.out.printf("Resource %2d is unavailable at %2d\n", avType, avUnit);
            } else {
                System.out.printf("%d units of resource %d is available at %2d\n", avUnit, avType, nextCycle);
            }
        }
    }
    public static void printTaskList(ArrayList<Task> array) {
        for (Task t : array) {
            t.print();
        }
    }

    public int totalTakenTime(ArrayList<Task> finishList) {
        int totalTakenTime = 0;
        for (Task t : finishList) {
            totalTakenTime += t.takenTime;
        }
        return totalTakenTime;
    }

    public int totalWaitTime(ArrayList<Task> finishList) {
        int totalWaitTime = 0;
        for (Task t : finishList) {
            totalWaitTime += t.waitTime;
        }
        return totalWaitTime;
    }

    public int pTotalWaitTime(ArrayList<Task> finishList) {
        int totalWaitTime = totalWaitTime(finishList);
        int totalTakenTime = totalTakenTime(finishList);
        double pTotalWaitTime = (double)totalWaitTime/(double)totalTakenTime;
        return ((int)(pTotalWaitTime*100));
    }

    public void printStats(ArrayList<Task> finishedList) {
        System.out.println("FIFO");
        for (Task t : finishedList) {
            t.printStats();
        }
        System.out.printf("Total  \t %d\t %d\t %2d%%\t\n", totalTakenTime(finishedList), totalWaitTime(finishedList), pTotalWaitTime(finishedList));
    }
}


