/* Bowen Zhang
 * 
 * Operating System 
 * 
 * Lab 4
 * 
 */

import java.io.*;
import java.util.*;

public class Paging{
	
	int machineSize;
	int pageSize;
	int processSize;
	int jobmix;
	int numRef; // number of references each process has
	String repMeth; // replacement strategy
	Scanner scanner;
	Process[] processes;
	Frame[] frame;
	
	public void start() {
		try {
			//initialize file reader
			scanner = new Scanner(new BufferedReader(new FileReader("random-numbers")));
		}catch(FileNotFoundException e) {
			System.out.println("File Not Found! Check the directory.");
		}
		//initial frame table based on machine size and page size
		frame = new Frame[machineSize/pageSize];
		for(int i = 0; i < frame.length; i++) {
			frame[i] = new Frame();
		}
		int time = 0;// this is the counter of time
		switch(jobmix) {
			case 1:{
				//initialize processes with given jobmix number
				processes = new Process[1];
				processes[0] = new Process(processSize, 1, numRef, 1, 0, 0);
				while(processes[0].refRemain > 0) {
					time ++;
					int pagenum = processes[0].nextword/pageSize; // calculate the page number
					//update the frame table if there is a page fault
					if(pageFault(pagenum, 1, time)) {
						updateTable(1, pagenum, time);
					}
					//decrease the remaining references and calculate the next word.
					processes[0].refRemain--;
					processes[0].nextWord(processes[0].a, processes[0].b, processes[0].c, scanner);
				}
				break;
			}
			case 2:{
				//initialize processes with given jobmix number
				processes = new Process[4];
				for(int i = 0; i < processes.length; i++) {
					processes[i] = new Process(processSize, i+1, numRef, 1, 0, 0);
				}
				//suppose in each round all the processes does three references
				//calculate the total round needed to complete all the reference
				int round = numRef/3;
				// for example if numRef = 10, there should be 4 round to complete
				if(numRef % 3 != 0)
					round++;
				//start round
				for(int i = 0; i < round; i++) {
					
					//go over all the processes
					for(int j = 0; j < processes.length; j++) {
						
						//each process does 3 references each time
						for(int k = 0; k < 3; k++) {
							if(processes[j].refRemain > 0) { // exit the quantum if all the references are done
								time++;
								int nw = processes[j].nextword;
								int pagenum = nw / pageSize;
								//update the frame table if there is a page fault
								if(pageFault(pagenum, j+1, time)) {
									updateTable(j+1, pagenum, time);
								}
								//decrease the remaining references and calculate the next word.
								processes[j].refRemain--;
								processes[j].nextWord(processes[j].a, processes[j].b, processes[j].c, scanner);
							}else
								break;
						}
					}
				}
				break;
			}
			case 3:{
				//initialize processes with given jobmix number
				processes = new Process[4];
				for(int i = 0; i < processes.length; i++) {
					processes[i] = new Process(processSize, i+1, numRef, 0, 0, 0);
				}
				//suppose in each round all the processes does three references
				//calculate the total round needed to complete all the reference
				int round = numRef/3;
				// for example if numRef = 10, there should be 4 round to complete
				if(numRef % 3 != 0)
					round++;
				//start round
				for(int i = 0; i < round; i++) {
					
					//go over all the processes
					for(int j = 0; j < processes.length; j++) {
						
						//each process does 3 references each time
						for(int k = 0; k < 3; k++) {
							if(processes[j].refRemain > 0) {// exit the quantum if all the references are done
								time++;
								int nw = processes[j].nextword;
								int pagenum = nw / pageSize;
								//update the frame table if there is a page fault
								if(pageFault(pagenum, j+1, time)) {
									updateTable(j+1, pagenum, time);
								}
								//decrease the remaining references and calculate the next word.
								processes[j].refRemain--;
								processes[j].nextWord(processes[j].a, processes[j].b, processes[j].c, scanner);
							}else
								break;
						}
					}
				}
				break;
			}
			case 4:{
				//initialize processes with given jobmix number
				processes = new Process[4];
				processes[0] = new Process(processSize, 1, numRef, 0.75, 0.25, 0);
				processes[1] = new Process(processSize, 2, numRef, 0.75, 0, 0.25);
				processes[2] = new Process(processSize, 3, numRef, 0.75, 0.125, 0.125);
				processes[3] = new Process(processSize, 4, numRef, 0.5, 0.125, 0.125);
				
				//suppose in each round all the processes does three references
				//calculate the total round needed to complete all the reference
				int round = numRef/3;
				// for example if numRef = 10, there should be 4 round to complete
				if(numRef % 3 != 0)
					round++;
				//start round
				for(int i = 0; i < round; i++) {
					
					//go over all the processes
					for(int j = 0; j < processes.length; j++) {
						
						//each process does 3 references each time
						for(int k = 0; k < 3; k++) {
							if(processes[j].refRemain > 0) {// exit the quantum if all the references are done
								time++;
								int nw = processes[j].nextword;
								int pagenum = nw / pageSize;
								//update the frame table if there is a page fault
								if(pageFault(pagenum, j+1, time)) {
									updateTable(j+1, pagenum, time);
								}
								//decrease the remaining references and calculate the next word.
								processes[j].refRemain--;
								processes[j].nextWord(processes[j].a, processes[j].b, processes[j].c, scanner);
							}else
								break;
						}
					}
				}
				break;
			}
		}
		//print result
		int totfault = 0;
		double totrestime = 0; // total residency time
		int totevitime = 0; // total evict time
		System.out.println("The machine size is " + machineSize + ".");
		System.out.println("The page size is " + pageSize + ".");
		System.out.println("The process size is " + processSize + ".");
		System.out.println("The job mix number is " + jobmix + ".");
		System.out.println("The number of references per process is " + numRef + ".");
		System.out.println("The replacement algorithm is " + repMeth + ".");
		System.out.println();
		for(int i = 0; i < processes.length; i ++) {
			if(processes[i].evicttime != 0) {
				totfault += processes[i].pftime;
				totrestime += processes[i].residencytime;
				totevitime += processes[i].evicttime;
				System.out.println("Process " + (i+1) + " had " + processes[i].pftime + " faults and " + ((double)processes[i].residencytime)/processes[i].evicttime + " average residency.");
			}
			else {
				totfault += processes[i].pftime;
				totrestime += processes[i].residencytime;
				System.out.println("Process " + (i+1) + " had " + processes[i].pftime + " faults.");
				System.out.println("     With no evictions, the average residence is undefined.");
			}
		}
		System.out.println();
		if(totevitime != 0)
			System.out.println("The total number of faults is "+ totfault +" and the overall average residency is "+ totrestime/totevitime+ ".");
		else {
			System.out.println("The total number of faults is " + totfault + ".");
			System.out.println("     With no evictions, the overall average residence is undefined.");
		}
		
	}
	
