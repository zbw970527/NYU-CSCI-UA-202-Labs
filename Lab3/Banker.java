import java.io.*;
import java.util.*;

//Bowen Zhang bz896
//Lab 3

public class Banker{
	
	String filename;
	int[] resArrayO; // store the numebr of available resource of each type for Optimistic algorithm
	int[] resArrayB; // store the numebr of available resource of each type for Banker's algorithm
	ArrayList<Task> tasksO = new ArrayList<Task>(); // stores tasks for optimistic 
	ArrayList<Task> tasksB = new ArrayList<Task>(); // stores tasks for banker's
	ArrayList<Command> blocked; // the arraylist holding blocked commands
	ArrayList<Command> executing; // the arraylist holding commands that is going to be executed this round.
	ArrayList<Command> releaseArray; // the arraylist holding commands which is release
	int numRes; // number of resource types
	int numTasks; // number of tasks
	boolean[] terminate; // the array which store the terminate status of tasks. the slot id-1 will be set to true if it is terminated or aborted.
	
	public void readCommand() {
		try {
		   // initialize the file scanner
		   Scanner scanner = new Scanner(new BufferedReader(new FileReader(filename)));
		   numTasks = scanner.nextInt();
		   numRes = scanner.nextInt();
		   resArrayO = new int[numRes];
		   resArrayB = new int[numRes];
		   for(int i = 0; i < numRes; i++) {
			   resArrayO[i] = scanner.nextInt();
			   resArrayB[i] = resArrayO[i];
		   }
		   // initialize two copies of tasks which one is for optimistic and the other one is for banker's
		   for(int i = 0; i < numTasks; i++) {
			   Task t1 = new Task();
			   Task t2 = new Task();
			   t1.requests = new int[numRes];
			   t1.holding = new int [numRes];
			   t2.requests = new int[numRes];
			   t2.holding = new int [numRes];
			   tasksO.add(t1);
			   tasksB.add(t2);
		   }
		   // initialize two copies of tasks which one is for optimistic and the other one is for banker's
		   while(scanner.hasNext()) {
			   Command c1 = new Command(scanner.next(), scanner.nextInt(),scanner.nextInt(),scanner.nextInt(),scanner.nextInt());
			   Command c2 = new Command(c1.command, c1.id, c1.delay, c1.resType, c1.number);
			   tasksO.get(c1.id-1).commands.add(c1);
			   tasksB.get(c2.id-1).commands.add(c2);
		   }
		   
		  }catch(FileNotFoundException e) {
			  System.out.println("File not found! Please check the file name and the directory.");
		  }  
	}
	
	public void optimistic() {
		
		releaseArray = new ArrayList<Command>();// the arraylist contains command with release order
		blocked = new ArrayList<Command>(); // store the command which can't be granted right now
		executing = new ArrayList<Command>(); // store the command which is going be executed during the current cycle.
		int timer = 0;
		terminate = new boolean[numTasks];
		
		//initialize tasks
		for(int i = 0; i < numRes; i++) {
			for(int j = 0; j < tasksO.size();j++) {
				Task t = tasksO.get(j);
				Command c = t.commands.remove(0);
				t.requests[c.resType-1] = c.number;
			}
			timer++;
		}

		//while there are still remaining commands, operate the tasks. 
		while(allTrue(terminate) == false) {
			
			// get all the executable command first
			// pop the uppermost command in each task. skip the tasks which is terminated, aborted or has commands blocked.
			for(int index = 0; index < tasksO.size(); index++) {
				Task t = tasksO.get(index);
				if(t.blocked == false && terminate[index] == false) {
					if(t.commands.get(0).delay != 0) {
						t.commands.get(0).delay --;
					}
					else
						executing.add(t.commands.remove(0));
				}
			}
			
			if(blocked.size() != 0) {
				//check the blocked commands
				for(int i = 0; i < blocked.size(); i++) {
					Command c = blocked.get(i);
					Task t = tasksO.get(c.id-1);
					t.wTime ++;
					if(c.number <= resArrayO[c.resType-1]) {
						//grant the request if it fits the request
						t.requests[c.resType-1] -= c.number;
						resArrayO[c.resType-1] = resArrayO[c.resType-1] - c.number;
						t.holding[c.resType-1] += c.number;
						t.blocked = false;
						blocked.remove(i);
						i--;
					}
				}
			}
			//run for each processes.
			while(executing.size() > 0) {
				
				Command c = executing.remove(0);
				Task t = tasksO.get(c.id-1);
				switch(c.command) {
	    			case "initiate":{
	    				System.out.println("error!");
	    				break;
	    			}
	    			case "request":{
	    				if(c.number <= resArrayO[c.resType-1]) {
	    					// grant the request
	    					t.requests[c.resType-1] -= c.number;
	    					t.holding[c.resType-1] += c.number;
	    					resArrayO[c.resType-1] = resArrayO[c.resType-1] - c.number;
	    				}
	    				else {
	    					// block the task and add it into the blocked arraylist.
	    					blocked.add(c);
	    					t.blocked = true;
	    				}
	    				break;
	    			}
	    			case "release":{
	    				// add the release command into releaseArrayList. Since the release resources will be available at the end, so I make a new Arraylist and operate
	    				// on this after all the other commands are executed. So that this will not be messed up with other request commands.
	    				releaseArray.add(c);
	    				break;
	    			}
	    			case "terminate":{
	    				// terminate the processe
	    				// even though the terminate process didn't execute with the last non-terminate command at the same loop, the end time recorded it still the same
	    				// as the last non-terminated command.
	    				t.tTime = timer;
	    				terminate[c.id-1] = true;
	    				break;
	    			}
				}
				
			}
			//release resources
			while(releaseArray.size() > 0) {
				Command c = releaseArray.remove(0);
				Task t = tasksO.get(c.id-1);
				resArrayO[c.resType-1] += c.number;
				t.holding[c.resType-1] -= c.number;
				t.requests[c.resType-1] += c.number;
			}
			
			//check the deadlock and abort tasks if needed
			boolean flag = true;
			if(allTrue(terminate))
				flag = false;
			else {
				for(int i = 0; i < tasksO.size(); i++) {
					Task t = tasksO.get(i);
					if(t.aborted == false && terminate[i] == false && t.blocked == false)
						flag = false;
				}
			}
			while(flag == true) {
				//abort the task with lowest index
				for(int index = 0; index < terminate.length; index++) {
					if(terminate[index] == false) {
						Task t = tasksO.get(index);
						t.aborted = true;
						terminate[index] = true;
						t.blocked = false;
						//release resources
						for(int i = 0; i < t.holding.length; i++) {
							resArrayO[i] += t.holding[i];
							t.holding[i] = 0;
						}
						//remove pending commands
						for(int i = 0; i < blocked.size(); i++) {
							Command c = blocked.get(i);
							if(c.id-1 == index) {
								blocked.remove(i);
							}
						}
						System.out.println();
						System.out.println("Task " + (index+1) + " is aborted and all the resources are released and available next round.");
						System.out.println();
						break;
					}
				}
				//check if the deadlock is gone
				for(int i = 0; i < blocked.size(); i++) {
					Command c = blocked.get(i);
					if(c.number <= resArrayO[c.resType-1]) {
						flag = false;
					}
				}
			}
			// time increment
			timer++;
		}
	}
	
