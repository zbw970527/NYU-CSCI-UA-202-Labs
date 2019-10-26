/* Bowen Zhang
 * Operting System
 * Programming project 2
 * 
 * Note: if you want to grade the code part by part, please commen the functions calls of the other three in the start() function.
 * 
 * Note: there might be tiny differences (differences in blanks in verbose output, and decimal numbers), and all the other data are the same
 * 
 * Note: for round robbin, there will be potential difference in verbose type
 *      for example, if only has 1s before terminate, and randomOS(b) = 5, for teacher's answer, it will print
 *      Before cycle   99:  terminated  0 terminated  0    running  2.
 *      but mine will give
 *      Before cycle   99:  terminated  0 terminated  0    running  1.
 *      
 *      for this case, my code will return the actual time which cpu bursts.
 *      (ps: i really don't understand why we should return running 2 even though there are only 1s before terminate)
 */


import java.io.*;
import java.util.*;
import java.text.DecimalFormat;

public class Scheduling{
 
 ArrayList<Node> start = new ArrayList<Node>();
 public Scanner filereader;
 boolean verbose = false; // set it true if verbose mode is needed
 
 public void fcfs(boolean v) {
  
  boolean print = v;
  if(v) {
   System.out.println("This detailed printout gives the state and remaining burst for each process\n");
   System.out.print("Before cycle    0: ");
   for(int p = 0; p < start.size(); p++) {
    System.out.print("  unstarted  0");
   }
   System.out.println(".");
  }
  
  int timePassed = 0; // holds the overall time of the operation
  int cpuburst = 0; // this variable stores the cpu burst of each time.
  int ioburst = 0; // this variable stores the io time of each time
  int totalcput = 0; // this variable holds the total cpu burst time of each processes
  int totaliot = 0; // this variable holds the time when there is/are process(es) doing io
  ArrayList<Node> ioqueue = new ArrayList<Node>(); // the ioqueue holds the processes which is doing io. processes will be remove from it if they finish io
  ArrayList<Node> queue = new ArrayList<Node>();  // the queue holds the process which is going to be run. aka. ready queue
  ArrayList<Node> before = new ArrayList<Node>(); // an array which stores elements that are not even come in . elements will be removed from the queue if timaPassed = a
  
  for(int i = 0; i < start.size(); i++) {
   Node n = start.get(i);
   if(n.a == 0) {
    n.status = "ready";
    queue.add(n);
   }else {
    n.status = "unstarted";
    before.add(n);
   }
  }
  
  while(queue.size()!=0 || ioqueue.size() != 0) {
   if(queue.size() != 0){
    Node node = queue.remove(0);
    node.status = "running";
    node.timeIncrement = 0;
    cpuburst = randomOS(node.b);
    node.remainingburst = cpuburst;
    if(cpuburst > node.remainingTime)
     cpuburst = node.remainingTime;
    
    for(int b = 0; b < cpuburst; b++) {
    
     timePassed ++;
     totalcput ++;
     if(v) {
      if(timePassed < 10)
       System.out.print("Before cycle\t" + timePassed + ": ");
      if(timePassed >=10 && timePassed < 100)
       System.out.print("Before cycle   " + timePassed + ": ");
      if(timePassed >=100 && timePassed < 1000)
       System.out.print("Before cycle  " + timePassed + ": ");
      if(timePassed >=1000)
       System.out.print("Before cycle " + timePassed + ": ");
      for(int p = 0; p < start.size(); p++) {
       Node pp = start.get(p);
       if(pp.status.compareTo("running") == 0)
        System.out.print("    " + pp.status + "  " + pp.remainingburst);
       if(pp.status.compareTo("blocked") == 0)
        System.out.print("    " + pp.status + "  " + pp.doingio);
       if(pp.status.compareTo("ready") == 0)
        System.out.print("      " + pp.status + "  " + pp.remainingburst);
       if(pp.status.compareTo("unstarted") == 0)
        System.out.print("  " + pp.status + "  0");
       if(pp.status.compareTo("terminated") == 0)
        System.out.print(" " + pp.status + "  0");
      }
      System.out.println(".");
     }
     node.remainingburst --;
     node.remainingTime --;
     //check if the process finish running
     if(node.remainingTime == 0) {
      node.finishTime = timePassed;
      node.status = "terminated";
     }
        
        // update the waiting time of queue
     for(int i = 0; i < queue.size(); i++) {
      queue.get(i).waitingtime ++;
      queue.get(i).timeIncrement ++;
     }
     
     //check the ioqueue, add nodes to the quene when they done io. 
     if(ioqueue.size() != 0)
      totaliot++;
     for(int index = 0; index < ioqueue.size(); index++) {
      ioqueue.get(index).doingio --;
      if(ioqueue.get(index).doingio == 0) { // if it finish its io process, remove it from ioqueue and add to queue. 
       Node n = ioqueue.remove(index);
       n.status = "ready";
       queue.add(n);
       index--;
      }
     }
     
     //uppdate the time in before queue and add process to queue if the timePassed = a
     for(int index = 0; index < before.size(); index++) {
      before.get(index).timetoready --;
      if(before.get(index).timetoready == 0) {
       Node n = before.remove(index);
       n.status = "ready";
       queue.add(n);
       index --;
      }
     }
     
    }
    //it process finishes its io process and does not terminate, calculate the io time and put it in ioqueue
    if(node.remainingTime > 0) {
     ioburst = randomOS(node.io);
     node.doingio = ioburst;
     node.status = "blocked";
     node.iotime += ioburst;
     ioqueue.add(node);
    }
    
    sortqueue(queue);
   }
   else {// when queue is empty but ioqueue is not
    boolean breakflag = false; // if any of the process in the ioqueue finishes its io process, turn the flag to true and break from the loop

    for(int i = 0; i > -1; i ++) {
     totaliot ++;
     timePassed++;
     if(v) {
      if(timePassed < 10)
       System.out.print("Before cycle\t" + timePassed + ": ");
      if(timePassed >=10 && timePassed < 100)
       System.out.print("Before cycle   " + timePassed + ": ");
      if(timePassed >=100 && timePassed < 1000)
       System.out.print("Before cycle  " + timePassed + ": ");
      if(timePassed >=1000)
       System.out.print("Before cycle " + timePassed + ": ");
      for(int p = 0; p < start.size(); p++) {
       Node pp = start.get(p);
       if(pp.status.compareTo("running") == 0)
        System.out.print("    " + pp.status + "  " + pp.remainingburst);
       if(pp.status.compareTo("blocked") == 0)
        System.out.print("    " + pp.status + "  " + pp.doingio);
       if(pp.status.compareTo("ready") == 0)
        System.out.print("      " + pp.status + "  " + pp.remainingburst);
       if(pp.status.compareTo("unstarted") == 0)
        System.out.print("  " + pp.status + "  0");
       if(pp.status.compareTo("terminated") == 0)
        System.out.print(" " + pp.status + "  0");
      }
      System.out.println(".");
     }
     for(int j = 0; j < ioqueue.size(); j++) {
      Node n = ioqueue.get(j);
      n.doingio --;
      if(n.doingio == 0) {
       breakflag = true;
       ioqueue.remove(j);
       n.status = "ready";
       queue.add(n);
       j = j -1;
      }
     }
     
     if(breakflag)
      break;
    }
    sortqueue(queue);
   }
  }
  int twt = 0; //total waiting time
  int ttt = 0; // total turnaround time
  System.out.println("The scheduling algorithm used was First Come First Served\n");
  for(int i = 0; i < start.size(); i++) {
   Node n = start.get(i);
   System.out.println("Process " + n.index +":");
   System.out.println("\t(A,B,C,IO) = (" + n.a +","+ n.b +","+ n.c +"," + n.io +")");
   System.out.println("\tFinishing time: " + n.finishTime);
   System.out.println("\tTurnaround time: " + (n.finishTime - n.a));
   System.out.println("\tI/O time: " + n.iotime);
   System.out.println("\tWaiting time: " + n.waitingtime +"\n");
   twt += n.waitingtime;
   ttt+= (n.finishTime - n.a);
   
  }
  System.out.println("Summary Data:");
  System.out.println("\tFinishing time: " + timePassed);
  System.out.println("\tCPU Utilization: " + new DecimalFormat("#0.000000").format(1.0 * totalcput/timePassed));
  System.out.println("\tI/O Utilization: " + new DecimalFormat("#0.000000").format(1.0* totaliot/timePassed));
  System.out.println("\tThroughput: " + new DecimalFormat("#0.000000").format(start.size() / (timePassed/100.0)) + " processes per hundred cycles");
  System.out.println("\tAverage turnaround time: " + new DecimalFormat("#0.000000").format(ttt * 1.0 / start.size()));
  System.out.println("\tAverage waiting time: " + new DecimalFormat("#0.000000").format(twt * 1.0 / start.size())+"\n");
 }
 
