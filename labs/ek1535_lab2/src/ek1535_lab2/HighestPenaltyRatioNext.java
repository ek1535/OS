package ek1535_lab2;

import java.util.*;

/**
 * Created by Aedo on 4/1/16.
 */
public class HighestPenaltyRatioNext extends Scheduler {

    private int X;

    private int finishTime;
    private float avgTurnaroundTime;
    private float avgWaitTime;
    private float ioUtilization;
    private double cpuUtilization;
    private float throughput;

    private int totalIOTime;
    private int totalRunTime;

    public Queue<Process> hprn(ArrayList<Process> processList, ArrayList randNumList, boolean verbose) {
        int quantum = 1;
        int cycle = 0;
        int count = 0;
        totalRunTime = 0;
        totalIOTime = 0;
        Process currentProcess;
        int numProcesses = processList.size();

        Comparator<Process> arrivalComparator = new ArrivalTimeComparator();
        Comparator<Process> idComparator = new InputTimeComparator();
        Comparator<Process> tieBreakComparator = new tieBreakComparator();

        Queue<Process> runningQ = new LinkedList<>();
        ArrayList<Process> readyList = new ArrayList<>();
        ArrayList<Process> blockedList = new ArrayList<>();
        PriorityQueue<Process> blockedQ = new PriorityQueue<>(tieBreakComparator);
        PriorityQueue<Process> terminatedQ = new PriorityQueue<>(idComparator);
        PriorityQueue<Process> arrivalQ = new PriorityQueue<>(processList.size(), arrivalComparator);

        Comparator<Process> penaltyComparator = new Comparator<Process>() {
            @Override
            public int compare(Process currentProcess, Process nextProcess) {
                //return currentProcess.getArrivalTime() < nextProcess.getArrivalTime() ? -1:1;

                if (currentProcess.penaltyRatio > nextProcess.penaltyRatio) {
                    return -1;
                } else if (currentProcess.penaltyRatio < nextProcess.penaltyRatio) {
                    return 1;
                } else {
                    if (currentProcess.inputTime < nextProcess.inputTime) {
                        return -1;
                    } else if (currentProcess.inputTime > nextProcess.inputTime) {
                        return 1;
                    } else {
                        if (currentProcess.id < nextProcess.id) {
                            return -1;
                        } else if (currentProcess.id > nextProcess.id) {
                            return 1;
                        } else { //if arrival time is equal, choose priority by input time
                            return 0;
                        }//if arrival time is equal, choose priority by input time
                    }
                }
            }
        };

        //set input time to arrival time
        for (int i = 0; i < processList.size(); i++) {
            currentProcess = processList.get(i);
            currentProcess.inputTime = currentProcess.arrivalTime;
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

        while (terminatedQ.size() < numProcesses) {

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

            /** Update Penalty Ratio **/
            if (!readyList.isEmpty()) {
                for (Process p : readyList) {
                    p.waitTime++;
                    p.T = cycle - p.arrivalTime;
                    p.penaltyRatio = p.T/Math.max(1,p.cpuTime);
                }
            }
            if(!blockedList.isEmpty()) {
                for (Process p :  blockedList) {
                    p.T = cycle - p.arrivalTime;
                    p.penaltyRatio = p.T/Math.max(1,p.cpuTime);
                }
            }
            if(!runningQ.isEmpty()) {
                for (Process p : runningQ) {
                    p.T = cycle - p.arrivalTime;
                    p.penaltyRatio = p.T/Math.max(1,p.cpuTime);
                }
            } /** Update Penalty Ratio **/

            /** doBlocked **/
            if (!blockedList.isEmpty()) {
                for (int i = 0; i < blockedList.size(); i++) {
                    currentProcess = blockedList.get(i);
                    currentProcess.ioTime++;
                    currentProcess.ioLeft--;
                }
                totalIOTime++;

                Iterator<Process> iterator = blockedList.iterator();
                while (iterator.hasNext()) {
                    currentProcess = iterator.next();
                    if (currentProcess.ioLeft <= 0) {
                        blockedQ.add(currentProcess);
                        iterator.remove();
                    }
                }
                while (!blockedQ.isEmpty()) {
                    currentProcess = blockedQ.remove();
                    readyList.add(currentProcess);
                    currentProcess.state = "ready";
                    //currentProcess.inputTime = cycle;
                }
            } /** doBlocked **/

            /** doRunning **/
            if (!runningQ.isEmpty()) {
                currentProcess = runningQ.peek();
                currentProcess.cpuTime++;
                currentProcess.cpuLeft--;
                totalRunTime++;

                if (currentProcess.totalCPUTime <= currentProcess.cpuTime) {
                    terminatedQ.add(runningQ.poll());
                    currentProcess.state = "terminated";
                    currentProcess.finishTime = cycle;
                } else if (currentProcess.cpuLeft <= 0) {
                    blockedList.add(runningQ.poll());
                    currentProcess.state = "blocked";

                    X = (int) randNumList.get(count);
                    count++;
                    //System.out.printf("Find I/O burst when blocking process: %d\n", X);
                    currentProcess.ioLeft = randomOS(currentProcess.ioBurst);
                }
            } /** doRunning **/

            /** doArriving **/
            while (!arrivalQ.isEmpty() && arrivalQ.peek().arrivalTime <= cycle) {
                currentProcess = arrivalQ.remove();
                readyList.add(currentProcess);
                currentProcess.state = "ready";
            } /** doArriving **/

            /** doReady **/
            if (!readyList.isEmpty() && runningQ.isEmpty()) {

                Collections.sort(readyList, penaltyComparator);

                //run process w/ highest r value
                runningQ.add(readyList.remove(0));
                currentProcess = runningQ.peek();
                //System.out.printf("\nr = %f\n", currentProcess.penaltyRatio);
                currentProcess.state = "running";

                X = (int) randNumList.get(count); //chose rand int x = 1
                count++;
                //System.out.printf("Find CPU burst when choosing ready process to run: %d\n", X);

                currentProcess.cpuLeft = randomOS(currentProcess.cpuBurst);

                if (currentProcess.cpuLeft >= (currentProcess.totalCPUTime - currentProcess.cpuTime)) {
                    currentProcess.cpuLeft = (currentProcess.totalCPUTime - currentProcess.cpuTime);
                }
            } /** doReady **/
            cycle++;
        }
        System.out.println("\t\t\t\t\tHighest Penalty Ratio Next");
        finishTime = cycle - 1;

        avgTurnaroundTime = this.avgTurnaroundTime(terminatedQ);
        avgWaitTime = this.avgWaitTime(terminatedQ);
        ioUtilization = this.ioUtil();
        cpuUtilization = this.cpuUtil(terminatedQ, totalRunTime);
        throughput = this.throughput(terminatedQ);

        for (Process s : processList) {
            s.printStats();
        }
        this.printSummary();
        return terminatedQ;
    }

    public int randomOS(int U) {
        return 1 + (X % U);
    }

    public float avgTurnaroundTime(Queue<Process> terminatedQ) {
        float totalTurnaroundTime = 0;

        for (Process p : terminatedQ) {
            totalTurnaroundTime += p.getTurnaroundTime();
        }
        return (totalTurnaroundTime / terminatedQ.size());
    }

    public float avgWaitTime(Queue<Process> terminatedQ) {
        float totalWaitTime = 0;
        for (Process p : terminatedQ) {
            totalWaitTime += p.waitTime;
        }
        return (totalWaitTime / terminatedQ.size());
    }

    public double cpuUtil(Queue<Process> terminatedQ, int runTime) {
        float p = (float) runTime / (float) this.finishTime;
        return (p);
    }

    public float ioUtil() {
        return ((float) this.totalIOTime / (float) this.finishTime);
    }

    public float avgIOTime(Queue<Process> terminatedQ) {
        float totalIOTime = 0;
        for (Process p : terminatedQ) {
            totalIOTime += p.ioTime;
        }
        return (totalIOTime / terminatedQ.size());
    }

    public float throughput(Queue<Process> terminatedQ) {
        float x = (float) terminatedQ.size() / (float) finishTime;
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
}





