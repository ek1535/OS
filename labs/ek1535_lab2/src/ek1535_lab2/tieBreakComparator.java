package ek1535_lab2;

import java.util.Comparator;

/**
 * Created by Aedo on 3/19/16.
 * Tie-breaking rule for lab 2
 * Sort by arrival time and then by input time
 */

public class tieBreakComparator implements Comparator<Process> {
    @Override
    public int compare(Process currentProcess, Process nextProcess) {
        //return currentProcess.getArrivalTime() < nextProcess.getArrivalTime() ? -1:1;

        if (currentProcess.inputTime < nextProcess.inputTime) {
            return -1;
        } else if (currentProcess.inputTime > nextProcess.inputTime) {
            return 1;
        } else {
            if (currentProcess.id < nextProcess.id) {
                return -1;
            } else if (currentProcess.id > nextProcess.id) {
                return 1;
            } else { //if arrival time is equal, choose priority by input time
                return 0;
            }//if arrival time is equal, choose priority by input time

        }

    }
}