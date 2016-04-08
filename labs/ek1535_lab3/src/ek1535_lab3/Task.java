package ek1535_lab3;

import java.util.*;

/**
 * Created by Aedo on 4/6/16.
 * Each task has arbitrary number of activities
 */
public class Task {
    int id;
    int[] resourceTypeHeld;
    int[] resourceNumHeld;
    int resourceRequest;
    ArrayList<Activity> activityList;
    Activity activity1;
    Activity activity2;
    Activity activity3;
    Activity activity4;
    Activity activity5;
    Activity activity6;

    public Task(int id, ArrayList<Activity> activityList) {
        this.id = id;
        this.activityList = activityList;
    }

    // 1 activity
    public Task(Activity a) {
        this.activity1 = a;
    }
    // 2 activities
    public Task(Activity a, Activity b) {
        this.activity1 = a;
        this.activity2 = b;
    }
    // 3
    public Task(Activity a, Activity b, Activity c) {
        this.activity1 = a;
        this.activity2 = b;
        this.activity3 = c;
    }
    // 4
    public Task(Activity a, Activity b, Activity c, Activity d) {
        this.activity1 = a;
        this.activity2 = b;
        this.activity3 = c;
        this.activity4 = d;
    }
    // 5
    public Task(Activity a, Activity b, Activity c, Activity d, Activity e) {
        this.activity1 = a;
        this.activity2 = b;
        this.activity3 = c;
        this.activity4 = d;
        this.activity5 = e;
    }
    // 6
    public Task(Activity a, Activity b, Activity c, Activity d, Activity e, Activity f) {
        this.activity1 = a;
        this.activity2 = b;
        this.activity3 = c;
        this.activity4 = d;
        this.activity5 = e;
        this.activity6 = f;
    }

    public void addActivity(Activity a) {
        this.activityList.add(a);
    }

    public void print() {
        for (Activity a : this.activityList) {
            System.out.printf(a.toString());
        }
    }
    public String toString() {
        Activity current = new Activity();
        for (Activity a : this.activityList) {
            current = a;
        }
        return ("Task: " + current.toString());
    }
}
