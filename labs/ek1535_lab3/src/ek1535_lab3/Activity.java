package ek1535_lab3;

/**
 * Created by Aedo on 4/6/16.
 */
public class Activity {
    String name;
    int taskNumber;
    int delay;
    int resourceType;
    int initialClaim;
    int numberRequested;

    public Activity(){}

    public Activity(String name, int taskNumber, int delay, int resourceType, int initialClaim) {
        this.name = name;
        this.taskNumber = taskNumber;
        this.delay = delay;
        this.resourceType = resourceType;
        /*
        if (activity == "initiate") {
            this.initialClaim = initialClaim;
        } else if (activity == "request" || activity == "release") {
            this.numberRequested = initialClaim;
        }*/
        this.numberRequested = initialClaim;
    }

    @Override
    public String toString() {
        return (this.name + " " +  this.taskNumber + " " + this.delay + " " + this.resourceType + " " + this.numberRequested + "\n");
    }
    public void print() {
        System.out.printf("%10s %2d %2d %2d %2d \n", this.name, this.taskNumber, this.delay, this.resourceType, this.numberRequested);
    }

}