 public void rr(boolean v) {
  
  boolean print = v;
  if(v) {
   System.out.println("This detailed printout gives the state and remaining burst for each process\n");
   System.out.print("Before cycle    0: ");
   for(int p = 0; p < start.size(); p++) {
    System.out.print("  unstarted  0");
   }
   System.out.println(".");
  }
  
  int timePassed = 0; // holds the overall time of the operation
  int cpuburst = 0; // this variable stores the cpu burst of each time.
  int ioburst = 0; // this variable stores the io time of each time
  int totalcput = 0; // this variable holds the total cpu burst time of each processes
  int totaliot = 0; // this variable holds the time when there is/are process(es) doing io
  ArrayList<Node> ioqueue = new ArrayList<Node>(); // the ioqueue holds the processes which is doing io. processes will be remove from it if they finish io
  ArrayList<Node> queue = new ArrayList<Node>();  // the queue holds the process which is going to be run. aka. ready queue
  ArrayList<Node> before = new ArrayList<Node>(); // an array which stores elements that are not even come in . elements will be removed from the queue if timaPassed = a
  
  //copy the data from input to queue
  for(int i = 0; i < start.size(); i++) {
   Node n = start.get(i);
   if(n.a == 0) {
    n.status = "ready";
    queue.add(n);
   }else {
    n.status = "unstarted";
    before.add(n);
   }
  }
  while(queue.size()!=0 || ioqueue.size() != 0) {
   if(queue.size() != 0){
    // pop up the running processe from the queue
    Node node = queue.remove(0);
    node.status = "running";
    node.timeIncrement = 0;
    
    if(node.remainingburst != 0) {
     cpuburst = node.remainingburst;
     node.remainingburst = 0;
    }
    else {
     cpuburst = randomOS(node.b);
    }
    if(cpuburst > node.remainingTime) {
     cpuburst = node.remainingTime;
    }
    
    if(cpuburst >= 2) {
     node.remainingburst = cpuburst-2;
     cpuburst = 2;
    }

    node.currentburst = cpuburst;
    
    for(int b = 0; b < cpuburst; b++) {
     
     timePassed ++;
     totalcput ++;
     
     if(v) {
      if(timePassed < 10)
       System.out.print("Before cycle\t" + timePassed + ": ");
      if(timePassed >=10 && timePassed < 100)
       System.out.print("Before cycle   " + timePassed + ": ");
      if(timePassed >=100 && timePassed < 1000)
       System.out.print("Before cycle  " + timePassed + ": ");
      if(timePassed >=1000)
       System.out.print("Before cycle " + timePassed + ": ");
      for(int p = 0; p < start.size(); p++) {
       Node pp = start.get(p);
       if(pp.status.compareTo("running") == 0)
        System.out.print("    " + pp.status + "  " + pp.currentburst);
       if(pp.status.compareTo("blocked") == 0)
        System.out.print("    " + pp.status + "  " + pp.doingio);
       if(pp.status.compareTo("ready") == 0)
        System.out.print("      " + pp.status + "  0");
       if(pp.status.compareTo("unstarted") == 0)
        System.out.print("  " + pp.status + "  0");
       if(pp.status.compareTo("terminated") == 0)
        System.out.print(" " + pp.status + "  0");
      }
      System.out.println(".");
     }
     node.currentburst--;
     node.remainingTime --;
     //added it to the finish queue if it is finished
     if(node.remainingTime == 0) {
      node.finishTime = timePassed;
      node.status = "terminated";
     }
     
        // update the waiting time in the queue. 
     for(int i = 0; i < queue.size(); i++) {
      queue.get(i).waitingtime ++;
      queue.get(i).timeIncrement ++;
     }
     
     //update the remaining time of processes in ioqueue, add nodes to the quene when they done io.
     if(ioqueue.size() != 0)
      totaliot++;
     for(int index = 0; index < ioqueue.size(); index++) {
      ioqueue.get(index).doingio --;
      if(ioqueue.get(index).doingio == 0) { // if it finish its io process, remove it from ioqueue and add to queue. 
       Node n = ioqueue.remove(index);
       n.status = "ready";
       queue.add(n);
       index--;
      }
     }
     
     //uppdate the time in before queue and add process to queue if the timePassed = a
     for(int index = 0; index < before.size(); index++) {
      before.get(index).timetoready --;
      if(before.get(index).timetoready == 0) {
       Node n = before.remove(index);
       n.status = "ready";
       queue.add(n);
       index --;
      }
     }
    }
    
    // if it finishes cpu burst, calculate the io burst and add it to io queue.
    if(node.remainingburst == 0) {
     if(node.remainingTime > 0) {
      ioburst = randomOS(node.io);
      node.doingio = ioburst;
      node.status = "blocked";
      node.iotime += ioburst;
      ioqueue.add(node);
     }
    }
    // if after the 2 second period, there are still cpu burst time remaining, save the state and add the process back to the queue
    else {
     queue.add(node);
     node.status = "ready";
    }
    sortqueue(queue);
   }
   else {// when queue is empty but ioqueue is not
    boolean breakflag = false; // if any of the process in the ioqueue finishes its io process, turn the flag to true and break from the loop

    for(int i = 0; i > -1; i ++) {
     totaliot ++;
     timePassed++;
     if(v) {
      if(timePassed < 10)
       System.out.print("Before cycle\t" + timePassed + ": ");
      if(timePassed >=10 && timePassed < 100)
       System.out.print("Before cycle   " + timePassed + ": ");
      if(timePassed >=100 && timePassed < 1000)
       System.out.print("Before cycle  " + timePassed + ": ");
      if(timePassed >=1000)
       System.out.print("Before cycle " + timePassed + ": ");
      for(int p = 0; p < start.size(); p++) {
       Node pp = start.get(p);
       if(pp.status.compareTo("running") == 0)
        System.out.print("    " + pp.status + "  " + pp.currentburst);
       if(pp.status.compareTo("blocked") == 0)
        System.out.print("    " + pp.status + "  " + pp.doingio);
       if(pp.status.compareTo("ready") == 0)
        System.out.print("      " + pp.status + "  ");
       if(pp.status.compareTo("unstarted") == 0)
        System.out.print("  " + pp.status + "  0");
       if(pp.status.compareTo("terminated") == 0)
        System.out.print(" " + pp.status + "  0");
      }
      System.out.println(".");
     }
     for(int j = 0; j < ioqueue.size(); j++) {
      Node n = ioqueue.get(j);
      n.doingio --;
      if(n.doingio == 0) {
       breakflag = true;
       ioqueue.remove(j);
       n.status = "ready";
       queue.add(n);
       j = j -1;
      }
     }
     
     if(breakflag)
      break;
    }
    sortqueue(queue);
   }
  }
  int twt = 0; //total waiting time
  int ttt = 0; // total turnaround time
  System.out.println("The scheduling algorithm used was Round Robbin\n");
  for(int i = 0; i < start.size(); i++) {
   Node n = start.get(i);
   System.out.println("Process " + n.index +":");
   System.out.println("\t(A,B,C,IO) = (" + n.a +","+ n.b +","+ n.c +"," + n.io +")");
   System.out.println("\tFinishing time: " + n.finishTime);
   System.out.println("\tTurnaround time: " + (n.finishTime - n.a));
   System.out.println("\tI/O time: " + n.iotime);
   System.out.println("\tWaiting time: " + n.waitingtime +"\n");
   twt += n.waitingtime;
   ttt+= (n.finishTime - n.a);
   
  }
  System.out.println("Summary Data:");
  System.out.println("\tFinishing time: " + timePassed);
  System.out.println("\tCPU Utilization: " + new DecimalFormat("#0.000000").format(1.0 * totalcput/timePassed));
  System.out.println("\tI/O Utilization: " + new DecimalFormat("#0.000000").format(1.0* totaliot/timePassed));
  System.out.println("\tThroughput: " + new DecimalFormat("#0.000000").format(start.size() / (timePassed/100.0)) + " processes per hundred cycles");
  System.out.println("\tAverage turnaround time: " + new DecimalFormat("#0.000000").format(ttt * 1.0 / start.size()));
  System.out.println("\tAverage waiting time: " + new DecimalFormat("#0.000000").format(twt * 1.0 / start.size()) + "\n");
 }
 
