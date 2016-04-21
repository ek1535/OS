package ek1535_lab3;

import java.util.*;

/**
 * Created by Aedo on 4/6/16.
 */
public class Banker {
    ArrayList<Task> finishedList = new ArrayList<>(); //stores all finished tasks
    ArrayList<Task> deadList = new ArrayList<>();
    ArrayList<Task> taskList = new ArrayList<>();
    LinkedHashMap<Integer, Integer> rMap = new LinkedHashMap<>(); //stores all availalbe resources at cycle
    Map<Integer, Integer> releasedMap = new LinkedHashMap<>(); //stores all released resources at cycle
    Map<Integer, Integer> requestMap = new LinkedHashMap<>(); //stores all requested resources at cycle
    Task currentT;
    Activity currentA;
    Activity nextA;
    int cycle;
    int nextCycle;
    int updateType;
    int updateUnit;


    public ArrayList<Task> banker(ArrayList<Task> taskList, int tasks, LinkedHashMap<Integer, Integer> resourceMap) {
        cycle = 0;
        nextCycle = 1;

        Integer avType; //resource type available
        Integer avUnit; //resource unit available
        int requestType; //resource type requested by current task
        int requestUnit; //resource unit requested by current task
        int releaseType; //resource type released by current task
        int releaseUnit; //resource unit released by current task
        int heldType; //resource type held by current task;
        int heldUnit; //resource unit held by current task
        int delay; //delay of current process
        int totalWaitTime = 0;

        rMap = resourceMap;

        for (Map.Entry<Integer, Integer> entry : rMap.entrySet()) {
            releasedMap.put(entry.getKey(), 0);
        }
        //ArrayList<Task> finishedList = new ArrayList<>(); //arrayList of finished tasks
        Queue<Task> taskQ;



        // while not all processes are finished
        while (finishedList.size() < tasks) {
            System.out.printf("During %d - %d\n", cycle, nextCycle);
            /** Foreach task in taskList */
            for (Task currenT : taskList) {
                currentT = currenT;
                currentA = currentT.activityList.get(currentT.current);

                /** Check if not terminated */
                if (!currentT.state.equals("done") && !currenT.state.equals("aborted")) {

                    if (!currentA.name.equals("terminate")) {
                        nextA = currentT.activityList.get(currentT.next);
                    }
                    /** Initiate **/
                    //ignore claim for optimistic manager
                    if (currentA.name.equals("initiate")) {
                        initiate();
                    } /** End Initiate */

                    /** Check for delay **/
                    else if (currentA.delayStart < currentA.delay) {
                        delay();
                    } else { /** No delay **/

                        /** Request **/
                        if (currentA.name.equals("request")) {
                            request();
                        } /** End Request */

                        /** Release */
                        else if (currentA.name.equals("release")) {
                            release();
                        } /** End Release */
                    }
                } /** End check not terminated */
            } /** End foreach Task */

            Collections.sort(taskList, new blockedComparator());

            for (Map.Entry<Integer, Integer> entry : releasedMap.entrySet()) {
                updateType = entry.getKey();
                updateUnit = entry.getValue();
                rMap.put(updateType, rMap.get(updateType) + updateUnit);
                releasedMap.put(updateType, 0);
            }

            printAvResources(rMap, nextCycle);
            System.out.println();
            cycle++;
            nextCycle++;
            System.out.println();
        } // while not all processes are finished

        Collections.sort(finishedList, new taskComparator());
        return finishedList;
        //printStats(finishedList);
    }


