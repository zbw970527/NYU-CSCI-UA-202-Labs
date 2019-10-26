import java.io.*;
import java.util.*;

/*Bowen Zhang
  bz896
  Operatiing System Project 1
  */

public class Linker{
  
 //this variable saves all the defined symbol
 ArrayList<Node> symbolList = new ArrayList<Node>();
 
 //this variable saves all the datas that will be resolved or relocated or done nothing
    ArrayList<DataNode> dataList = new ArrayList<DataNode>();
    
    //this variable saves all the use list of each module. 
    String[][] useList;
    
    //this variable saves the number of modules in total.
    int numberModules;
    
    // this is used to save uselists for later check(if the uselist elements in the module is used,
    // it will be marked as true here;)
 boolean[][] useListUsage; 
 
 
  //this method determines the base address for each module and 
  //produces a symbol table containing the absolute address for each defined symbol.
  public void passOne(){
      Scanner scanner = null;
    
      scanner = new Scanner(new BufferedReader(new InputStreamReader(System.in)));
      String s = scanner.next();
      numberModules = Integer.parseInt(s);
      int relative = 0;  // store the difference between relative address and actual address
      int startpoint = 0; // this variable marks the start of the symbolList that will be checked in the loop for def exceed
      useList = new String[numberModules][];
      useListUsage = new boolean[numberModules][];
      
      for(int index = 0; index < numberModules; index++){
        int num = Integer.parseInt(scanner.next()); // the total number of each definition, uselist, and datalist(the fore most number in each line)
        // save the defiinition list
        for(int i = 0; i < num; i ++){
         Node node = new Node(scanner.next(), Integer.parseInt(scanner.next()) + relative, index); // build up the symbol list. Node  (name, value,module)
         boolean repeat = false; // flag variable. if the symbol is duplicated, this will be set to true, and the node will not be added into the list
         
         //this loop goes over the saved symbollist and check if the newest symbol
         //is duplicated or not
         for(int x = 0; x < symbolList.size(); x++) {
          if((node.name).compareTo(symbolList.get(x).name) == 0) {
           symbolList.get(x).duplicated = true;
           repeat = true;
          }
         }
         if(repeat == false)
          symbolList.add(node);
        }
        
        // save the use list
        num = Integer.parseInt(scanner.next()); // this variable is the total number of elements in the use list of this module
        // create an array 
        useList[index] = new String[num];
        useListUsage[index] = new boolean[num];
        
        for(int i = 0; i < num; i++){
          useList[index][i] = scanner.next();
        }
        // save the datalist and add the relative
        num = Integer.parseInt(scanner.next());
        for(int i = 0; i < num; i ++){
          
          dataList.add(new DataNode(scanner.next(), Integer.parseInt(scanner.next()), index, relative, num));
        }
        //check if the def exceeds the module or not.
        for(int j = startpoint; j < symbolList.size();j++) {
         Node node = symbolList.get(j);
         if((node.value - relative) > num) {
          node.outofbounds = true;
          node.value = 0 + relative;
         }
        }
        startpoint = startpoint + symbolList.size();//update startpoint for next module
        relative = relative + num; //update the relative address for next module
      }
      
      System.out.println("Symbol Table");
      for(int index = 0; index < symbolList.size(); index ++){
        Node node = symbolList.get(index);
        if(node.duplicated == false)
         System.out.println(node.name + "=" + node.value);
        else
         System.out.println(node.name + "=" + node.value + " Error: This variable is multiply defined; first value used.");
      }
          scanner.close();
    }
  