 public void psjf(boolean v) {
  
  boolean print = v;
  if(v) {
   System.out.println("This detailed printout gives the state and remaining burst for each process\n");
   System.out.print("Before cycle    0: ");
   for(int p = 0; p < start.size(); p++) {
    System.out.print("  unstarted  0");
   }
   System.out.println(".");
  }
  
  
  
  int timePassed = 0; // holds the overall time of the operation
  int cpuburst = 0; // this variable stores the cpu burst of each time.
  int ioburst = 0; // this variable stores the io time of each time
  int totalcput = 0; // this variable holds the total cpu burst time of each processes
  int totaliot = 0; // this variable holds the time when there is/are process(es) doing io
  ArrayList<Node> ioqueue = new ArrayList<Node>(); // the ioqueue holds the processes which is doing io. processes will be remove from it if they finish io
  ArrayList<Node> queue = new ArrayList<Node>();  // the queue holds the process which is going to be run. aka. ready queue
  ArrayList<Node> before = new ArrayList<Node>(); // an array which stores elements that are not even come in . elements will be removed from the queue if timaPassed = a
  //add data into queue and before.
  for(int i = 0; i < start.size(); i++) {
   Node n = start.get(i);
   if(n.a == 0) {
    n.status = "ready";
    queue.add(n);
   }
   else {
    n.status = "unstarted";
    before.add(n);
   }
  }
  //to rearrange the list to let the shortest remaining time on the front.
  for(int i = 0; i < queue.size(); i++) {
   for(int j = i+1; j < queue.size(); j++) {
    if(queue.get(i).remainingTime > queue.get(j).remainingTime) {
     Node n = queue.get(i);
     queue.set(i, queue.get(j));
     queue.set(j, n);
    }
   }
  }
  // start of running
  while(queue.size() != 0 || ioqueue.size() != 0) {
   if(queue.size() != 0) {
    // get the current running proccess
    Node current = queue.get(0);
    current.status = "running";
    if(current.remainingburst != 0)
     cpuburst = current.remainingburst;
    else {
     cpuburst = randomOS(current.b);
    }
    current.remainingburst = cpuburst;
    if(cpuburst > current.remainingTime) {
      
     cpuburst = current.remainingTime;
    }
    
    
    // each round of this loop indicates 1s cpu burst
    for(int i = 0; i < cpuburst; i ++) {
     //update nodes in queue
     timePassed ++ ;
     totalcput ++;
     
     if(v) {
      if(timePassed < 10)
       System.out.print("Before cycle\t" + timePassed + ": ");
      if(timePassed >=10 && timePassed < 100)
       System.out.print("Before cycle   " + timePassed + ": ");
      if(timePassed >=100 && timePassed < 1000)
       System.out.print("Before cycle  " + timePassed + ": ");
      if(timePassed >=1000)
       System.out.print("Before cycle " + timePassed + ": ");
      for(int p = 0; p < start.size(); p++) {
       Node pp = start.get(p);
       if(pp.status.compareTo("running") == 0)
        System.out.print("    " + pp.status + "  " + pp.remainingburst);
       if(pp.status.compareTo("blocked") == 0)
        System.out.print("    " + pp.status + "  " + pp.doingio);
       if(pp.status.compareTo("ready") == 0)
        System.out.print("      " + pp.status + "  " + pp.remainingburst);
       if(pp.status.compareTo("unstarted") == 0)
        System.out.print("  " + pp.status + "  0");
       if(pp.status.compareTo("terminated") == 0)
        System.out.print(" " + pp.status + "  0");
      }
      System.out.println(".");
     }
     
     //uodate the waiting time of processes in the queue
     for(int index = 1; index < queue.size(); index++) {
      queue.get(index).waitingtime ++;
      
     }
     current.remainingTime --; // subtract the remaining time and burst time.
     current.remainingburst--;
     // if the process finish running, remove it from stack and put it into the index of finished array.
     if(current.remainingTime == 0) {
      queue.remove(0);
      current.remainingburst = 0;
      current.finishTime = timePassed;
      current.status = "terminated";
     }
     //update the remaining time in ioqueue
     if(ioqueue.size() != 0)
      totaliot ++;
     for(int index = 0; index < ioqueue.size(); index++) {
      ioqueue.get(index).doingio --;
      if(ioqueue.get(index).doingio == 0) { // if it finish its io process, remove it from ioqueue and add to queue. 
       Node n = ioqueue.remove(index);
       n.status = "ready";
       queue.add(n);
        index--;
      }
     }
     // if the burst period is over, remove the node from the queue and add it to the ioqueue
     if(current.remainingburst == 0 && current.remainingTime != 0) {
      queue.remove(0);
      ioburst = randomOS(current.io);
      current.doingio = ioburst;
      current.iotime += ioburst;
      current.status = "blocked";
      ioqueue.add(current);
     }
     //uppdate the time in before queue and add process to queue if the timePassed = a
     for(int index = 0; index < before.size(); index++) {
      before.get(index).timetoready --;
      if(before.get(index).timetoready == 0) {
       Node n = before.remove(index);
       n.status = "ready";
       queue.add(n);
       index --;
      }
     }
     //sort the queue by the order of least remaining time and least index.
     for(int a = 0; a < queue.size(); a++) {
      for(int b = a+1; b < queue.size(); b++) {
       if(queue.get(a).remainingTime > queue.get(b).remainingTime) {
        Node n = queue.get(a);
        queue.set(a, queue.get(b));
        queue.set(b, n);
       }
       if(queue.get(a).remainingTime == queue.get(b).remainingTime && queue.get(a).index > queue.get(b).index) {
        Node n = queue.get(a);
        queue.set(a, queue.get(b));
        queue.set(b, n);
       }
      }
     }
     // if the first process in the queue is not the process when the loop start, which means a process with higher priority came in, 
     // we stop the burst process of current processe and switch to the one with higher priority.
     if(queue.size() != 0 && queue.get(0).index != current.index) {
      if(current.remainingburst != 0) {
       current.status = "ready";
      }

      break;
     }
     
    }
   }
   else {// when ioqueue is not empty but queue is empty
    boolean breakflag = false; // if any of the process in the ioqueue finishes its io process, turn the flag to true and break from the loop
    
    
    
    for(int i = 0; i > -1; i ++) {
     totaliot ++;
     timePassed++;
     if(v) {
      if(timePassed < 10)
       System.out.print("Before cycle\t" + timePassed + ": ");
      if(timePassed >=10 && timePassed < 100)
       System.out.print("Before cycle   " + timePassed + ": ");
      if(timePassed >=100 && timePassed < 1000)
       System.out.print("Before cycle  " + timePassed + ": ");
      if(timePassed >=1000)
       System.out.print("Before cycle " + timePassed + ": ");
      for(int p = 0; p < start.size(); p++) {
       Node pp = start.get(p);
       if(pp.status.compareTo("running") == 0)
        System.out.print("    " + pp.status + "  " + pp.remainingburst);
       if(pp.status.compareTo("blocked") == 0)
        System.out.print("    " + pp.status + "  " + pp.doingio);
       if(pp.status.compareTo("ready") == 0)
        System.out.print("      " + pp.status + "  " + pp.remainingburst);
       if(pp.status.compareTo("unstarted") == 0)
        System.out.print("  " + pp.status + "  0");
       if(pp.status.compareTo("terminated") == 0)
        System.out.print(" " + pp.status + "  0");
      }
      System.out.println(".");
     }
     for(int j = 0; j < ioqueue.size(); j++) {
      Node n = ioqueue.get(j);
      n.doingio --;
      if(n.doingio == 0) {
       breakflag = true;
       ioqueue.remove(j);
       n.status = "ready";
       queue.add(n);
       j = j -1;
      }
     }
     
     if(breakflag)
      break;
    }
    //sort the queue
    for(int i = 0; i < queue.size(); i++) {
     for(int j = i+1; j < queue.size();j++) {
      if(queue.get(i).remainingTime > queue.get(j).remainingTime) {
       Node n = queue.get(i);
       queue.set(i, queue.get(j));
       queue.set(j, n);
      }
      if(queue.get(i).remainingTime == queue.get(j).remainingTime){
       if(queue.get(i).index > queue.get(j).index) {
        Node n = queue.get(i);
        queue.set(i, queue.get(j));
        queue.set(j, n);
       }
      }
     }
    }
    
   }
  }
  int twt = 0; //total waiting time
  int ttt = 0; // total turnaround time
  System.out.println("The scheduling algorithm used was Preemptive Shortest Job First\n"); 
  for(int i = 0; i < start.size(); i++) {
   Node n = start.get(i);
   System.out.println("Process " + n.index +":");
   System.out.println("\t(A,B,C,IO) = (" + n.a +","+ n.b +","+ n.c +"," + n.io +")");
   System.out.println("\tFinishing time: " + n.finishTime);
   System.out.println("\tTurnaround time: " + (n.finishTime - n.a));
   System.out.println("\tI/O time: " + n.iotime);
   System.out.println("\tWaiting time: " + n.waitingtime +"\n");
   twt += n.waitingtime;
   ttt+= (n.finishTime - n.a);
   
  }
  System.out.println("Summary Data:");
  System.out.println("\tFinishing time: " + timePassed);
  System.out.println("\tCPU Utilization: " + new DecimalFormat("#0.000000").format(1.0 * totalcput/timePassed));
  System.out.println("\tI/O Utilization: " + new DecimalFormat("#0.000000").format(1.0* totaliot/timePassed));
  System.out.println("\tThroughput: " + new DecimalFormat("#0.000000").format(start.size() / (timePassed/100.0)) + " processes per hundred cycles");
  System.out.println("\tAverage turnaround time: " + new DecimalFormat("#0.000000").format(ttt * 1.0 / start.size()));
  System.out.println("\tAverage waiting time: " + new DecimalFormat("#0.000000").format(twt * 1.0 / start.size()) + "\n");
  
  
 }
 
