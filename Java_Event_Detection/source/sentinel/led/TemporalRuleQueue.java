/**
 * TemporalRuleQueue.java --
 * Author          : Seokwon Yang
 * Created On      : Fri Jan  8 23:06:54 1999
 * Last Modified By: Seokwon Yang
 * Last Modified On: Fri Jan  8 23:06:56 1999
 * RCS             : $Id: header.el,v 1.1 1997/02/17 21:45:38 seyang Exp seyang $
 * Copyright (C) University of Florida 1999
 */

package sentinel.led;

/** The ProcessRuleList class denotes the list that is used to insert the rules
 *  triggered from an event. The rule scheduler traverses this rule list to execute
 *  the rules in the order of their priority and coupling modes. It contains three
 *  rule queues - the immediate rule queue, and two deferred rule queues.
 */
class TemporalRuleQueue extends RuleQueue {

  private static boolean ruleSchedulerDebug = Utilities.isDebugFlagTrue("ruleSchedulerDebug");


  TemporalRuleQueue(Thread applThread, String ruleQueueType){
    super(applThread,ruleQueueType);
  }

  protected synchronized void insertRule(RuleThread newRuleThrd){

    RuleThread prevThread;
    RuleThread currntThrd = null;
    int newRulePriority = newRuleThrd.getRulePriority();
    Thread parent = newRuleThrd.getParent();

    if (ruleSchedulerDebug) {
      System.out.print("Inserting rule thread");
      newRuleThrd.print();
      System.out.println("rulePriority = " + newRulePriority);
      System.out.println("Its parent is " +parent.getName());
    }

    //If the parent of new rule is a Timer Thread, treat it as a top level rule.
    if(parent != applThread && (!(parent instanceof RuleThread))){
      // System.out.print("Its parent is the timer thread");
      // when queue is empty, insert at head
      if (head == null){
        head = newRuleThrd;
        head.next = null;
        return;
      }
      currntThrd = head;
        // No priority order for rule thread spawn from Timer thread
      // Put the new rule at end of queue.
      if(head.next == null){
        currntThrd.next = newRuleThrd;
        newRuleThrd.next = null;
      }
      else{
        while(currntThrd.next != null){
          currntThrd = currntThrd.next;
        }
        currntThrd.next = newRuleThrd;
        newRuleThrd.next = null;
      }
      return;
    }

    //If the parent of new rule is a Rule Thread, fine the right place after its parent
    else if( parent instanceof RuleThread){

    // NEED TO CHECK WHETHER WE NEED TO TAKE CARE OF COUPLING MODE FOR THIS RULE
    // NOW WE ASSUME THAT IT HAS IMM COUPLING MODE
      currntThrd = head;
      prevThread = currntThrd;

      // need to find the right spot which is after its parent
      while (currntThrd != null){
        if(currntThrd == parent){
	  if (ruleSchedulerDebug){
	    System.out.println("found the parent");
            System.out.println("change the mode to be wait");
          }

          // change parent's operating mode to be WAIT
	  parent = (RuleThread) newRuleThrd.getParent();
          //if(a.getOperatingMode()==RuleOperatingMode.READY)
          ((RuleThread) parent).setOperatingMode(RuleOperatingMode.WAIT);

          if(currntThrd.next == null){
            currntThrd.next = newRuleThrd;
            newRuleThrd.next = null      ;
            return;
          }
          else{
            prevThread = currntThrd;
            currntThrd = currntThrd.next;

	    if ( newRuleThrd.getCoupling().getId() ==0){ // IMMEDIATE COUPLING
  	      // When a rule has imm coupling, it need to put right after its parent
              while(currntThrd != null && currntThrd.getParent() == parent){
                // finding the place in the queue, based on priority
              	if(newRulePriority > currntThrd.getRulePriority()){
		  newRuleThrd.next = currntThrd;
  		  prevThread.next	= newRuleThrd;
                  return;
                }
                prevThread = currntThrd;
    		currntThrd = currntThrd.next;
              }
              newRuleThrd.next = currntThrd;
              prevThread.next	= newRuleThrd;
              return;
            }
          }
        }

        prevThread = currntThrd;
	currntThrd = currntThrd.next;
      }
    }
    else{
      if (ruleSchedulerDebug)
        System.out.println("Alert Design ERROR!!!!");
    }
  }


  void joinRuleThreads() {
    RuleThread currntThrd = head;
    while(currntThrd  != null){
      if(currntThrd == Thread.currentThread()){
        if(currntThrd.next != null && currntThrd.next.getParent() == Thread.currentThread()){
	  currntThrd = currntThrd.next;
          print();
	  try{
     	    currntThrd.isStart();
          }
 	  catch(Exception ie){
            ie.printStackTrace();
	  }
  	  try{
            if (ruleSchedulerDebug)
     	      System.out.println(currntThrd.getName() + "joins");
	    currntThrd.join();
            if (ruleSchedulerDebug)
              System.out.println("finish join");
          }
  	  catch(InterruptedException ie){
  	    ie.printStackTrace();
	  }
  	  currntThrd = currntThrd.next;
  	  if( currntThrd == null){
             return;
          }
	  else{
            if (ruleSchedulerDebug)
      	      System.out.println(currntThrd.getName());
          }


	  while(currntThrd != null && currntThrd.getParent() == Thread.currentThread()){
            try{
	      currntThrd.isStart();
              System.out.println("rule already started");
            }
            catch(Exception ie){
  	      ie.printStackTrace();
            }
            try{
//	      System.out.println("rule before joing");
              currntThrd.join();
//	      System.out.println("finish ready join");
            }
	    catch(InterruptedException ie){
  	      ie.printStackTrace();
	    }
	    currntThrd = currntThrd.next;
	  }
          return;
        }
      }
      currntThrd = currntThrd.next  ;
    }
  }
}