	public boolean pageFault(int pagenum, int processnum, int time) {
		for(int i = 0; i < frame.length; i++) {
			if(frame[i].vacant == false) {
				if(frame[i].pagenumber == pagenum && frame[i].procindex == processnum) {
					if(repMeth.compareTo("lru") == 0) {
						frame[i].pagehit(time);
					}
					return false;
				}
			}
		}
		//update the page fault times if there is a page fault
		processes[processnum-1].pftime++;
		return true;
	}
	
	//update the frame table based on given replacement strategy
	public void updateTable(int procindex, int pagenumber, int time) {
		switch(repMeth) {
			case "lru":{
				int lru = 0;
				//use the highest index of free frame if exist
				for(int i = frame.length-1; i >= 0; i--) {
					if(frame[i].vacant == true) {
						frame[i].update(procindex, pagenumber, time);
						return;
					}else {
						//if the frame is occupied, record the currently least recent used frame index.
						if(frame[i].lastused < frame[lru].lastused)
							lru = i;
					}
				}
				//replace the least used frame and add residency time & evict time
				processes[frame[lru].procindex-1].evicttime++;
				processes[frame[lru].procindex-1].residencytime += (time - frame[lru].loadtime);
				frame[lru].update(procindex, pagenumber, time);
				break;
			}
			case "fifo":{
				int earliest = 0;
				//use the highest index of free frame if exist
				for(int i = frame.length-1; i >= 0 ; i--) {
					if(frame[i].vacant == true) {
						frame[i].update(procindex, pagenumber, time);
						return;
					}else {
						// update the earliest frame index
						if(frame[i].loadtime < frame[earliest].loadtime)
							earliest = i;
					}
				}
				//replace the ealiest loaded frame and add residency time & evict time
				processes[frame[earliest].procindex-1].evicttime++;
				processes[frame[earliest].procindex-1].residencytime += (time - frame[earliest].loadtime);
				frame[earliest].update(procindex, pagenumber, time);
				break;
			}
			case "random":{
				//use the highest index of free frame if exist
				for(int i = frame.length-1; i >= 0 ; i--) {
					if(frame[i].vacant == true) {
						frame[i].update(procindex, pagenumber, time);
						return;
					}
				}
				//if no free frame, generate a random evict frame number and replace it.
				int evictframe = scanner.nextInt() %(machineSize/pageSize);
				processes[frame[evictframe].procindex-1].evicttime ++;
				processes[frame[evictframe].procindex-1].residencytime += (time - frame[evictframe].loadtime);	
				frame[evictframe].update(procindex, pagenumber, time);
				break;
			}
		}
	}
	