 public void unipro(boolean v) {
  
  boolean print = v;
  if(v) {
   System.out.println("This detailed printout gives the state and remaining burst for each process\n");
   System.out.print("Before cycle    0: ");
   for(int p = 0; p < start.size(); p++) {
    System.out.print("  unstarted  0");
   }
   System.out.println(".");
  }
  

  int timePassed = 0; // holds the overall time of the operation
  int cpuburst = 0; // this variable stores the cpu burst of each time.
  int ioburst = 0; // this variable stores the io time of each time
  int totalcput = 0; // this variable holds the total cpu burst time of each processes
  int totaliot = 0; // this variable holds the time when there is/are process(es) doing io
  ArrayList<Node> queue = new ArrayList<Node>(); // copy the list into the queue
  for(int i = 0; i < start.size(); i++) {
   Node n = start.get(i);
   if(n.a == 0)
    n.status = "ready";
   else
    n.status = "unstarted";
   queue.add(n);
  }
  // run the process on by one, in the sorted order
  for(int index = 0; index < queue.size(); index++){
   Node node = queue.get(index);
   while(node.remainingTime != 0) {
    cpuburst = randomOS(node.b);
    node.remainingburst = cpuburst;
    node.status = "running";
    if(cpuburst > node.remainingTime)
     cpuburst = node.remainingTime;
    
    for(int b = 0; b < cpuburst; b++) {
     
     
     timePassed ++;
     totalcput ++;
     
     if(v) {
      if(timePassed < 10)
       System.out.print("Before cycle\t" + timePassed + ": ");
      if(timePassed >=10 && timePassed < 100)
       System.out.print("Before cycle   " + timePassed + ": ");
      if(timePassed >=100 && timePassed < 1000)
       System.out.print("Before cycle  " + timePassed + ": ");
      if(timePassed >=1000)
       System.out.print("Before cycle " + timePassed + ": ");
      for(int p = 0; p < start.size(); p++) {
       Node pp = start.get(p);
       if(pp.status.compareTo("running") == 0)
        System.out.print("    " + pp.status + "  " + pp.remainingburst);
       if(pp.status.compareTo("blocked") == 0)
        System.out.print("    " + pp.status + "  " + pp.doingio);
       if(pp.status.compareTo("ready") == 0)
        System.out.print("      " + pp.status + "  " + pp.remainingburst);
       if(pp.status.compareTo("unstarted") == 0)
        System.out.print("  " + pp.status + "  0");
       if(pp.status.compareTo("terminated") == 0)
        System.out.print(" " + pp.status + "  0");
      }
      System.out.println(".");
     }
     node.remainingTime --;
     node.remainingburst --;
     //update the queue
     for(int i = index+1; i < queue.size(); i++) {
      Node n = queue.get(i);
      if(n.status.compareTo("ready") == 0)
       n.waitingtime ++;
      if(n.status.compareTo("unstarted") == 0) {
       n.timetoready --;
       if(n.timetoready == 0)
        n.status = "ready";
      }
     } 
     
    }
    if(node.remainingTime == 0) {
      node.finishTime = timePassed;
      node.status = "terminated";
      node.remainingburst = 0;
      ioburst = 0;
    }else {
     // goes into the io process and update the time
     ioburst = randomOS(node.io);
     node.iotime += ioburst;
     node.doingio = ioburst;
     node.status = "blocked";
     
     for(int b = 0; b < ioburst; b++) {
      timePassed ++;
      totaliot++;
      
      if(v) {
       if(timePassed < 10)
        System.out.print("Before cycle\t" + timePassed + ": ");
       if(timePassed >=10 && timePassed < 100)
        System.out.print("Before cycle   " + timePassed + ": ");
       if(timePassed >=100 && timePassed < 1000)
        System.out.print("Before cycle  " + timePassed + ": ");
       if(timePassed >=1000)
        System.out.print("Before cycle " + timePassed + ": ");
       for(int p = 0; p < start.size(); p++) {
        Node pp = start.get(p);
        if(pp.status.compareTo("running") == 0)
         System.out.print("    " + pp.status + "  " + pp.remainingburst);
        if(pp.status.compareTo("blocked") == 0)
         System.out.print("    " + pp.status + "  " + pp.doingio);
        if(pp.status.compareTo("ready") == 0)
         System.out.print("      " + pp.status + "  " + pp.remainingburst);
        if(pp.status.compareTo("unstarted") == 0)
         System.out.print("  " + pp.status + "  0");
        if(pp.status.compareTo("terminated") == 0)
         System.out.print(" " + pp.status + "  0");
       }
       System.out.println(".");
      }
      node.doingio -- ;
      
      for(int i = index+1; i < queue.size(); i++) {
       Node n = queue.get(i);
       if(n.status.compareTo("ready") == 0)
        n.waitingtime ++;
       if(n.status.compareTo("unstarted") == 0) {
        n.timetoready --;
        if(n.timetoready == 0)
         n.status = "ready";
       }
      } 
     }
    }
    
   }
  }
  System.out.println("The scheduling algorithm used was Uniprocessor\n");
  int twt = 0; //total waiting time
  int ttt = 0; // total turnaround time
  for(int i = 0; i < queue.size(); i++) {
   Node n = queue.get(i);
   System.out.println("Process " + n.index +":");
   System.out.println("\t(A,B,C,IO) = (" + n.a +","+ n.b +","+ n.c +"," + n.io +")");
   System.out.println("\tFinishing time: " + n.finishTime);
   System.out.println("\tTurnaround time: " + (n.finishTime - n.a));
   System.out.println("\tI/O time: " + n.iotime);
   System.out.println("\tWaiting time: " + n.waitingtime +"\n");
   twt += n.waitingtime;
   ttt+= (n.finishTime - n.a);
   
  }
  System.out.println("Summary Data:");
  System.out.println("\tFinishing time: " + timePassed);
  System.out.println("\tCPU Utilization: " + new DecimalFormat("#0.000000").format(1.0 * totalcput/timePassed));
  System.out.println("\tI/O Utilization: " + new DecimalFormat("#0.000000").format(1.0* totaliot/timePassed));
  System.out.println("\tThroughput: " + new DecimalFormat("#0.000000").format(queue.size() / (timePassed/100.0)) + " processes per hundred cycles");
  System.out.println("\tAverage turnaround time: " + new DecimalFormat("#0.000000").format(ttt * 1.0 / start.size()));
  System.out.println("\tAverage waiting time: " + new DecimalFormat("#0.000000").format(twt * 1.0 / start.size()) + "\n");
  
 }
 
