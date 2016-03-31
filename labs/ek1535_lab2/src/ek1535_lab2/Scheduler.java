package ek1535_lab2;

import java.util.Queue;

/**
 * Created by Aedo on 3/19/16.
 */
public abstract class Scheduler {
    int X;//random integer chosen from random-numbers.txt

    private int finishTime;
    private float avgTurnaroundTime;
    private float avgWaitTime;
    private float ioUtilization;
    private double cpuUtilization;
    private float throughput;

    public int randomOS(int U) {
        return 1 + (X%U);
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
    }


}
