Lab2 Scheduling

To run the program,
(1) complile it using "javac Scheduling.java"
(2) before running the program, make sure that random numbers and inut files are in the 
same directory as the java file
(3) before running the program, first open it, go to start(boolean b) method, fing the 
consecutive line 
fcfs(verbose);
rr(verbose)
uni(verbose)
psjf(verbose)

in the bottom part
uncomment the method which you are going to grade, and common all the other three.
(4) type "java Scheduling input-1" to test the input-1
(5) type "java Scheduling --verbose input-1" to test the input-1 with verbose mode
(6) please read the note part in the java file. Thank you!

Comments:
-3: Your program when run gets stuck while printing PSJF. Your program should be able to run without having to comment any lines (even though it is specified in the README, I shouldn't need to change and recompile your program to get the outputs). Also, the results of all the algorithms were supposed to be printed one after the other in the order specified
-1: The verbose outputs for some of the test cases for RR have incorrect burst values printed when the process terminates.
