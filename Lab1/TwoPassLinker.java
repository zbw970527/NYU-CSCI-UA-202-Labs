import java.io.*;
//import java.io.InoputStreamReader;
//import java.io.BufferedReader;
import java.util.*;

public class TwoPassLinker{
  
  ArrayList<DataNode> dataList;
  
  //determines the base address for each module and 
  //produces a symbol table containing the absolute address for each defined symbol.
  public void passOne(){
    BufferedReader reader = null;
    try{
      reader = new BufferedReader(new InputStreamReader(System.in));
      String s = reader.readLine();
      int numberModules = Integer.parseInt(s);
      int relative = 0;  // store the difference between relative address and actual address
      ArrayList<Node> symbolList = new ArrayList<Node>();
      ArrayList<DataNode> dataList = new ArrayList<DataNode>();
      String[] part = null;
      String[] useList = null;
      for(int i = 0; i < numberModules; i++){
        s = reader.readLine();
        part = s.split(" ");
        if(part[0] != "0"){
          for(int index = 1; index < part.length; index= index + 1){
            if(part[index].compareTo("") != 0){
              symbolList.add(new Node(part[index], Integer.parseInt(part[index+1]) + relative)); // build up the symbol list.
              index = index + 1;
            }
          }
        }
        s = reader.readLine();
        useList = s.split(" ");
        s = reader.readLine();
        part = s.split(" ");
        relative = relative + Integer.parseInt(part[0]);
        for(int j = 1; j < part.length; j = j + 1){
          if(part[j].compareTo("") != 0){
            if(part[j].charAt(0) > 65 && part[j].charAt(0) < 90){
              if(part[j].charAt(0) == 'E'){
                try{
                  dataList.add(new DataNode("data", Integer.parseInt(part[j+1]), part[j], null, useList[Integer.parseInt(part[j+1]) % 1000])); //String name, int value, String type, int relativeAddress
                }catch(ArrayIndexOutOfBoundsException e){
                  dataList.add(new DataNode("data", Integer.parseInt(part[j+1]), "I", null, null));
                }
                j = j + 1;
              }
            }
            
          }
        }
      }
      System.out.println("Symbol Table");
      for(int index = 0; index < symbolList.size(); index ++){
        Node node = symbolList.get(index);
        System.out.println(node.name + "=" + node.value);
      }
    }catch(IOException e){
      System.out.println("input error!");
    }
  }
  
  //uses the base addresses and the symbol table computed in pass one to generate the actual output by relocating
  //relative addresses and resolving external references. 
  public void passTwo(){
    
  }
  
  public static void main(String[] args){
    TwoPassLinker linker = new TwoPassLinker();
    linker.passOne();
  }
}

 class Node{
  String name;
  int value;
  
  public Node(String name, int value){
    this.name = name;
    this.value = value;
  }
}
 
 class DataNode extends Node {
   // guide points to the key

   String type;
   int relativeAddress;
   String symbolUse;
   
   public DataNode(String name, int value, String type, int relativeAddress, String symbolUse){
     super(name, value);
     this.type = type;
     this.relativeAddress = relativeAddress;
     this.symbolUse = symbolUse;
   }
}