    /**
     *  Initiate activity for Task
     *  if there are N resource types there are N initiate activities for:
     *      each Task, each requiring 1 cycle
     *  if initial claim exceeds available resources:
     *      abort task & release all resources
     *
     */
    public void initiate() {
        int avType;
        int avUnit;
        int releaseType;
        int releaseUnit;
        int initType;
        int initUnit;

        // initialize current tasks held resources to 0; NPE
        for (Map.Entry<Integer, Integer> avEntry : rMap.entrySet()) {
            avType = avEntry.getKey();
            avUnit = avEntry.getValue();
            currentT.rHeld.put(avType, 0);
        }

        initType = currentA.resourceType;
        initUnit = currentA.initialClaim;

        avUnit = rMap.get(initType);
        currentT.initialClaim.put(initType, initUnit);

        /** Check if initial claim exceeds available resources **/
        if (currentT.initialClaim.get(initType) > avUnit) { /** exceeds */
            System.out.printf("\tTask %d aborted, claim %d exceeds resources %d\n", currentT.id, currentT.initialClaim.get(initType), avUnit);
            /** Release */
            for (Map.Entry<Integer, Integer> entry : currentT.rHeld.entrySet()) {
                releaseType = entry.getKey();
                releaseUnit = entry.getValue();
                //check if resources can be released
                if (releaseUnit > 0) {
                    //update releasedMap
                    updateType = releaseType;
                    updateUnit = releaseUnit;
                    //releasedMap.put(releaseType, releasedMap.get(releaseType) + releaseUnit);
                    currentT.rHeld.put(releaseType, 0);
                    currentT.rRequest.put(releaseType, 0);
                    currentT.requestUnit = 0;
                    System.out.printf("Task %d aborted, releasing resources Type %d, Unit %d\n", currentT.id, updateType, updateUnit);
                }
            } /** Release done */
            /** Abort Task */
            currentT.state = "aborted";
            finishedList.add(currentT);
        } else { /** not exceeds */
            System.out.printf("\tTask %d initiates w/ claim %d\n", currentT.id, initUnit);
            currentT.current++; //increment activity counter for the task
            currentT.next++; //increment next activity counter for the task
            //cycle++;
            //nextCycle++;
        }
    }

    /**
     * Delay
     *
     */
    public void delay() {
        int delay = currentA.delay;
        //currentT.delayStart = 0;
        currentA.delayStart++;
        //check if task terminates
        if (currentA.name.equals("terminate") && (currentA.delay - currentA.delayStart) == 0) {
            System.out.printf("\tTask %d delayed (%d of %d cycles) and terminates at %d\n", currentT.id, currentA.delayStart, delay, nextCycle);
            currentT.takenTime = nextCycle;
            currentT.state = "done";
            finishedList.add(currentT);
        } else {
            System.out.printf("\tTask %d delayed (%d of %d cycles)\n", currentT.id, currentA.delayStart, delay);
        }
        //currentA.delay--;
    }

    /**
     * CHECK SAFE UNSAFE
     */
    public void request() {

        int requestType = currentA.resourceType;
        int requestUnit = currentA.unitRequested;
        int avUnit = rMap.get(requestType);
        int initialClaim = currentT.initialClaim.get(requestType);
        //System.out.printf("\n rType: %d, rUnit: %d, Claim: %d\n", requestType, requestUnit, initialClaim);
        int heldUnit;

        currentT.rRequest.put(requestType, requestUnit);
        currentT.requestType = requestType;
        currentT.requestUnit = requestUnit;

        /** Check if initial claim exceeds available units (can have multiple claims) */
        if (rMap.size() > 1) {
            for (Map.Entry<Integer, Integer> entry : rMap.entrySet()) {
                int avType = entry.getKey();
                int availableUnit = entry.getValue();
                if (availableUnit + currentT.rHeld.get(avType) < currentT.initialClaim.get(avType)) { // initial claim exceeds
                    currentT.state = "exceeds";
//                System.out.printf("\tTask %d request not granted (Available: %d \tClaim: %d) (unsafe, blocked)\n", currentT.id, requestUnit, initialClaim);
//                currentT.state = "blocked";
//                ++currentT.waitTime;
                    break;
                }
            }
        } else if (avUnit + currentT.rHeld.get(requestType) < currentT.initialClaim.get(requestType)) { // releasedMap.get(requestType)
            currentT.state = "exceeds";
        }

        /** Check if request exceeds initial claim */
        if ( (requestUnit + currentT.rHeld.get(requestType)) <= initialClaim) {
            /** Check if initial claim exceeds available units (can have multiple claims) */
            if (!currentT.state.equals("exceeds")) {
                /** Check if request exceeds available units */
                if (requestUnit <= avUnit) { /** available */
                    rMap.put(requestType, avUnit - requestUnit);
                    //check if resource is already held by task
                    if (currentT.rHeld.get(requestType) > 0) { //held
                        //update resources held by task
                        heldUnit = currentT.rHeld.get(requestType);
                        currentT.rHeld.put(requestType, heldUnit + requestUnit);
                    } else { //not held
                        //update resources held by task
                        currentT.rHeld.put(requestType, requestUnit);
                    }
                    //update resources requested by task
                    currentT.requestUnit = 0;
                    currentT.rRequest.put(requestType, 0);
                    System.out.printf("\tTask %d request granted (Holds: Type %d Unit %d)\n", currentT.id, requestType, requestUnit);
                /*
                if (deadList.contains(currentT)) {
                    deadList.remove(currentT);
                }
                */
                    currentT.state = "granted";
                    currentT.current++;
                    currentT.next++;
                } else { /** not available */
                    System.out.printf("\tTask %d request not granted (Request: %d \tAvailable: %d) (blocked)\n", currentT.id, requestUnit, avUnit);
                    requestMap.put(requestType, requestUnit);
                    currentT.state = "blocked";
                /*
                if (!deadList.contains(currentT)) {
                    deadList.add(currentT);
                }*/
                    ++currentT.waitTime;
                }
            }  else { /** initial claim exceeds available units */
                System.out.printf("\tTask %d request not granted (Available: %d \tClaim: %d) (unsafe, blocked)\n", currentT.id, avUnit, initialClaim);
                currentT.state = "blocked";
                ++currentT.waitTime;
            }
        } else { /** exceeds initial claim */
            //System.out.printf("\tTask %d request not granted (Request: %d \tClaim: %d) (unsafe, blocked)\n", currentT.id, requestUnit, initialClaim);
            //++currentT.waitTime;
            for (Map.Entry<Integer, Integer> entry : currentT.rHeld.entrySet()) {
                int releaseType = entry.getKey();
                int releaseUnit = entry.getValue();
                if (releaseUnit > 0) {
                    //update releasedMap
                    updateType = releaseType;
                    updateUnit = releaseUnit;
                    //releasedMap.put(releaseType, releasedMap.get(releaseType) + releaseUnit);
                    currentT.rHeld.put(releaseType, 0);
                    currentT.rRequest.put(releaseType, 0);
                    currentT.requestUnit = 0;
                    releasedMap.put(updateType, releasedMap.get(updateType) + updateUnit);
                    System.out.printf("Task %d aborted, releasing resources Type %d, Unit %d\n", currentT.id, updateType, updateUnit);
                }
            } /** Release done */
            /** Abort Task */
            currentT.state = "aborted";
            finishedList.add(currentT);
        }
    }

