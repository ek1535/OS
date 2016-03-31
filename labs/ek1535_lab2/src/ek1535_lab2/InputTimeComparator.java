package ek1535_lab2;

import java.util.Comparator;

/**
 * Created by Aedo on 3/28/16.
 */
public class InputTimeComparator implements Comparator<Process> {
    @Override
    public int compare(Process currentProcess, Process nextProcess) {
        //return currentProcess.getArrivalTime() < nextProcess.getArrivalTime() ? -1:1;

        if (currentProcess.id < nextProcess.id) {
            return -1;
        }
        if (currentProcess.id > nextProcess.id) {
            return 1;
        }
        return 0;
    }
}
