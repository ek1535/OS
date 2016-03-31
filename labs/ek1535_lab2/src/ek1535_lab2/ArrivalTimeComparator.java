package ek1535_lab2;

import java.util.Comparator;

/**
 * Created by Aedo on 3/19/16.
 * Tie-breaking rule for lab 2
 * Sort by arrival time and then by input time
 */

public class ArrivalTimeComparator implements Comparator<Process> {
        @Override
        public int compare(Process currentProcess, Process nextProcess) {
            //return currentProcess.getArrivalTime() < nextProcess.getArrivalTime() ? -1:1;

            if (currentProcess.arrivalTime < nextProcess.arrivalTime) {
                return -1;
            } else if (currentProcess.arrivalTime > nextProcess.arrivalTime) {
                return 1;
            } else { //if arrival time is equal, choose priority by input time
                if (currentProcess.id < nextProcess.id) {
                    return -1;
                } else if (currentProcess.id > nextProcess.id){
                    return 1;
                } else { //never the case here
                    return 0;
                }
            }

    }
}