    /**
     *
     */
    public void release() {
        int releaseType = currentA.resourceType;
        int releaseUnit = currentA.unitRequested;
        int heldUnit = currentT.rHeld.get(releaseType);

        /** Check if there are enough resources to release */
        if (heldUnit >= releaseUnit) {
            //update resources: available & held
            currentT.rHeld.put(releaseType, heldUnit - releaseUnit);
            releasedMap.put(releaseType, releasedMap.get(releaseType) + releaseUnit);
            /** Check if task terminates next cycle */
            if (nextA.name.equals("terminate") && nextA.delay == 0) { //terminated
                System.out.printf("\tTask %d releases Type %d, Unit %d (available at cycle %2d) and terminates at %2d\n", currentT.id, releaseType, releaseUnit, nextCycle, nextCycle + nextA.delay);
                currentT.takenTime = nextCycle + nextA.delay;
                currentT.state = "done";
                finishedList.add(currentT);
            } else { //not terminated
                System.out.printf("\tTask %d releases Type %d, Unit %d (available at cycle %2d)\n", currentT.id, releaseType, releaseUnit, nextCycle);
                currentT.current++;
                currentT.next++;
            }
        } else {
            System.out.printf("\tTask %d doesn't have enough resources\n", currentT.id);
            System.out.printf("\tRelease type: %d, unit: %d, held: %d\n", releaseType, releaseUnit, currentT.rHeld.get(releaseType));
        }
    }


    public void printAvResources(HashMap<Integer, Integer> rMap, int nextCycle) {
        for(Map.Entry<Integer,Integer> entry : rMap.entrySet()) {
            Integer avType = entry.getKey();
            Integer avUnit = entry.getValue();
            if (avUnit == 0) {
                System.out.printf("Resource %2d is unavailable at %2d\n", avType, avUnit);
            } else {
                System.out.printf("%d units of resource %d is available at %2d\n", avUnit, avType, nextCycle);
            }
        }
    }
    public static void printTaskList(ArrayList<Task> array) {
        for (Task t : array) {
            t.print();
        }
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
        System.out.println("\t\tBanker");
        for (Task t : finishedList) {
            t.printStats();
        }
        System.out.printf("Total  \t %d\t %d\t %2d%%\t\n", totalTakenTime(finishedList), totalWaitTime(finishedList), pTotalWaitTime(finishedList));
    }
}


