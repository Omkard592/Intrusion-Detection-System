/**
 * ProcessRuleList.java --
 * Author          : Seokwon Yang
 * Created On      : Fri Jan  8 23:06:54 1999
 * Last Modified By: Seokwon Yang
 * Last Modified On: Fri Jan  8 23:06:56 1999
 * RCS             : $Id: header.el,v 1.1 1997/02/17 21:45:38 seyang Exp seyang $
 * Copyright (C) University of Florida 1999
 */

package sentinel.led;
import java.util.Vector;
import java.util.Enumeration;
import java.lang.reflect.*;
import java.awt.*;

/**
 *  The ProcessRuleList class denotes the list that is used to insert the rules
 *  triggered from an event. The rule scheduler traverses this rule list to execute
 *  the rules in the order of their priority and coupling modes. It contains four
 *  rule queues - the temporal rule queue,the immediate rule queue,
 *  and two deferred rule queues.
 */

public class ProcessRuleList {

  private static boolean ruleSchedulerDebug = Utilities.isDebugFlagTrue("ruleSchedulerDebug");

  TemporalRuleQueue temporalRuleQueue;
  ImmRuleQueue immRuleQueue;
  DefRuleQueue deffRuleQueueOne;
  DefRuleQueue deffRuleQueueTwo;

  Thread applThread;

  ProcessRuleList(Thread applThread) {
    this.applThread = applThread;
    temporalRuleQueue = new TemporalRuleQueue(applThread,"temporalRuleQueue");
    immRuleQueue = new ImmRuleQueue(applThread,"immRuleQueue");
    deffRuleQueueOne = new DefRuleQueue(applThread,"deffRuleQueueOne");
    deffRuleQueueTwo = new DefRuleQueue(applThread,"deffRulequeueTwo");
  }

  TemporalRuleQueue getTemporalRuleQueue() {
    return temporalRuleQueue;
  }


  ImmRuleQueue getImmRuleQueue() {
    return immRuleQueue;
  }

  DefRuleQueue getDeffRuleQueueOne() {
    return deffRuleQueueOne;
  }

  DefRuleQueue getDeffRuleQueueTwo() {
    return deffRuleQueueTwo;
  }
  // modified by wtanpisu on 24 Jan 2000

  void print(){
    if(ruleSchedulerDebug)
      System.out.println("Rule in TemporalRuleQueue");
    temporalRuleQueue.print();
    if(ruleSchedulerDebug)
      System.out.println("Rule in ImmRuleQueue");
    immRuleQueue.print();
    if(ruleSchedulerDebug)
      System.out.println("Rule in DefRuleQueue");
    deffRuleQueueOne.print();
    if(ruleSchedulerDebug)
      System.out.println("Rule in DefRuleQueueTwo");
    deffRuleQueueTwo.print();
  }

  /** This method adds a rule thread to the process rule list. This method inserts
    *  the rule thread into one of the three queues above depending on their parent
    *  thread and their coupling mode.
    */

