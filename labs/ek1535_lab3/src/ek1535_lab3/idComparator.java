package ek1535_lab3;

import java.util.Comparator;

/**
 * Created by Aedo on 4/8/16.
 * Comparator compares Activity object by task number from least to greatest
 */
public class idComparator implements Comparator<Activity> {
    @Override
    public int compare(Activity current, Activity next) {
        if (current.taskNumber < next.taskNumber) {
            return -1;
        } else if (current.taskNumber > next.taskNumber) {
            return 1;
        } else {
            return 0;
        }
    }
}
