package ek1535_lab3;

import java.util.*;

/**
 * Created by Aedo on 4/6/16.
 * Simulation of optimistic resource manager
 * Ignore initial claim
 * Satisfy request if possible, if not make task wait(block)
 * When release occurs, satisfy pending(blocked) requests in FIFO manner
 *
 */
public class OptimisticResourceManager {
    //int rType; //resource type to be added to resource map at end of cycle
    //int rUnit; //resource unit to be added to resource map at end of cycle
    //int avType; //resource type available from resource map
    //int avUnit; //resource unit available from resource map
    int updateType;
    int updateUnit;
    int tasks;
    ArrayList<Task> finishedList = new ArrayList<>(); //arrayList of finished tasks
    ArrayList<Task> deadList = new ArrayList<>();
    Task currentT;
    Activity currentA;

    public void fifo2(HashMap<Integer, Integer> rMap) {
        System.out.printf("\nINSIDE\n");
        rMap.put(3,2);
        printAvResources(rMap,0);
    }

    public void fifo(ArrayList<Task> taskList, int tasks, LinkedHashMap<Integer, Integer> rMap) {
        int cycle = 0;
        int nextCycle = 1;
        int delay; //delay of current process
        int avType;
        int avUnit;
        int requestType; //resource type requested by current task
        int requestUnit; //resource unit requested by current task
        int releaseType; //resource type released by current task
        int releaseUnit; //resource unit released by current task
        int heldType; //resource type held by current task;
        int heldUnit; //resource unit held by current task
        boolean release = false; //checks it hashmap is not null for iteration
        Map<Integer, Integer> releasedMap = new LinkedHashMap<>(); //Map stores released resources
        //Initialize releasedMap
        for(Map.Entry<Integer, Integer> entry : rMap.entrySet()) {
            releasedMap.put(entry.getKey(),0);
        }
        //Map store not granted resources Task already had hashmap property of .rRequest
        Map<Integer, Integer> requestMap = new HashMap<>();
        //Map<Integer, Integer> updateMap = new HashMap<>(); //Map store released resources to added at end of cycle

        /**
         * Deadlock Conditions
         * All 4 conditions must be present for deadlock to happen
         * 1) Mutual exclusion(Mutex) : each resource is currently assigned to exactly 1 task or is available
         * 2) Hold-and-wait : tasks currently holding resources that were granted earlier
         *                    can request new resources
         * 3) No-preemption : resources previously granted cannot be forcibly taken away from task
         *                    (must be explicitly released by task holding them)
         * 4) Circular wait : there must be circular list of 2 or more tasks,
         *                    each of which waiting for a resource held by the next member of the claim
         *
         * Do Deadlocks happen after cycle(after each task's activity is done) not during
         */

        boolean mutex;
        boolean haw;
        boolean nonpreemption;
        boolean circularWait;

        //processes currently holding
        boolean deadlock = false; //if == tasks, then deadlock


        Queue<Task> blockedQ = new LinkedList<>(); //blockedQ containing blocked tasks so they are done in fifo

        //HashMap of available resources, key:resourceType value:resourceUnit
        //HashMap<Integer, Integer> rMap = new HashMap<>();


        /** while not all processes are finished **/
        while (finishedList.size() < tasks) {
            System.out.printf("During %d - %d\n", cycle, nextCycle);

            /** Iterate taskList **/
            for (Task currenT : taskList) {
                //System.out.printf("Current activity counter: %d", currenT.counter);
                currentT = currenT;
                //currentA.print();
                //Current activity of task fetched by the task's activity counter
                currentA = currenT.activityList.get(currenT.current);
                //Activity nextA = currenT.activityList.get(currenT.next);
                Activity nextA;
                //currenT.delay = currentA.delay;


                /** Initiate **/
                //ignore claim for optimistic manager
                if (currentA.name.equals("initiate")) {
                    System.out.printf("\tTask %d initiates\n", currenT.id);
                    currenT.current++; //increment activity counter for the task
                    currenT.next++; //increment next activity counter for the task
                }

                /** check for delay **/
                //checks the next activity even if there is delay
                else if (currentA.delay > 0) {
                    //delay = currentA.delay;
                    //currenT.delay = delay;
                    currenT.delayStart = 1;
                    System.out.printf("\tTask %d delayed (%d of %d cycles)\n", currenT.id, currenT.delayStart, currentA.delay);
                    currenT.delayStart++;
                    currentA.delay--;
                } else {
                    /** Request **/
                    //can only request one type of resource at a time
                    if (currentA.name.equals("request") && !currenT.state.equals("aborted") && !currenT.state.equals("done")) {
                        //define what resource current task requests
                        requestType = currentA.resourceType;
                        requestUnit = currentA.unitRequested;
                        avUnit = rMap.get(requestType);

                        //update the tasks request map
                        //one type per task
                        currenT.rRequest.put(requestType, requestUnit);
                        currenT.requestType = requestType;
                        currenT.requestUnit = requestUnit;

                        //check if resources are available
                        if (requestUnit <= avUnit) { //available
                            rMap.put(requestType, avUnit - requestUnit); //update available resources

                            //update held resources, check if same type is already held
                            if (currenT.rHeld.get(requestType) != null && currenT.rHeld.get(requestType) > 0) {
                                heldUnit = currenT.rHeld.get(requestType); //held unit for type resulting in null
                                currenT.rHeld.put(requestType, heldUnit + requestUnit);
                                currenT.rRequest.put(requestType, 0);
                                currenT.requestUnit = 0;
                            } else { //not already held
                                currenT.rHeld.put(requestType, requestUnit);
                            }

                            System.out.printf("\tTask %d request granted (Holds: Type %d Unit %d)\n", currenT.id, requestType, requestUnit);
                            if (deadList.contains(currenT)) {
                                deadList.remove(currenT);
                            }
                            deadlock = false;
                            currenT.state = "ready";
                            currenT.current++;
                            currenT.next++;
                            /** check if task terminates
                            //task can only terminate after release activity
                            //does not require cycle
                            if (currenT.activityList.get(currenT.next) != null && currenT.activityList.get(currenT.next).name.equals("terminate")) {
                                nextA = currenT.activityList.get(currenT.next);
                                System.out.printf("\tTask %d terminates at %2d", currenT.id, nextCycle + nextA.delay);
                                currenT.takenTime = nextCycle + nextA.delay;
                                currenT.state = "done";
                                finishedList.add(currenT);
                            } **/
                            //resources not available
                        } else {
                            System.out.printf("\tTask %d request not granted (Request: %d \tAvailable: %d) (blocked)\n", currenT.id, requestUnit, avUnit);
                            //update request
                            //currenT.rRequest.put(requestType, requestUnit);
                            requestMap.put(requestType, requestUnit);
                            currenT.state = "blocked";
                            //deadlock++;
                            if (!deadList.contains(currenT)) {
                                deadList.add(currenT);
                            }
                            ++currenT.waitTime;
                        }
                    } /** End Request **/

                    /** Release **/
                    else if (currentA.name.equals("release") && !currenT.state.equals("aborted") && !currenT.state.equals("done")) {
                        //resource to be released
                        releaseType = currentA.resourceType;
                        releaseUnit = currentA.unitRequested;
                        //avUnit = rMap.get(rType); //av unit for type resource added at end of cycle

                        //check if there are enough resources to release
                        //if (currenT.rHeld.get(rType) != null || currenT.rHeld.get(rType) > 0) {
                        if (currenT.rHeld.get(releaseType) >= releaseUnit) {
                            heldUnit = currenT.rHeld.get(releaseType); //held unit for type
                            //update resources: available & held
                            currenT.rHeld.put(releaseType, heldUnit - releaseUnit);
                            //released
                            //hold resource value to be updated at end of cycle
                            //can have multiple releases per cycle
                            releasedMap.put(releaseType, releasedMap.get(releaseType) + releaseUnit);
                            release = true;
                            //not enough held resources to be releases
                        } else {
                            System.out.printf("\tTask %d doesn't have enough resources\n", currenT.id);
                            System.out.printf("\tRelease type: %d, unit: %d, held: %d\n", releaseType, releaseUnit, currenT.rHeld.get(releaseType));
                        }
                        //add to resource map at end of cycle
                        //rMap.put(releaseType, avUnit + releaseUnit);
                        /** check if task terminates **/
                        //does not require cycle
                        if (currenT.activityList.get(currenT.next) != null && currenT.activityList.get(currenT.next).name.equals("terminate")) {
                            nextA = currenT.activityList.get(currenT.next);
                            System.out.printf("\tTask %d releases Type %d, Unit %d (available at cycle %2d) and terminates at %2d\n", currenT.id, releaseType, releaseUnit, nextCycle, nextCycle + nextA.delay);
                            currenT.takenTime = nextCycle + nextA.delay;

                            currenT.state = "done";
                            finishedList.add(currenT);
                            currenT.current++;
                            currenT.next++;
                        } else {
                            System.out.printf("\tTask %d releases Type %d, Unit %d (available at cycle %2d)\n", currenT.id, releaseType, releaseUnit, nextCycle);
                            currenT.current++;
                            currenT.next++;
                        }
                    } /** End Release **/
                }
            } /** End Iterate taskList **/

            /** Detect Deadlock
             *  when all non-terminated tasks are blocked
             *  not granted request map > avResources
             *
             *  When Deadlocked:              *if deadlock detected at cycle n
             *  print message
             *  release resource from lowest numbered deadlocked task
             *  and abort that task at cycle n
             *  release resource availalbe at n+1
             *  if deadlock remains: print message , abort lowered numbered deadlocked task (recursive?)
             *
             *  Note:
             *  if deadlock (actually occurs) at cycle n
             *  might not detect it until much later
             *  since there may be non-deadlocked tasks running -> 2 deadlocked 1 release
             *
             *  if deadlock detected at cycle n
             *  abort deadlocked tasks
             *
             *
             **/

            /** Sort deadList to be in order by id **/
            Collections.sort(deadList, new taskComparator());

            System.out.printf("\n %s \n", release);
            /** Detect Deadlock **/
            //if deadlist == tasks and there were no releases in previous cycle
            if (deadList.size() == tasks - finishedList.size() && release == false && tasks-finishedList.size() != 1) {// && deadlock == true) { //deadList();
                System.out.printf("\nDeadlocked, size: %d\n", deadList.size());

                /*for (Task t : deadList) {
                    for (Map.Entry<Integer, Integer> entry : t.rRequest.entrySet()) {
                        requestType = entry.getKey();
                        requestUnit = entry.getValue();

                        //check for each type/unit
                        if (requestUnit > rMap.get(requestType)) {
                            t.state = "deadlocked";
                        }// else { //one resource may be availalbe other may not be
                    }
                }*/
                Iterator<Task> it = deadList.iterator(); //allows concurrent modification vs foreach
                while (it.hasNext()) {
                    Task t = it.next();
                    /** Detect Deadlock **/
                    //check if each resource type/unit are not available

                    //initialize updateMap if it doesn't contain key (NPE)
                    if (!releasedMap.containsKey(t.requestType)) {
                        releasedMap.put(t.requestType, 0);
                    }
                    if (t.requestUnit > rMap.get(t.requestType) + releasedMap.get(t.requestType)) {
                        //t.state = "deadlocked";
                        //deadList.add(t);
                        System.out.printf("\nDeadlocked\n");

                        /** Release lowest(current) deadlocked resource -> available next cycle **/
                        //Task currenTask = deadList.get(0);
                        for (Map.Entry<Integer, Integer> entry : t.rHeld.entrySet()) {
                            releaseType = entry.getKey();
                            releaseUnit = entry.getValue();
                            //System.out.printf("\nTask %d -> Release: Type %d, Unit %d\n", t.id, releaseType, releaseUnit);
                           // t.printMap();
                            //hasValue = ((Boolean)(updateMap.get(releaseType))).booleanValue();
                            if (releaseUnit > 0 && (Integer)releaseUnit != null) {
                                //System.out.println("\nUPDATE\n");
                                //released resource availalbe at n+1
                                //update update map && check for nullpointer
                                //if(updateMap.get(releaseType) != null && updateMap.get(releaseType) > 0 && updateMap.containsKey(releaseType) {
                                if (releasedMap.containsKey(releaseType)) {
                                    releasedMap.put(releaseType, releasedMap.get(releaseType) + releaseUnit);
                                } else {
                                    releasedMap.put(releaseType, releaseUnit);
                                }
                                updateType = releaseType;
                                updateUnit = releaseUnit; //updateMap.get(releaseType) + releaseUnit);
                                t.rHeld.put(releaseType, 0);
                                t.rRequest.put(releaseType, 0);
                                t.requestUnit = 0;
                                System.out.printf("Task %d aborted, releasing resources Type %d, Unit %d\n", t.id, updateType, updateUnit);
                            }
                        } /** Release done **/
                        /** Abort Task **/
                        t.state = "aborted";
                        finishedList.add(t);
                        //deadList.remove(t);
                    } else {
                        t.state = "ready";
                    }
                }
            }
            //deadLock(deadList);
            /** End Deadlock **/

            Collections.sort(taskList, new blockedComparator());
            //update rMap if there is release
            //can have 2 releases in same cycle: 1) from release 2) deadlock
            for (Map.Entry<Integer, Integer> entry : releasedMap.entrySet()) {
                updateType = entry.getKey();
                updateUnit = entry.getValue();
                rMap.put(updateType, rMap.get(updateType) + updateUnit);
                releasedMap.put(updateType, 0);
            }
            //releasedMap.clear();

            //print resources for next cycle
            //only need to print at end of cycle
            printAvResources(rMap, nextCycle);
            //printID(taskList);
            System.out.println();
            release = false;
            cycle++;
            nextCycle++;
            System.out.println();
        } /** while not all processes are finished **/
        Collections.sort(finishedList, new taskComparator());

        printStats(finishedList);
        }


