package ek1535_lab2;

import java.util.Comparator;

/**
 * Created by Aedo on 4/1/16.
 */
public class InputTimeComparator implements Comparator<Process> {
    @Override
    public int compare(Process currentProcess, Process nextProcess) {
        if (currentProcess.id < nextProcess.id) {
            return -1;
        } else if (currentProcess.id > nextProcess.id) {
            return 1;
        } else { //if arrival time is equal, choose priority by input time
            return 0;
        }
    }
}