 public int randomOS(int bound) {
  return 1+(Integer.parseInt(filereader.next()) % bound);
 }
 
 public void start(String s) {
  String filename = s;
  try {
   // initialize the file scanner
   Scanner scanner = new Scanner(new BufferedReader(new FileReader(filename)));
   filereader = new Scanner(new BufferedReader(new FileReader("random-numbers")));
  
   StringBuilder builder = new StringBuilder();
   int numprocess = Integer.parseInt(scanner.next());
   builder.append("The original input was: " + numprocess + " ");
   for(int i = 0; i < numprocess; i++) {
    builder.append(" ");
    Node node = new Node(Integer.parseInt(scanner.next()), Integer.parseInt(scanner.next()), Integer.parseInt(scanner.next()), Integer.parseInt(scanner.next()), i);
    start.add(node);
    builder.append(node.a + " " + node.b +" " + node.c +" "+node.io+" ");
   }
   // take the inputs and print the inputs
   System.out.println(builder.toString());
   builder = new StringBuilder();
   builder.append("The (sorted) input is:  " + numprocess + " ");
   // sort the inputs and print 
   for(int i = 0; i < numprocess; i ++) {
    for(int j = i+1; j < numprocess; j++) {
     if(start.get(i).a > start.get(j).a) {
      Node temp = start.get(i);
      start.set(i, start.get(j));
      start.set(j, temp);
     }
     if(start.get(i).a == start.get(j).a && start.get(i).index > start.get(j).index) {
      Node temp = start.get(i);
      start.set(i, start.get(j));
      start.set(j, temp);
     }
    }
    Node node = start.get(i);
    builder.append(" " + node.a + " " + node.b +" " + node.c +" "+node.io+" ");
   }
   for(int i = 0; i < start.size(); i++) {
    start.get(i).index = i;
   }
   System.out.println(builder.toString()+"\n");
   fcfs(verbose);
      rr(verbose);
   unipro(verbose);
   psjf(verbose);
  }catch(FileNotFoundException e) {
   System.out.println("File not found! Please check if random-numbers is in the directory or not.");
  }
 }
 // compare and sort the queue by the timeincrement. 
 public void sortqueue(ArrayList<Node> list) {
  if(list.size() > 1){
   for(int i = 0; i < list.size(); i++) {
    for(int j = i+1; j < list.size(); j++) {
     if(list.get(i).timeIncrement < list.get(j).timeIncrement) {
      Node temp = list.get(i);
      list.set(i, list.get(j));
      list.set(j, temp);
     }
     if(list.get(i).timeIncrement == list.get(j).timeIncrement) {
      if(list.get(i).index > list.get(j).index) {
       Node temp = list.get(i);
       list.set(i, list.get(j));
       list.set(j, temp);
      }
     }
    }
    
   }
  }
 }