    /**
     *
     *  METHODS
     *
     */


    /** Detect Deadlock
     *  when all non-terminated tasks have outstanding requests that manager cannot satisfy
     *  not granted request map > avResources
     *
     *  When Deadlocked:              *if deadlock detected at cycle n
     *  print message
     *  release resource from lowest numbered deadlocked task
     *  and abort that task at cycle n
     *  release resource availalbe at n+1
     *  if deadlock remains: print message , abort lowered numbered deadlocked task (recursive?)
     *
     *  Note:
     *  if deadlock (actually occurs) at cycle n
     *  might not detect it until much later
     *  since there may be non-deadlocked tasks running
     *
     *  if deadlock detected at cycle n
     *  abort deadlocked tasks
     *
     *
     **/
    public void detectDeadLock(ArrayList<Task> deadList, HashMap<Integer, Integer> updateMap, HashMap<Integer,Integer> rMap) {
        //incase release delay is later, then no deadlock CHEKC
        //need to fix deadlock value -> deadlocked when its not
        int releaseType, releaseUnit;
        /** Sort deadList to be in order by id **/
        Collections.sort(deadList, new taskComparator());

        /** Detect Deadlock **/
        for (Task current : deadList) {
            if (current.requestUnit > rMap.get(current.requestType)) {
                //t.state = "deadlocked";
                //deadList.add(t);
                System.out.printf("\nDeadlocked Again\n");
            }
        }
        /* Detect Deadlock
        for(Task t : deadList) {
            for (Map.Entry<Integer, Integer> entry : t.rRequest.entrySet()) {
                requestType = entry.getKey();
                requestUnit = entry.getValue();

                //check for each type/unit
                if (requestUnit > rMap.get(requestType)) {
                    t.state = "deadlocked";
                }// else { //one resource may be availalbe other may not be
            }
        }*/

        Iterator<Task> it = deadList.iterator(); //allows concurrent modification vs foreach
        while (it.hasNext()) {
            Task t = it.next();
            /** Detect Deadlock **/
            //check if each resource type/unit are not available
            for(Task task : deadList) {
                //for each Resource T held by task
                for (Map.Entry<Integer, Integer> entry : t.rRequest.entrySet()) {
                    int requestType = entry.getKey();
                    int requestUnit = entry.getValue();

                    //check for each type/unit
                    if (requestUnit > rMap.get(requestType)) {
                        t.state = "deadlocked";
                    }// else { //one resource may be availalbe other may not be
                }
            if (t.requestUnit > rMap.get(t.requestType)) {
                //t.state = "deadlocked";
                //deadList.add(t);
                System.out.printf("\nDeadlocked Again\n");

                /** Release lowest(current) deadlocked resource -> available next cycle **/
                //Task currenTask = deadList.get(0);
                t.printMap();

                for (Map.Entry<Integer, Integer> entry : t.rHeld.entrySet()) {
                    releaseType = entry.getKey();
                    releaseUnit = entry.getValue();
                    if (releaseUnit > 0) {
                        //released resource availalbe at n+1
                        updateMap.put(releaseType, updateMap.get(releaseType) + releaseUnit);
                        updateType = releaseType;
                        updateUnit = releaseUnit; //updateMap.get(releaseType) + releaseUnit);
                        t.rHeld.put(releaseType, 0);
                        t.rRequest.put(releaseType, 0);
                    }
                } /** Release done **/
                System.out.printf("Task %d aborted, releasing resources Type %d, Unit %d\n",t.id, updateType, updateUnit);
                /** Abort Task **/
                t.state = "aborted";
                finishedList.add(t);
                deadList.remove(t);
                } else {
                    t.state = "ready";
                }
            }

            //check if deadlock remains
            //deadlock(deadList);

        /*
            for(Map.Entry<Integer,Integer> entry : currenTask.rHeld.entrySet()) {
                releaseType = entry.getKey();
                releaseUnit = entry.getValue();
                if (releaseUnit > 0) {
                    //released resource availalbe at n+1
                    updateMap.put(releaseType, updateMap.get(releaseType) + releaseUnit);
                    updateType = releaseType;
                    updateUnit = releaseUnit; //updateMap.get(releaseType) + releaseUnit);
                    currenTask.rHeld.put(releaseType, 0);
                    currenTask.rRequest.put(releaseType, 0);
                }
            }
            //abort task
            currenTask.state = "aborted"; //terminated
            finishedList.add(currenTask);
            deadList.remove(currenTask);

            //chekck if deadlock remains
            deadlock(deadList);
        }*/


        }
    }


