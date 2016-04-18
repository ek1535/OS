package ek1535_lab3;

import java.util.Comparator;

/**
 * Created by Aedo on 4/15/16.
 */
public class taskComparator implements Comparator<Task> {

    @Override
    public int compare(Task current, Task next) {
        if (current.id < next.id) {
            return -1;
        } else if (current.id > next.id) {
            return 1;
        } else {
            return 0;
        }
    }
}