 public static void main(String[] args) {
  Scheduling s = new Scheduling();
  if(args.length == 1) {
   s.verbose = false;
   s.start(args[0]);
  }
  else {
   s.verbose = true;
   s.start(args[1]);
  }
  
 }
 
 class Node{
  int index; // the index of the process. this is used to arrange priority
  int a;
  int b;
  int c;
  int io;
  String status;
  int doingio = 0; // the time left for current io process
  int finishTime; // store the time when the process finished all the running.
  int waitingtime = 0; // store the time that the process spend in ready queue
  int iotime; // store the total iotime of a process
  int remainingTime; // this counts how many cpu burst is still needed to complete the processe
  int timeIncrement = 0; /* this is a variable that hold the waiting time change.
                            for this variable, it is used in fcfs(), rr(), unipro(). Since in these three algorithm, I add all the burst time at the begining.
                            for example, if randomOS(b) gives 4, I add it to all the processes. if at that time, some processes in ioqueue have remaining io time 2,
                            then the timeincrement will be 2 since at the end of this burst, it has been waiting in the ready queue for 2s.
                            by comparing the timeincrement I can figure out who came in ready queue earlier. if the variable of two processes is same, compare their index.
  */
  int currentburst = 0; // this variable is used in rr to hold the remaining burst time for this round. the maximum value of this variable is 2.
  
  int remainingburst = 0; // this variable counts the remaining time for this current burst.
  int timetoready = 0; // this variable holds the time before the processe come in.
  
  public Node(int a, int b, int c, int io, int index) {
   this.a = a;
   this.b = b;
   this.c = c;
   this.remainingTime = c;
   this.io = io;
   this.index = index;
   this.timetoready = a;
  }
  
 }
}