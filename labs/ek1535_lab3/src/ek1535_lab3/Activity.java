package ek1535_lab3;

import java.util.HashMap;

/**
 * Created by Aedo on 4/6/16.
 */
public class Activity {
    String name;
    int taskNumber;
    int delay; //# of cycles btw completion of previous activity and start of current activity
    int resourceType;
    int initialClaim; //maximum number of units of each resource the process can "possibly" need
    //HashMap<Integer, Integer> initialClaim;
    int unitRequested;
    int delayStart;

    public Activity(){}

    public Activity(String name, int taskNumber, int delay, int resourceType, int unitRequested) {
        this.name = name;
        this.taskNumber = taskNumber;
        this.delay = delay;
        this.resourceType = resourceType;
        //this.initialClaim = new HashMap<>();
        this.delayStart = 0;

        if (this.name.equals("initiate")) {
            //this.initialClaim = unitRequested;
            this.initialClaim = unitRequested; //put(resourceType, unitRequested);
        } else if (this.name.equals("request") || this.name.equals("release")) {
            this.unitRequested = unitRequested;
        }
        //this.unitRequested = unitRequested;
    }

    /**
     * Constructor deep clones Activity object passsed in
     * creates new object instead of pointing a reference to original object
     * WORKS
     * @param a
     */
    public Activity(Activity a){
        this.name = a.name;
        this.taskNumber = a.taskNumber;
        this.delay = a.delay;
        this.resourceType = a.resourceType;
        this.delayStart = 0;
        //this.initialClaim = new HashMap<>();

        if (this.name.equals("initiate")) {
            //this.initialClaim = a.unitRequested;
            this.initialClaim = a.initialClaim; //put(a.resourceType, a.unitRequested);
        } else if (this.name.equals("request") || this.name.equals("release")) {
            this.unitRequested = a.unitRequested;
        }
    }

    @Override
    public String toString() {
        return (this.name + " " +  this.taskNumber + " " + this.delay + " " + this.resourceType + " " + this.unitRequested + "\n");
    }
    public void print() {
        System.out.printf("%10s %2d %2d %2d %2d \n", this.name, this.taskNumber, this.delay, this.resourceType, this.unitRequested);
    }

}
