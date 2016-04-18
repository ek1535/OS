package ek1535_lab3;

import java.util.Comparator;

/**
 * Created by Aedo on 4/13/16.
 * Comparator compare Task object by state from blocked being first and then by id
 */
public class blockedComparator implements Comparator<Task> {
    @Override
    public int compare(Task current, Task next) {
        if(current.state.equals("blocked") && !next.state.equals("blocked")) {
            return -1;
        } else if(!current.state.equals("blocked") && next.state.equals("blocked")) {
            return 1;
        } else if (current.state.equals("blocked") && next.state.equals("blocked")) { //all blocked
            return 0;
            /*

            }*/
        } else { //no blocked state
            if (current.id < next.id) {
                return -1;
            } else if (current.id > next.id) {
                return 1;
            } else {
                return 0;
            }
        }
    }
}
