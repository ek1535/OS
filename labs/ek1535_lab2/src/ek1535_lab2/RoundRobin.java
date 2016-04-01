package ek1535_lab2;
import java.util.*;
/**
 * Created by Aedo on 3/28/16.
 */
public class RoundRobin extends Scheduler {
    int CPUBurst;
    int IOBurst;
    int IOTime;

    private int X;

    private int finishTime;
    private float avgTurnaroundTime;
    private float avgWaitTime;
    private float ioUtilization;
    private double cpuUtilization;
    private float throughput;

    private int totalIOTime; //incremented every cycle when at least one process is blocked
    private int totalRunTime; //incremented every cycle when at least process is running

    public Queue<Process> rr(ArrayList<Process> processList, ArrayList randNumList) {
        int quantum = 2;
        int cycle = 0;
        int count = 0;
        totalRunTime = 0;
        totalIOTime = 0;
        Process currentProcess;
        int numProcess = processList.size();

        Comparator<Process> idComparator = new ArrivalTimeComparator();
        Comparator<Process> arrivalComparator = new ArrivalTimeComparator();
        Comparator<Process> tieBreakComparator = new tieBreakComparator();

        Queue<Process> runningQ = new LinkedList<>();
        Queue<Process> readyQ = new LinkedList<>();
        ArrayList<Process> readyList = new ArrayList<>();

        ArrayList<Process> blockedList = new ArrayList<>();
        //PriorityQueue<Process> readyQ = new PriorityQueue<>(arrivalComparator);
        PriorityQueue<Process> blockedQ = new PriorityQueue<>(arrivalComparator);
        PriorityQueue<Process> arrivalQ = new PriorityQueue<>(processList.size(), arrivalComparator);
        PriorityQueue<Process> terminatedQ = new PriorityQueue<>(idComparator);


        boolean preemption = false;
        boolean blocked = false;


        for(int i = 0; i < processList.size(); i++) {
            currentProcess = processList.get(i);
            arrivalQ.add(currentProcess);
        }


        /** Print **/
        System.out.printf("The original input was: %2d ", processList.size());
        for (Process p : processList) {
            System.out.printf(p.toString());
        }
        System.out.println();

        Collections.sort(processList, arrivalComparator);
        for (int i = 0; i < processList.size(); i++) {
            currentProcess = processList.get(i);
            currentProcess.id = i;
        }
        System.out.printf("The (sorted) input is:  %2d ", processList.size());
        for (Process p : processList) {
            System.out.printf(p.toString());
        }
        System.out.println();
        /** Print **/

        while (terminatedQ.size() < numProcess) {
            //quantum = 2;

            /**if verbose**/
            System.out.printf("Before cycle\t %2d:", cycle);
            for (Process s : processList) {
                if (s.state == "unstarted" || s.state == "terminated" || s.state == "ready") {
                    System.out.printf("\t\t\tProcess %s: %10s %2d", s.id, s.state, 0);
                } else if (s.state == "running") {
                    //For RR: quantum instead of s.cpuLeft
                    System.out.printf("\t\t\tProcess %s: %10s %2d", s.id, s.state, s.cpuLeft);
                } else if (s.state == "blocked") {
                    System.out.printf("\t\t\tProcess %s: %10s %2d", s.id, s.state, s.ioLeft);
                }
            }
            System.out.println();
            /**if verbose**/

            if (!readyList.isEmpty())
            {
                for (Process p : readyList) {
                    p.waitTime++;
                }
                //readList p.waitTime++;
            }

            /** doBlocked **/
            if (!blockedList.isEmpty())
            {
                for (int i = 0; i < blockedList.size(); i++) {
                    currentProcess = blockedList.get(i);
                    currentProcess.ioTime++;
                    currentProcess.ioLeft--;
                }
                totalIOTime++;

                /** tie break **/
                Iterator<Process> blockedIterator = blockedList.iterator();
                while(blockedIterator.hasNext()) {
                    currentProcess = blockedIterator.next();
                    if (currentProcess.ioLeft <= 0) {
                        blockedQ.add(currentProcess); //add process to blockedQ
                        blockedIterator.remove(); //remove process from list
                    }
                }

                //blockedQ is a priorityQ based on arrival time
                while(!blockedQ.isEmpty()) {
                    currentProcess = blockedQ.remove();
                    readyList.add(currentProcess); //add to back of readyQ
                    currentProcess.state = "ready";
                    currentProcess.inputTime = cycle;
                    blocked = true;
                }
                /** tie break **/

            } /** doneBlocked **/

            /** doRunning **/
            // can: 1)terminate 2)preempt 3)block
            if (!runningQ.isEmpty())
            {
                currentProcess = runningQ.peek();

                currentProcess.cpuTime++;
                currentProcess.cpuLeft--;
                quantum--;
                totalRunTime++;

                //check if total cpu is done -> terminated
                if (currentProcess.totalCPUTime <= currentProcess.cpuTime) {
                    terminatedQ.add(runningQ.poll());
                    currentProcess.state = "terminated";
                    currentProcess.finishTime = cycle;
                    quantum = 2; //reset quantum when process terminates
                }
                //check if cpu burst is done -> blocked
                else if (currentProcess.cpuLeft <= 0) {
                    blockedList.add(runningQ.poll());
                    currentProcess.state = "blocked";
                    X = (int) randNumList.get(count);
                    count++;
                    System.out.printf("Find I/O burst when blocking process: %d\n", X);
                    currentProcess.ioLeft = randomOS(currentProcess.ioBurst);
                    quantum = 2; //reset quantum when process cpu burst done
                }
                //preempted(goes to ready or could go to terminated)
                else if (quantum == 0) { //quantum == 0 cycle%2 quantum == 0 && cycle != 0
                    readyList.add(runningQ.poll()); //add to preempted list to add to back of ready list
                    currentProcess.state = "ready";
                    currentProcess.inputTime = cycle;
                    preemption = true;
                    quantum = 2; //reset quantum when quantum done
                }
                //else still running/continue to next cycle
            } /** doneRunning **/

            /** doArriving **/
            while (!arrivalQ.isEmpty() && arrivalQ.peek().arrivalTime <= cycle)
            {
                currentProcess = arrivalQ.remove();
                readyList.add(currentProcess);
                currentProcess.state = "ready";
            } /** doneArriving **/



            /** tie break **/
            if (blocked == true && preemption == true ){
                Collections.sort(readyList, tieBreakComparator);
                blocked = false;
                preemption = false;
            } /** tie break **/

             /** doReady **/
            if (!readyList.isEmpty() && runningQ.isEmpty()) {

                /** run next process **/
                Iterator<Process> readyIterator = readyList.iterator();
                /*
                while(readyIterator.hasNext()) {
                    currentProcess = readyIterator.next();
                    runningQ.add(currentProcess);
                    readyIterator.remove();
                }*/
                //when run -> ready(preempted) && block -> ready
                //will choose block all the time
                runningQ.add(readyList.remove(0));
                currentProcess = runningQ.peek();
                currentProcess.state = "running";


                /** tie break
                //could bring preempted process to top
                Collections.sort(readyList, arrivalComparator);

                for (int i = 0; i < readyList.size(); i++) {
                    currentProcess = readyList.get(i);
                    readyQ.add(currentProcess);
                }
                readyList.removeAll(readyQ);
                /*

                }
                /** tie break **/


                /** Set CPU burst for next cycle **/
                //only choose the cpu burst for next cycle, don't actually decrement/run
                //preempted processes should maintain burst
                //check if preempted process
                //Process w/ cpu burst done -> choose new cpu burst
                if (currentProcess.cpuLeft <= 0) //preempted process cpuLeft > 0
                {
                    X = (int) randNumList.get(count);
                    count++;
                    System.out.printf("Find CPU burst when choosing ready process to run: %d\n", X);
                    currentProcess.cpuLeft = randomOS(currentProcess.cpuBurst);

                    //if cpu burst greater than remaining than set to total
                    if (randomOS(currentProcess.cpuBurst) > (currentProcess.totalCPUTime - currentProcess.cpuTime)) {
                        currentProcess.cpuLeft = (currentProcess.totalCPUTime - currentProcess.cpuTime);
                        //check if cpuBurst can terminate within quantum
                        if (currentProcess.cpuLeft < quantum) {
                            quantum = currentProcess.cpuLeft;
                        }

                    // if cpu burst greater than quantum //preemption should go to ready not blocked
                    } else if (currentProcess.cpuLeft < quantum) {
                        quantum = currentProcess.cpuLeft;
                        //currentProcess.cpuLeft = quantum;
                        //needs to go to preemption(ready) instead of blocked
                    }
                }

            } /** doneReady **/
            preemption = false;
            blocked = false;
            cycle++;
        }

        System.out.println("Round Robin");
        finishTime = cycle - 1;
        avgTurnaroundTime = avgTurnaroundTime(terminatedQ);
        avgWaitTime = avgWaitTime(terminatedQ);
        ioUtilization = ioUtil(terminatedQ);
        cpuUtilization = cpuUtil(terminatedQ, totalRunTime);
        throughput = throughput(terminatedQ);

        for(Process s : processList) {
            s.printStats();
        }
        printSummary();
        return terminatedQ;
    }



