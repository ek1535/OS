package ek1535_lab3;

import java.util.*;

/**
 * Created by Aedo on 4/6/16.
 */
public class OptimisticResourceManager {


    public void fifo(ArrayList<Task> taskList, int resourceType, int resourceUnit) {
        int cycle = 0;
        int nextCycle = 1;
        Resource resource1 = new Resource(resourceUnit); //resource available

        for (Task t : taskList) {
            Activity current = t.activityList.get(cycle);

            if(current.name.equals("initiate")) {
                if(resource1.unit > current.initialClaim) { //initialclaim!!
                    System.out.printf("During %2d-%2d each task completes its initiate", cycle, nextCycle);
                }
            }
            else if(current.name.equals("request")) {
                if (resource1.unit > current.numberRequested) {
                    System.out.printf("Task %2d's completes its request\n", t.id);
                }

            }
            else if (current.name.equals("release")) {

            }
        }
        taskList.get(cycle).activityList.get(cycle);
    }
}