	public void banker() {
		
		releaseArray = new ArrayList<Command>();// the arraylist contains command with release order
		blocked = new ArrayList<Command>(); // store the command which can't be granted right now
		executing = new ArrayList<Command>(); // store the command which is going be executed during the current cycle.
		int timer = 0;
		//initialize tasks
		
		terminate = new boolean[numTasks]; // initialize the terminal array to be false
		
		//initialize the tasks
		for(int i = 0; i < numRes; i++) {
			for(int j = 0; j < tasksB.size();j++) {
				Task t = tasksB.get(j);
				Command c = t.commands.remove(0);
				//abort the task if claim exceeds the input. 
				if(c.number > resArrayB[i]) {
					t.aborted = true;
					terminate[c.id-1] = true;
					System.out.println(c.id + " is aborted");
				}
				else 
					t.requests[c.resType-1] = c.number;
			}
			timer++;
		}
		
		//while there are still remaining commands
		while(allTrue(terminate) == false) {
			
			// get all the executable command first
			for(int index = 0; index < tasksB.size(); index++) {
				Task t = tasksB.get(index);
				if(t.blocked == false && terminate[index] == false && t.aborted == false) {
					if(t.commands.get(0).delay != 0) {
						t.commands.get(0).delay --;
					}
					else
						executing.add(t.commands.remove(0));
				}
			}
			
			if(blocked.size() != 0) {
				//check the blocked commands
				for(int i = 0; i < blocked.size(); i++) {
					Command c = blocked.get(i);
					Task t = tasksB.get(c.id-1);
					t.wTime ++;
					if(!checkDL(c)) {
						//grant the request
						t.requests[c.resType-1] -= c.number;
						resArrayB[c.resType-1] = resArrayB[c.resType-1] - c.number;
						t.holding[c.resType-1] += c.number;
						t.blocked = false;
						blocked.remove(i);
						i--;
					}
				}
			}
			//run for each processes.
			while(executing.size() > 0) {
				
				Command c = executing.remove(0);
				Task t = tasksB.get(c.id-1);
				switch(c.command) {
	    			case "initiate":{
	    				System.out.println("error!");
	    				break;
	    			}
	    			case "request":{
	    				if(!checkDL(c)) {
	    					if(c.number <= tasksB.get(c.id-1).requests[c.resType-1]) {
	    						// grant the request
		    					t.requests[c.resType-1] -= c.number;
		    					t.holding[c.resType-1] += c.number;
		    					resArrayB[c.resType-1] = resArrayB[c.resType-1] - c.number;
	    					}
	    					else {
	    						//abort the task because demand exceed claim
	    						t.aborted = true;
	    						terminate[c.id-1] = true;
	    						//release resources
	    						for(int i = 0; i < t.holding.length; i++) {
	    							resArrayB[i] += t.holding[i];
	    							t.holding[i] = 0;
	    							t.blocked = false;
	    						}
	    						//remove pending requests
	    						for(int i = 0; i < blocked.size(); i++) {
	    							Command c1 = blocked.get(i);
	    							if(c1.id-1 == c.id-1) {
	    								blocked.remove(i);
	    							}
	    						}
	    						System.out.println();
	    						System.out.println("Task " + c.id + " is aborted because the request exceeds claim and all the resources are released and available next round.");
	    						System.out.println();
	    					}
	    				}
	    				else {
	    					// add the command to blocked arraylist if request can't granted
	    					blocked.add(c);
	    					t.blocked = true;
	    				}
	    				break;
	    			}
	    			case "release":{
	    				releaseArray.add(c);
	    				break;
	    			}
	    			case "terminate":{
	    				t.tTime = timer;
	    				terminate[c.id-1] = true;
	    				break;
	    			}
				}
				
			}
			// check release array and update resources
			while(releaseArray.size() > 0) {
				Command c = releaseArray.remove(0);
				Task t = tasksB.get(c.id-1);
				resArrayB[c.resType-1] += c.number;
				t.holding[c.resType-1] -= c.number;
				t.requests[c.resType-1] += c.number;
			}
			
			timer++;
		}
		//print the status after 
//		System.out.println("Banker");
//		int totalT = 0;
//		int totalW = 0;
//		for(int i = 0; i < tasksB.size(); i++) {
//			Task t = tasksB.get(i);
//			if(t.aborted == false) {
//				totalT += t.tTime;
//				totalW += t.wTime;
//				System.out.println("Task " + (i+1) + " " + t.tTime + " " + t.wTime + " " + Math.round(t.wTime*1.0/t.tTime*100) + "%");
//			}
//			else
//				System.out.println("Task " + (i+1) + " aborted");
//		} 
//		System.out.println("total " + totalT + " " + totalW + " " + Math.round(totalW*1.0/totalT*100) + "%");	
//		
	}
	