    public float avgTurnaroundTime(Queue<Process> terminatedQ) {
        float totalTurnaroundTime = 0;

        for(Process p : terminatedQ) {
            totalTurnaroundTime += p.getTurnaroundTime();
        }
        return (totalTurnaroundTime/terminatedQ.size());
    }

    public float avgWaitTime(Queue<Process> terminatedQ) {
        float totalWaitTime = 0;
        for(Process p : terminatedQ) {
            totalWaitTime += p.waitTime;
        }
        return (totalWaitTime/terminatedQ.size());
    }

    public double cpuUtil(Queue<Process> terminatedQ, int runTime) {
        float p = (float)runTime/(float)this.finishTime;

        return(p);
    }

    public float ioUtil(Queue<Process> terminatedQ) {

        return ((float)this.totalIOTime/(float)this.finishTime);
    }

    public float avgIOTime(Queue<Process> terminatedQ) {
        float totalIOTime = 0;
        for(Process p : terminatedQ) {
            totalIOTime += p.ioTime;
        }
        return (totalIOTime/terminatedQ.size());
    }

    //number of jobs completed every 100 cycles
    public float throughput(Queue<Process> terminatedQ) {
        float x = (float)terminatedQ.size()/(float)finishTime;

        return x * 100;
    }

    public void printSummary() {
        System.out.printf("Summary Data:\n");
        System.out.printf("\t\tFinishing time: %d\n", this.finishTime);
        System.out.printf("\t\tCPU Utilization: %f\n", this.cpuUtilization);
        System.out.printf("\t\tI/O Utilization: %f\n", this.ioUtilization);
        System.out.printf("\t\tThroughput: %f process per hundred cycles\n", this.throughput);
        System.out.printf("\t\tAverage turnaround time: %f\n", this.avgTurnaroundTime);
        System.out.printf("\t\tAverage waiting time: %f\n", this.avgWaitTime);
        System.out.println();
    }

    public int randomOS(int U) {
        return 1 + (X%U);
    }

}