  //uses the base addresses and the symbol table computed in pass one to generate the actual output by relocating
  //relative addresses and resolving external references. 
  public void passTwo(){
   
 System.out.println();
 System.out.println("Memory Map");
 
    for(int i = 0; i < dataList.size(); i++) {
     DataNode node = dataList.get(i);
     
     switch(node.name) {
      case "A":{
       //Error check: If an absolute address exceeds the size of the machine, 
       //print an error message and use the value zero
        if(node.value % 1000 >= 200) {
         if(i <= 9) 
          System.out.println(i + ":  " + (node.value)/1000*1000 + " Error: Absolute address exceeds machine size; zero used.");
         else
          System.out.println(i + ": " + (node.value)/1000*1000 + " Error: Absolute address exceeds machine size; zero used.");
        }
        else {
         if(i <= 9)
          System.out.println(i + ":  " + node.value);
         else
          System.out.println(i + ": " + node.value);
        }
        break;
       }
      case "I":{
       if(i <= 9)
        System.out.println(i + ":  " + node.value);
       else
        System.out.println(i + ": " + node.value);
       break;
      }
      case "R":{
       if(node.value % 1000 > node.moduleSize) {
        //Error check: Ifarelative address exceeds the size of the module, 
        //print an error message and use the value zero (absolute)
        if(i <= 9)
         System.out.println(i + ":  " + (node.value)/1000*1000 + " Error: Relative address exceeds module size; zero used.");
        else
         System.out.println(i + ": " + (node.value)/1000*1000 + " Error: Relative address exceeds module size; zero used.");
       }
       else {
        if(i <= 9)
         System.out.println(i + ":  " + (node.value + node.relativeAddress));
        else
         System.out.println(i + ": " + (node.value + node.relativeAddress));
       }
       break;
      }
      case "E":{
       int entry = node.value % 1000;
       //Error check: If an external address is too large to reference an entry in the use list, 
       //print an error message and treat the address as immediate
       if(entry >= useList[node.module].length) {
        if(i <= 9)
         System.out.println(i + ":  " + node.value + " Error: External address exceeds length of use list; treated as immediate.");
        else
         System.out.println(i + ": " + node.value + " Error: External address exceeds length of use list; treated as immediate.");
       }
       else {
        String s = useList[node.module][entry]; // store the name of definition used.
        Node def = null;
        //check if the definition used is defined and saved in symbolList or not;
        for(int index = 0; index < symbolList.size(); index++) {
         def = symbolList.get(index);
         if((def.name).compareTo(s) == 0) {
          //mark the useage true
          useListUsage[node.module][entry] = true;
          break;
         }
         else
          def = null;
        }
        //Error check: Ifasymbol is used but not defined, 
        //print an error message and use the value zero
        if(def == null) {
         // if a symbol is not defined, also mark the symbol in the useList as used.
         useListUsage[node.module][entry] = true;
         if(i <= 9)
       System.out.println(i + ":  " + node.value / 1000 * 1000 + " Error: "+ s +" is not defined; zero used");
      else
       System.out.println(i + ": " + node.value / 1000 * 1000 + " Error: "+ s +" is not defined; zero used");
        }
        else {
         if(def.outofbounds == false) {
          def.accessed = true;
          if(i <= 9)
           System.out.println(i + ":  " + (node.value / 1000 * 1000 + def.value));
          else
           System.out.println(i + ": " + (node.value / 1000 * 1000 + def.value));
         }
         else {
          if(i <= 9)
           System.out.println(i + ":  " + (node.value / 1000 * 1000 + def.value));
          else
           System.out.println(i + ": " + (node.value / 1000 * 1000 + def.value));
         }
        }
       }
      }
     }
     
    }
    System.out.println();
    //Error check: if the symbol is defined but not used, print a warning message.
    for(int i = 0; i < symbolList.size(); i ++) {
     Node n = symbolList.get(i);
     if(n.accessed == false) {
      System.out.println("Warning: " + n.name + " was defined in module " + n.module + " but never used.\n");
     }
    }
    
    //Error check: Ifasymbol appears in a use list but it not actually used in the module 
    //(i.e., not referred to in an E-type address), print a warning message
    for(int x = 0; x < useListUsage.length; x++) {
     for(int y = 0; y < useListUsage[x].length; y++) {
      if(useListUsage[x][y] == false) {
       
       System.out.println("Warning: In module " + x +" "+ useList[x][y] +" appeared in the use list but was not actually used.\n");
      }
     }
    }
    
    //Error check: If an address appearing in a definition exceeds the size of the module, 
    //print an error message and treat the address as 0 (relative)
    for(int i = 0; i < symbolList.size(); i++) {
     Node n = symbolList.get(i);
     if(n.outofbounds == true)
      System.out.println("Error: In module "+ n.module +" the def of "+ n.name +" exceeds the module size; zero (relative) used.\n");
    }
  }
  
  public static void main(String[] args){
    Linker linker = new Linker();
    linker.passOne();
    linker.passTwo();
  }
}

 class Node{
  String name; //variable indicates whether the name(for symbols) or type(for datas)
  int value; //variable indicates whether the value of symbol or the original 4-digit value for data.
  int module; //variable indicates the number of module that the node is in
  boolean accessed = false; //variable indicates whether the symbol is accessed later or not.
  boolean outofbounds = false; // variable indicates whether the symbol is outofbounds or not
  boolean duplicated = false; // variable indicates whether the symbol is duplicated or not
  
  public Node(String name, int value, int module){
    this.name = name;
    this.value = value;
    this.module = module;
  }
}
 
 class DataNode extends Node {
   
   int relativeAddress; //variables stores the difference bewteen actual address and relative address
   int moduleSize; // save the size of the use list of the module that the data belongs to 
    // save the number of module that the data is located
   String symbolUse;
   
   public DataNode(String name, int value, int module, int relativeAddress, int moduleSize){
     super(name, value, module);
     this.relativeAddress = relativeAddress;
     this.moduleSize = moduleSize;
   }
}