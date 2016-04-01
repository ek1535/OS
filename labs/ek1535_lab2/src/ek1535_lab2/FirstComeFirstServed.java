package ek1535_lab2;

import com.apple.concurrent.Dispatch;

import java.util.*;

/**
 * Created by Aedo on 3/19/16.
 */
public class FirstComeFirstServed extends Scheduler {
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


    //returns list of the finished processes
    public Queue<Process> fcfs(ArrayList<Process> processList, ArrayList randNumList, boolean verbose) {
        int quantum = 1; //how much the cycle is incremented by
        int cycle = 0;
        int count = 0; //keeps track of random number list
        totalRunTime = 0;
        totalIOTime = 0;
        Process currentProcess; //keeps track of current process
        int numProcesses = processList.size();

        //id comparator to sort the print of terminatedQ
        Comparator<Process> idComparator = new InputTimeComparator();
        //tie break comparator to sort arrivalQ and blockedQ
        Comparator<Process> arrivalComparator = new ArrivalTimeComparator();

        //fifo queue stores running process
        Queue<Process> runningQ = new LinkedList<>();
        //fifo queue based on tie breaking rule just poll() to runningQ
            //doesnt have worry about ordering because processes come in sorted manner
        Queue<Process> readyQ = new LinkedList<>();
        //stores all blocked processes
        ArrayList<Process> blockedList = new ArrayList<>();

        //create a priority queue of processes based on arrival time and then input time
        //sorted(tie-break) queue of blocked processes w/ io left = 0
        PriorityQueue<Process> blockedQ = new PriorityQueue<>(arrivalComparator);
        //sorted queue by tie break
        PriorityQueue<Process> arrivalQ = new PriorityQueue<>(processList.size(), arrivalComparator);
        //stores terminated processes in order of arrival
        PriorityQueue<Process> terminatedQ = new PriorityQueue<>(idComparator);

        //PriorityQueue<Process> readyQ = new PriorityQueue<>(arrivalComparator); //queue stores all ready processes

        //create arrivalQ from process list
        for(int i = 0; i < processList.size(); i++) {
            arrivalQ.add(processList.get(i));
        }

        /** Print **/
        System.out.printf("The original input was: %2d ", processList.size());
        for (Process p : processList) {
            System.out.printf(p.toString());
        }
        System.out.println();

        Collections.sort(processList, arrivalComparator);
        System.out.printf("The (sorted) input is:  %2d ", processList.size());
        for (Process p : processList) {
            System.out.printf(p.toString());
        }
        System.out.println();
        /** Print **/



        //while not all process are terminated
        //arrival -> ready -> run _> blocked -> arrival_> ready
        while (terminatedQ.size() < numProcesses) { //running forever

            /**if verbose**/
            if(verbose) {
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
            }
            /**if verbose**/



            //if processes made it to ready Q this round and didn't run (some process still running)
                //wait time should be incremented concurrently
            //Placement?
            if (!readyQ.isEmpty()) {
                for (Process p : readyQ) {
                    p.waitTime++;
                }
            }

            /** doBlocked **/
            //blocked list is in unsorted matter
            //should be sorted when deciding how to add to readyQ
                //after each process has been updated, sort the queue and remove ready processes
            if (!blockedList.isEmpty()) {

                //each blocked process does i/o concurrently
                //insertion to readyQ must be prioritized first
                //update iotime and ioleft for each process
                //for (int i = blockedList.size()-1; i > -1; i-- ) {
                for (int i = 0; i < blockedList.size(); i++) {
                    currentProcess = blockedList.get(i);
                    currentProcess.ioTime++;
                    currentProcess.ioLeft--;
                    //check here?
                        //if removed from loop, messes up the ith index (use iterator?)
                }
                totalIOTime++;

                //sort io done processes by priority in blockedQ
                //find if any process have ioLeft <= 0 search true
                //then prioritize them
                /*
                for (int i = 0; i < blockedList.size(); i++) {
                    currentProcess = blockedList.get(i);
                    if (currentProcess.ioLeft <= 0) {

                    }
                }

                for (Process p : blockedList) {
                    if (p.ioLeft <= 0) {
                        currentProcess = blockedList.remove(p);
                        blockedQ.add(currentProcess); //blockedList.remove(p);
                    }
                }*/

                Iterator<Process> iterator = blockedList.iterator();
                while(iterator.hasNext()) {
                    currentProcess = iterator.next();
                    if (currentProcess.ioLeft <= 0) {
                        blockedQ.add(currentProcess); //add process to blockedQ
                        iterator.remove(); //remove process from list
                    }
                }
                //add sorted process from blockedQ to readyQ and them remove
                while(!blockedQ.isEmpty()) {
                    currentProcess = blockedQ.remove();
                    readyQ.add(currentProcess);
                    currentProcess.state = "ready";
                }
            } /** doBlocked **/

            /** doRunning **/
            if (!runningQ.isEmpty()) {
                currentProcess = runningQ.peek();

                currentProcess.cpuTime++;
                currentProcess.cpuLeft--;

                totalRunTime++;

                //check if total cpu is done -> terminated
                if (currentProcess.totalCPUTime <= currentProcess.cpuTime) {
                    terminatedQ.add(runningQ.poll());
                    currentProcess.state = "terminated";
                    currentProcess.finishTime = cycle;
                }
                //check if cpu burst is done -> blocked
                else if (currentProcess.cpuLeft <= 0) {
                    blockedList.add(runningQ.poll());
                    currentProcess.state = "blocked";

                    X = (int) randNumList.get(count);
                    count++;
                    //System.out.printf("Find I/O burst when blocking process: %d\n", X);
                    currentProcess.ioLeft = randomOS(currentProcess.ioBurst);
                } //else still running/continue to next cycle
            } /** doRunning **/

            /** doArriving **/
            //processes arrive simultaneously
            //traverse arrivalQ until all arrived process are in readyQ
            while (!arrivalQ.isEmpty() && arrivalQ.peek().arrivalTime <= cycle) {
                //for (Process s : arrivalQ) {
                //    System.out.printf(s.toString() + "\n");
                //}
                currentProcess = arrivalQ.remove();
                readyQ.add(currentProcess);
                currentProcess.state = "ready";
                //currentProcess = readyQ.peek();
                //currentProcess.state = "ready";
                //System.out.printf("\ncurrent process:" + currentProcess.toString() + "\n");
            } /** doArriving **/

            /** doReady **/
            //add ready queues to the running queue(no priority, by order)
            //readyQ not empty = runnable, runningQ emepty = no runs
            //send ready process to running
            if (!readyQ.isEmpty() && runningQ.isEmpty()) {
                //swich ready to running, only one runnable process at a time

                //System.out.println("readyQpeek" + readyQ.peek().toString());
                runningQ.add(readyQ.poll());
                currentProcess = runningQ.peek();
                currentProcess.state = "running";

                /** Set CPU burst for next cycle **/
                //only choose the cpu burst for next cycle, don't actually decrement/run
                X = (int)randNumList.get(count); //chose rand int x = 1
                count++;
                //System.out.printf("Find CPU burst when choosing ready process to run: %d\n", X);

                //check if cpu burst is less than total cpu
                //randomOS chooses the cpu burst from random numbers
                currentProcess.cpuLeft = randomOS(currentProcess.cpuBurst); //if greater than quantum?? (no preemption here)


                if (currentProcess.cpuLeft >= (currentProcess.totalCPUTime - currentProcess.cpuTime)) { //currentProcess.cpuLeft <= randomOS(X)) {
                    //if cpu burst greater than remaining than set to total
                    currentProcess.cpuLeft = (currentProcess.totalCPUTime - currentProcess.cpuTime); //randomOS(X) is chosen cpu burst
                    //count++;
                    // if cpu burst greater than quantum //preemption should go to ready not blocked
                } /*else if (randomOS(currentProcess.cpuBurst) > quantum) {
                    currentProcess.cpuLeft = quantum;
                    //needs to go to preemption(ready) instead of blocked
                } else {
                    currentProcess.cpuLeft = randomOS(currentProcess.cpuBurst);
                    count++;
                }*/
                /** CPU Burst **/
            } /** doReady **/

            cycle++;
        }

        System.out.println("\t\t\t\t\tFirst Come First Served");
        finishTime = cycle - 1;

        avgTurnaroundTime = avgTurnaroundTime(terminatedQ);
        avgWaitTime = avgWaitTime(terminatedQ);
        ioUtilization = ioUtil();
        cpuUtilization = cpuUtil(terminatedQ, totalRunTime);
        throughput = throughput(terminatedQ);
        //System.out.printf("%d", runTime);
        //this.printSummary vs printSummary(terminatedQ)

        for(Process s : processList) {
            s.printStats();
        }
        printSummary();
        return terminatedQ;
    }