  void addRuleThread(RuleThread ruleThread) {

    if (ruleSchedulerDebug){
      System.out.println("Display all the existing rule in the queue");
      print();
      System.out.print("Adding this following rule thread into the queue" );
      ruleThread.print();
    }


    CouplingMode coupling = ruleThread.getCoupling();
    Thread parent = ruleThread.getParent();

//    System.out.println("ProcessRuleList::addRuleThread");


    if (coupling.getId() == 0) {   // The rule's IMMEDIATE Coupling

      // Top-level transaction
      // if(parent == timerThread){
      if(parent instanceof Timer){
        temporalRuleQueue.insertRule(ruleThread);
        if (ruleSchedulerDebug)
          print();
      }

      /** If the parent is the thread generated from actionlistener by JVM
       *  Parent is instance of EventDispatchThread class
       */
      else if (parent.getClass().getName().equals("EventDispatchThread") ||
                parent == applThread ||
                parent.getName ().equals (Constant.LEDReceiverThreadName)) {

        immRuleQueue.insertRule(ruleThread);
        if (ruleSchedulerDebug)
          print();
      }

      // Nested transaction
      // when the parent is a rule thread resided in the temporal rule queue
      else if(temporalRuleQueue.isRuleThreadPresent(parent)){
        temporalRuleQueue.insertRule(ruleThread);
        if(ruleSchedulerDebug)
          print();
      }

      // when the parent is a rule thread resided in the immediate rule queue
      // the parent is not applThread
      else if (immRuleQueue.isRuleThreadPresent(parent)) {
        immRuleQueue.insertRule(ruleThread);
        if (ruleSchedulerDebug)
          print();
      }

      // when the parent is a rule thread and it's not in imm rule queue
      // add rule in deferred rule queue
      else {
        if(parent instanceof RuleThread){
          //System.out.println("its parent is rule thread");
          RuleThread TempRuleThread = (RuleThread)parent;
          if(TempRuleThread.getCoupling() == CouplingMode.DEFERRED )  {// parent is a deferred rule.
            deffRuleQueueOne.insertRule(ruleThread);
            if (ruleSchedulerDebug)
              print();
          }
          else{
            System.out.println("ERROR Can't add this rule into the rulequeue !!");
          }
        }
        else{
          System.out.println("ERROR Can't add this rule into the rulequeue!");
        }
      }
    }

    // Insert the rule with the deferred coupling mode into the rule queue.
    else if (coupling.getId() == 1) {	//The rule's DEFERRED COUPLING

      // Top-level transaction
      if(parent instanceof Timer){
        temporalRuleQueue.insertRule(ruleThread);
        if (ruleSchedulerDebug)
          print();
      }

      /** If the parent is the thread generated from actionlistener by JVM
       *  Parent is instance of EventDispatchThread class
       */
      else if (parent.getClass().getName().equals("EventDispatchThread") ||
              parent == applThread ||
              parent.getName ().equals (Constant.LEDReceiverThreadName)){
	deffRuleQueueOne.insertRule(ruleThread);
	if (ruleSchedulerDebug)
	  print();
      }
      else if(temporalRuleQueue.isRuleThreadPresent(parent)){
        temporalRuleQueue.insertRule(ruleThread);
        if(ruleSchedulerDebug)
          print();
      }
      else if (immRuleQueue.isRuleThreadPresent(parent)){
	deffRuleQueueOne.insertRule(ruleThread);
	if (ruleSchedulerDebug)
	  print();
      }
      else if (deffRuleQueueOne.isRuleThreadPresent(parent)){
	deffRuleQueueTwo.insertRule(ruleThread);
	if (ruleSchedulerDebug)
	  print();
      }
      else{
	System.out.println("ERROR Can't add this rule into the rulequeue");
      }
    }
  }


  void deleteRuleThread(RuleThread rt, RuleQueue ruleQueue) {
    if(ruleQueue.getRuleQueType() == "immRuleQueue"){
      if(ruleSchedulerDebug){
        System.out.println("Delete rule from IMM rule queue");
        print();
      }
      ((ImmRuleQueue)ruleQueue).deleteRuleThread(rt);
    }
    else if(ruleQueue.getRuleQueType() == "temporalRuleQueue"){
      if(ruleSchedulerDebug){
	    System.out.println("Delete rule from temp rule queue");
        print();
      }
      temporalRuleQueue.deleteRuleThread(rt);
    }
    else if (ruleQueue.getRuleQueType() == "deffRuleQueueOne"){
      deffRuleQueueOne.deleteRuleThread(rt);
      if( deffRuleQueueOne.getHead() == null){
	if(ruleSchedulerDebug)
	  System.out.println("Put the rules in Def QueueTwo in QueueOne");
        deffRuleQueueOne.head = deffRuleQueueTwo.head;
        deffRuleQueueTwo =  new DefRuleQueue(applThread,"deffRulequeueTwo");
      }
    }
   }


  void waitForImmRules() {
    // if the current thread is a rule thread in Temporoal
    if(Thread.currentThread() instanceof RuleThread && temporalRuleQueue.isRuleThreadPresent(Thread.currentThread())){
        temporalRuleQueue.joinRuleThreads ();
    }
    else if (deffRuleQueueOne.isRuleThreadPresent(Thread.currentThread())){
       // if the parent is in the deferred rule list
      deffRuleQueueOne.joinRuleThreads();
    }
    else{
      immRuleQueue.joinRuleThreads();
    }
  }

   // added by wtanpisu on 24 Jan 2000
}