	public static void main(String[] args) {
		Paging p = new Paging();
		p.machineSize = Integer.parseInt(args[0]);
		p.pageSize = Integer.parseInt(args[1]);
		p.processSize = Integer.parseInt(args[2]);
		p.jobmix = Integer.parseInt(args[3]);
		p.numRef = Integer.parseInt(args[4]);
		p.repMeth = args[5];
		
		p.start();
	}
}
class Process{
	
	int size = 0;
	int index = 0;
	int refRemain = 0;// remaining references of this process
	int pftime = 0; //page fault time
	int evicttime = 0; // evict time
	int residencytime = 0; // total residency of the process
	int nextword = 0; // the next word to reference
	double a;//fraction A
	double b;//Fraction B
	double c;//fraction C
	
	public Process(int size, int index, int numRef, double a, double b, double c) {
		this.size = size;
		this.index = index;
		this.refRemain = numRef;
		this.nextword = (111*index)%size; // calculate the start word.
		this.a = a;
		this.b = b;
		this.c = c;
	}
	//calculate net word
	public void nextWord(double a, double b, double c, Scanner scanner) {
		
		double quotient = scanner.nextInt()/(Integer.MAX_VALUE + 1d);
		if(quotient < a) {
			nextword = (nextword+1) % size;
		}else if(quotient < a+b) {
			nextword = (nextword-5+size) % size;
		}else if(quotient < a+b+c) {
			nextword = (nextword+4) % size;
		}else {
			nextword = scanner.nextInt() % size;
		}
	}
}
class Frame{
	boolean vacant = true;
	int procindex = 0; // the index of process which is stored in this frame.
	int pagenumber = 0; // the page number of the current frame
	int loadtime = 0;// the time when the frame is loaded
	int lastused = 0;// the time when the frame is last used. only used in lru
	
	// update the frame
	public void update(int procindex, int pagenumber, int loadtime) {
		this.procindex = procindex;
		this.pagenumber = pagenumber;
		this.loadtime = loadtime;
		lastused = loadtime;
		vacant = false;
	}
	//increase the last used time
	public void pagehit(int time) {
		lastused = time;
	}
}