	// the method checks if all the boolean value in the array are all true. 
	//print true if all true
	public boolean allTrue(boolean[] array) {
		boolean flag = true;
		for(boolean b : array) {
			if(!b) {
				flag = false;
				break;
			}
		}
		return flag;
	}
	
	//Check if there is potential deadlock
	public boolean checkDL(Command c) {
		Task t = tasksB.get(c.id-1);
		
		for(int j = 0; j < numRes; j++) {
			if(t.requests[j] > resArrayB[j]) {
				return true;
			}
		}
		return false;
	}
	
	public void printStatistics() {
		//print the status statistics 
		System.out.println("\t\tFIFO \t\t\t\t\t BANKER'S");
		int totalTO = 0;
		int totalWO = 0;
		int totalTB = 0;
		int totalWB = 0;
		for(int i = 0; i < numTasks; i++) {
			Task t1 = tasksO.get(i);
			Task t2 = tasksB.get(i);
			if(t1.aborted == false) {
				totalTO += t1.tTime;
				totalWO += t1.wTime;
				System.out.print("\tTask " + (i+1) + "\t " + t1.tTime + "   " + t1.wTime + "   " + Math.round(t1.wTime*1.0/t1.tTime*100) + "%");
			}
			else
				System.out.print("\tTask " + (i+1) + "\t aborted");
			if(t2.aborted == false) {
				totalTB += t2.tTime;
				totalWB += t2.wTime;
				System.out.print("\t\t\tTask " + (i+1) + "\t " + t2.tTime + "   " + t2.wTime + "   " + Math.round(t2.wTime*1.0/t2.tTime*100) + "%");
			}
			else
				System.out.print("\t\t\tTask " + (i+1) + "\t aborted");
			System.out.println();
		} 
		System.out.print("\ttotal  \t" + totalTO + "   " + totalWO + "   " + Math.round(totalWO*1.0/totalTO*100) + "%");
		System.out.print("\t\t\ttotal  \t" + totalTB + "   " + totalWB + "   " + Math.round(totalWB*1.0/totalTB*100) + "%");
	}
	
	public static void main(String[] args) {
		Banker b = new Banker();
		b.filename = args[0];
		b.readCommand();
		b.optimistic();
		b.banker();
		b.printStatistics();
	}
	
}
// this is the class of command, which is sth like "initiate  1 0 1 4"
class Command{
	
	String command;
	int id;
	int delay;
	int resType;
	int number;
	
	public Command(String command, int id, int delay, int resType, int number) {
		this.command = command;
		this.id = id;
		this.delay = delay;
		this.resType = resType;
		this.number = number;
	}
}
// the task class
class Task{
	int tTime = 0;
	int wTime = 0;
	ArrayList<Command> commands = new ArrayList<Command>();
	boolean aborted = false;
	boolean blocked = false;
	int[] requests;
	int[] holding;
	
	public Task() {}
}