package ek1535_lab3;

import java.util.*;

/**
 * Created by Aedo on 4/6/16.
 * Each task object stores an arrayList of its activity
 * Each task has arbitrary number of activities object
 *
 */
public class Task {
    int id; //task number
    String state; //state of task (i.e. blocked)
    int delay;
    int delayStart;
    int current; //counter for activity
    int next; //counter for next activity
    int requestType; //request resource type for task (can only have one)
    int requestUnit;

    //Resource resourceRequest; //resource type and unit requested by task
    ArrayList<Activity> activityList; //arrayList of activities for each task
    //ArrayList<Resource> resourceHeld; //arrayList of resources held
    HashMap<Integer, Integer> rHeld; //hashmap of resources held
    HashMap<Integer, Integer> rRequest; //hashmap of resources requested


    int takenTime; //time taken to complete task
    int waitTime; //time spent on waiting



    public Task(int id) {
        this.id = id;
        this.takenTime = 0;
        this.waitTime = 0;
        this.delayStart = 0;
        this.activityList = new ArrayList<>();
        this.rHeld = new HashMap<>();
        this.rRequest = new HashMap<>();
        this.current = 0;
        this.next = 1;
        this.state = "";
        this.requestType = 1;
        this.requestUnit = 0;
    }
    public Task(int id, ArrayList<Activity> activityList) {
        this.id = id;
        this.takenTime = 0;
        this.waitTime = 0;
        this.delayStart = 0;
        this.activityList = activityList;
        this.rHeld = new HashMap<>();
        this.rRequest = new HashMap<>();
        this.current = 0;
        this.next = 1;
        this.state = "";
    }

    public Task(Task t) {
        this.id = t.id;
        this.takenTime = 0;
        this.waitTime = 0;
        this.delayStart = 0;
        this.activityList = new ArrayList<>(); //copy
        this.rHeld = new HashMap<>();
        this.rRequest = new HashMap<>();
        this.current = 0;
        this.next = 1;
        this.state = "";
    }

    public void addActivity(Activity a) {
        this.activityList.add(a);
    }

    //percentage of time spent waiting
    public int pWaitTime() {
        double pWaitTime = (double)this.waitTime/(double)this.takenTime;
        pWaitTime = Math.round(pWaitTime*100);
        return ((int)(pWaitTime));
    }

    public void print() {
        System.out.printf("Task %d: \n", this.id);
        for (Activity a : this.activityList) {
            a.print();
        }
        System.out.println();
    }
    public String toString() {
        Activity current = new Activity();
        for (Activity a : this.activityList) {
            current = a;
        }
        return ("Task: " + current.toString());
    }

    public void printStats() {
        if (this.state.equals("aborted")) {
            System.out.printf("Task %d\t %s\t\n", this.id, this.state);
        } else {
            System.out.printf("Task %d\t %d\t %d\t %2d%%\t\n", this.id, this.takenTime, this.waitTime, this.pWaitTime());
        }
    }
    public void printMap() {
        System.out.printf("\nTask %d Held Map\n", this.id);
        for(Map.Entry<Integer, Integer> entry : this.rHeld.entrySet()) {
            System.out.printf("Type: " + entry.getKey() + " Unit: " + entry.getValue() +"\n");
        }
        /*
        for(int i = 0; i < list.size; i++) {
            System.out.printf("%4d  %d\n", i, list.get(i));
        }
        */
    }

}
