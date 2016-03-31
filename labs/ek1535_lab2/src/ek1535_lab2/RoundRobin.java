package ek1535_lab2;
import java.util.*;
/**
 * Created by Aedo on 3/28/16.
 */
public class RoundRobin extends Scheduler {
    int CPUBurst;
    int IOBurst;
    int IOTime;
    private int finishTime;
    private float avgTurnaroundTime;
    private float avgWaitTime;
    private float ioUtilization;
    private double cpuUtilization;
    private float throughput;
    private int runTime; //incremented every cycle process is running

    public Queue<Process> rr(ArrayList<Process> processList, ArrayList randNumList) {
        int quantum = 2;
        int cycle = 0;
        int count = 0;
        runTime = 0;
        Process currentProcess;
        int numProcess = processList.size();

        //create a priority queue of processes based on arrival time and then input time
        Comparator<Process> arrivalComparator = new ArrivalTimeComparator();
        PriorityQueue<Process> arrivalQ = new PriorityQueue<>(processList.size(), arrivalComparator);

        for(int i = 0; i < processList.size(); i++) {
            arrivalQ.add(processList.get(i));
        }

        Queue<Process> readyQ = new LinkedList<>(); //queue stores all ready processes
        Queue<Process> blockedQ = new LinkedList<>(); //queue stores all blocked processes
        Queue<Process> runningQ = new LinkedList<>(); //queue stores running process
        Queue<Process> terminatedQ = new LinkedList<>(); //queue stores terminated processes
        ArrayList<Process> blockedList = new ArrayList<>();

        for(int i = 0; i < processList.size(); i++) {
            arrivalQ.add(processList.get(i));
        }

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

        while (terminatedQ.size() < numProcess) {

            //if verbose
            System.out.printf("Before cycle\t %2d:", cycle);
            for (Process s : processList) {
                if (s.state == "unstarted" || s.state == "terminated" || s.state == "ready") {
                    System.out.printf("\t\t\tProcess %s: %10s %2d", s.id, s.state, 0);
                } else if (s.state == "running") {
                    System.out.printf("\t\t\tProcess %s: %10s %2d", s.id, s.state, s.cpuLeft);
                } else if (s.state == "blocked") {
                    System.out.printf("\t\t\tProcess %s: %10s %2d", s.id, s.state, s.ioLeft);
                }
            }
            System.out.println();

            if (!readyQ.isEmpty()) {
                for (Process p : readyQ) {
                    p.waitTime++;
                }
            }

            //doBlocked
            if (!blockedList.isEmpty()) {
                //each blocked process does i/o concurrently
                for (int i = blockedList.size()-1; i > -1; i--) {
                    currentProcess = blockedList.get(i);
                    currentProcess.ioTime++;
                    currentProcess.ioLeft--;
                    if(currentProcess.ioLeft <= 0) {
                        currentProcess.state = "ready";
                        readyQ.add(currentProcess);
                        //currentProcess.state = "ready";
                        blockedList.remove(i); //blockedList.remove(currentProcess);
                    }
                }
            }

            //doRunning
            if (!runningQ.isEmpty()) {
                currentProcess = runningQ.peek();

                currentProcess.cpuTime++; //update cpu time for process
                currentProcess.cpuLeft--; //currentProcess.cpuBurst - 1; //update cpu burst for process

                runTime++;

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
                }
                //check if RR quantum is 0 -> preempted
                else if (cycle%quantum == 0 && cycle != 0) {
                    readyQ.add(runningQ.poll());
                    currentProcess.state = "ready";
                }
            }

            //doArriving
            while (!arrivalQ.isEmpty() && arrivalQ.peek().arrivalTime <= cycle) {
                currentProcess = arrivalQ.remove();
                readyQ.add(currentProcess);
                currentProcess.state = "ready";
            }

            //doReady
            if (!readyQ.isEmpty() && runningQ.isEmpty()) {
                runningQ.add(readyQ.poll()); //which ready state first?
                currentProcess = runningQ.peek();
                currentProcess.state = "running";

                X = (int) randNumList.get(count);
                count++;
                //System.out.printf("Find CPU burst when choosing ready process to run: %d\n", X);
                currentProcess.cpuLeft = randomOS(currentProcess.cpuBurst);

                //randomOS chooses the cpu burst from random numbers
                if (currentProcess.cpuLeft >= (currentProcess.totalCPUTime - currentProcess.cpuTime)) { //currentProcess.cpuLeft <= randomOS(X)) {
                    //if cpu burst greater than remaining than set to total
                    currentProcess.cpuLeft = (currentProcess.totalCPUTime - currentProcess.cpuTime); //randomOS(X) is chosen cput burst
                    //System.out.printf("smaller cpBurst: %d, OS: %d", currentProcess.cpuBurst, randomOS(X));
                }
            }


            cycle++;
        }
        System.out.println("Round Robin");
        finishTime = cycle - 1;
        avgTurnaroundTime = avgTurnaroundTime(terminatedQ);
        avgWaitTime = avgWaitTime(terminatedQ);
        ioUtilization = ioUtil(terminatedQ);
        cpuUtilization = cpuUtil(terminatedQ, runTime);
        throughput = throughput(terminatedQ);
        //Comparator<Process> input
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
        float p = (float)runTime/(float)this.finishTime;

        //System.out.printf("\n\n\n\n\nrunTime: %d\n finishTime: %d, p:%f", runTime, finishTime, p);

        return(p);
    }

    //percentage of time job is blocked
    //total io time / finish time
    public float ioUtil(Queue<Process> terminatedQ) {
        float totalIOTime = 0;
        for (Process p : terminatedQ) {
            totalIOTime += p.ioTime;
        }
        return (totalIOTime/finishTime);
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