    public void printAvResources(HashMap<Integer, Integer> rMap, int nextCycle) {
        for(Map.Entry<Integer,Integer> entry : rMap.entrySet()) {
            Integer avType = entry.getKey();
            Integer avUnit = entry.getValue();
            if (!avType.equals("null")) { //empty key is initialized to 0;
                if(avUnit == 0) {
                    System.out.printf("Resource: Type %d is unavailable at %2d\n", avType, nextCycle);
                } else {
                    System.out.printf("Resource: Type %d, Unit %d is available at %2d\n", avType, avUnit, nextCycle);
                }
            }
        }
    }
    public static void printTaskList(ArrayList<Task> array) {
        for (Task t : array) {
            t.print();
        }
    }
    public static void printID(ArrayList<Task> array) {
        for(Task t : array) {
            System.out.printf("Task %d\t", t.id);
        }
    }
    public static void printMap(HashMap<Integer,Integer> map) {
        //System.out.println("Symbol Table");
        for(Map.Entry<?,?> entry : map.entrySet()) {
            System.out.printf("%4s: " + entry.getValue().toString() + "\n", entry.getKey());
        }
        /*
        for(int i = 0; i < list.size; i++) {
            System.out.printf("%4d  %d\n", i, list.get(i));
        }
        */
    }


    public int totalTakenTime(ArrayList<Task> finishList) {
        int totalTakenTime = 0;
        for (Task t : finishList) {
            totalTakenTime += t.takenTime;
        }
        return totalTakenTime;
    }

    public int totalWaitTime(ArrayList<Task> finishList) {
        int totalWaitTime = 0;
        for (Task t : finishList) {
            if (!t.state.equals("aborted")) {
                totalWaitTime += t.waitTime;
            }
        }
        return totalWaitTime;
    }

    public int pTotalWaitTime(ArrayList<Task> finishList) {
        int totalWaitTime = totalWaitTime(finishList);
        int totalTakenTime = totalTakenTime(finishList);
        double pTotalWaitTime = (double)totalWaitTime/(double)totalTakenTime;
        //round to nearest integer
        pTotalWaitTime = Math.round(pTotalWaitTime*100);
        return ((int)(pTotalWaitTime));
    }

    public void printStats(ArrayList<Task> finishedList) {
        System.out.println("\t\tFIFO");
        for (Task t : finishedList) {
            t.printStats();
        }
        System.out.printf("Total  \t %d\t %d\t %2d%%\t\n", totalTakenTime(finishedList), totalWaitTime(finishedList), pTotalWaitTime(finishedList));
    }
}