    //start to finish
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

    //percentage of time job is running
    //given x cycles, how many cycles is running
    // (finish - io - wait) / finish
    public double cpuUtil(Queue<Process> terminatedQ, int runTime) {
        //float p = ioUtil(terminatedQ);
        float avgWaitTime = avgWaitTime(terminatedQ);
        float avgIOTime = avgIOTime(terminatedQ);
        float avgTurnaroundTime = avgTurnaroundTime(terminatedQ);
        int numProcesses = terminatedQ.size();
        //float avgNotRunningTime = avg
        //float p = (avgWaitTime + avgIOTime)/avgTurnaroundTime;
        //return (1 - (Math.pow(p, numProcesses)));
        float p = (float)this.totalRunTime/(float)this.finishTime;

        //System.out.printf("\n\n\n\n\nrunTime: %d\n finishTime: %d, p:%f", runTime, finishTime, p);

        return(p);
    }

    //percentage of time job is blocked
    //total io time / finish time
    //given we had x cycles, how many cycles did we have "at least" 1 blocked process
    public float ioUtil() {
        /*
        float totalIOTime = 0;
        for (Process p : terminatedQ) {
            totalIOTime += p.ioTime;
        }*/
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
        //float x = (avgTurnaroundTime + avgWaitTime)/terminatedQ.size();

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