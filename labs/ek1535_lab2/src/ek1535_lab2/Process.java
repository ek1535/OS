package ek1535_lab2;

/**
 * Created by Aedo on 3/19/16.
 */
public class Process {
    int arrivalTime;
    int cpuBurst;
    int totalCPUTime;
    int ioBurst;
    String state;
    int startTime;
    int ioTime; //time in blocked, every process in blocked is doing IO
    int cpuTime; //time in run
    int waitTime; //time in ready, startTime-arrivalTime? w/o blocked, io consideration
    int finishTime;
    //int turnaroundTime; //time from arrival to completion -> finishTime - arrivalTime
    int arrivalLeft;
    int ioLeft;
    int cpuLeft;
    int id;

    public Process() {

    }
    public Process(int id, int arrivalTime, int cpuBurst, int totalCPUTime, int ioBurst) {
        this.id = id;
        this.arrivalTime = arrivalTime;
        this.cpuBurst = cpuBurst;
        this.totalCPUTime = totalCPUTime;
        this.ioBurst = ioBurst;
        this.state = "unstarted";
        this.ioTime = 0;
        this.waitTime = 0;
        this.startTime = 0;
        this.finishTime = 0;
    }

    //clone process
    public Process(Process p) {
        this.id = p.id;
        this.arrivalTime = p.arrivalTime;
        this.cpuBurst = p.cpuBurst;
        this.totalCPUTime = p.totalCPUTime;
        this.ioBurst = p.ioBurst;
        this.state = "unstarted";
        this.ioTime = 0;
        this.waitTime = 0;
        this.startTime = 0;
        this.finishTime = 0;
    }


    public int ioTime() {
        return this.ioTime;
    }
    //@Override
    public String toString() {
        return (this.arrivalTime + " " + this.cpuBurst + " " + this.totalCPUTime + " " + this.ioBurst + "    ");
    }
    public int getTurnaroundTime() {
        return this.finishTime - this.arrivalTime;
    }
    public void printStats() {
        System.out.printf("Process %d: \n", this.id);
        System.out.printf("\t\t(A,B,C,IO) = (%d,%d,%d,%d)\n", this.arrivalTime, this.cpuBurst,this.totalCPUTime,this.ioBurst);
        System.out.printf("\t\tFinishing time: %d\n", this.finishTime);
        System.out.printf("\t\tTurnaround time: %d\n", this.getTurnaroundTime());
        System.out.printf("\t\tI/O time: %d\n", this.ioTime);
        System.out.printf("\t\tWaiting time: %d\n\n", this.waitTime);
    }

    public int totalCpuLeft() {
        return this.totalCPUTime - this.cpuTime;
    }

}

