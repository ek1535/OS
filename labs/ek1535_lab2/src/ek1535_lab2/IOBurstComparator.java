package ek1535_lab2;

import java.util.Comparator;

/**
 * Created by Aedo on 3/29/16.
 */
public class IOBurstComparator implements Comparator<Process> {
    @Override
    public int compare(Process currentProcess, Process nextProcess) {
        //return currentProcess.getArrivalTime() < nextProcess.getArrivalTime() ? -1:1;

        if (currentProcess.cpuLeft < nextProcess.cpuLeft) {
            return -1;
        } else if (currentProcess.cpuLeft > nextProcess.cpuLeft) {
            return 1;
        } else { //if arrival time is equal, choose priority by input time
            if (currentProcess.id < nextProcess.id) {
                return -1;
            } else if (currentProcess.id > nextProcess.id) {
                return 1;
            } else { //never the case here
                return 0;
            }
        }

    }
}
