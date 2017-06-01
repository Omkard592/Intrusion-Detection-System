/**
 * RuleQueue.java --
 * Author          : Seokwon Yang
 * Created On      : Fri Jan  8 23:06:54 1999
 * Last Modified By: Seokwon Yang
 * Last Modified On: Fri Jan  8 23:06:56 1999
 * RCS             : $Id: header.el,v 1.1 1997/02/17 21:45:38 seyang Exp seyang $
 * Copyright (C) University of Florida 1999
 */

package sentinel.led;


/** The RuleQueue class is used to store rule threads in a linked list. A RuleQueue
 *  instance is a linked list of rule threads. The rule queues temporalRuleQueue,
 *  immediateRuleQueue, deferredRuleQueueOne and deferredRuleQueueTwo used by
 *  the scheduler are instances of RuleQueue.
 *  The RuleQueue contains a reference to the application thread in order to keep track
 *  of the parent of top-level rules (the rules that are triggered by events raised
 *  from the application thread).
 */

abstract class  RuleQueue {
  RuleThread head;
  Thread applThread;

  // added by wtanpisu
  // rational : to name the type of the rule queue e.g. immRuleQueue
  String ruleQueueType;
  // added by wtanpisu on 24 Jan 2000

  private boolean ruleSchedulerDebug = Utilities.isDebugFlagTrue("ruleSchedulerDebug");

  RuleQueue(Thread applThread, String ruleQueueType) {
     this.applThread = applThread;
     this.ruleQueueType = ruleQueueType;
     head = null;
  }

  // added by wtanpisu
  // rational : to name the type of the rule queue e.g. immRuleQueue
  public String getRuleQueType(){
    return ruleQueueType;
  }
  // added by wtanpisu on 24 Jan 2000


   /** This method inserts a rule thread in a rule queue based on its priority and
    *  parent. The rule threads are inserted in decreasing order of their priority.
    *  If the rule is a top level rule (parent is application thread), it is placed in
    *  the order of its priority.
    *  If the rule is not a top level rule (parent is some other rule thread, not
    *  application thread), the parent rule is searched in the rule queue and the
    *  rule thread is placed next to it.
    *  The method need to be synchonized to prevent consistency prolem
    */

  // changed by wtanpisu
  // rational : to utilize the rule scheduler
  protected abstract void insertRule(RuleThread newRuleThd);
  // added by wtanpisu on 24 Jan 2000


  /** This method is used to search for the given thread in this rule queue.
    */
  RuleThread findRuleThread(Thread rt) {
    RuleThread tempThread = head;
    String currName = rt.getName();
    while (tempThread != null) {
      if (tempThread.getName().equals(currName))
	return tempThread;
      else
	tempThread = tempThread.next;
    }
    return null;
  }

  /** This method sets a rule thread to the FINISHED rule operating mode. This
   *  method is called when the rule thread finishes execution.
   */
  void setRuleThreadToFinish(RuleThread rt) {
    String currRuleName = rt.getName();
    if (ruleSchedulerDebug)
      System.out.println("Setting " + currRuleName + " to FINISHED");
      RuleThread tempThread;
      for (tempThread = head; tempThread != null; tempThread = tempThread.next) {
	if (tempThread.getName().equals(currRuleName)) {
	  System.out.println("Setting " + tempThread.getName() + " to FINISHED");
	  tempThread.setOperatingMode(RuleOperatingMode.FINISHED);
	}
      }
   }

   /** This method deletes the given rule thread from this rule queue.
    */
  void deleteRuleThread(RuleThread ruleThrd) {
    if (ruleSchedulerDebug)
      System.out.println("Deleting rule rule thread");
    if(head == null){
      return;
    }
    RuleThread tempThread;
    if(ruleThrd!=null){
      String currName = ruleThrd.getName();
      if( ruleThrd == head){
        head = ruleThrd.next;
	return;
      }
      if (ruleSchedulerDebug)
        System.out.println("Deleting rule " + currName);
      for (tempThread = head; tempThread != null; tempThread = tempThread.next) {
	if (tempThread.next == ruleThrd) {
	  tempThread.next = tempThread.next.next;
          break;
        }
      }
    }
    return;
  }

   /** This method adds a rule thread to the rule queue. Unlike the insert method,
    *  this method does not cosider priority or parent while adding the rule.
    *  It is private method that is used in the getHighestPriorityRules() method.
    */
   private void add(RuleThread rt) {
    if (head == null) {
      head = rt;
      return;
    }
    RuleThread tempThread = head;
    while (tempThread.next != null)
      tempThread = tempThread.next;
    tempThread.next = rt;
  }

   /** This method returns true if the given rule thread is present in the rule
    *  queue and false otherwise.
    */
  boolean isRuleThreadPresent(Thread rt) {
    RuleThread tempThread = head;
    String currName = rt.getName();
    while (tempThread != null) {
      if (tempThread.getName().equals(currName))
	return true;
      else
	tempThread = tempThread.next;
      }
    return false;
  }

  /** This method waits until all the rule threads in this rule queue finished
   *  execution and removed from the rule queue. This method is called from the
   *  waitForImmediateRules() method from the notifyEvent() method.
   */
  // changeded by wtanpisu
  // rational : an old version doesn't work properly

  void joinRuleThreads() {
    RuleThread tempThread = head;
    while(tempThread  != null){
      //If the current thread is timer thread
      if((Thread.currentThread() != applThread) && (!(Thread.currentThread() instanceof RuleThread))){
        if( (tempThread.getParent() != applThread) && (!(tempThread.getParent() instanceof RuleThread)) && tempThread != null){
  	  System.out.println(Thread.currentThread().getName()+" name is this thread in join rule trhead");
    	  print();
  	  try{
	    tempThread.isStart();
	    System.out.println("rule already started");
          }
          catch(Exception ie){
	    ie.printStackTrace();
          }
          try{
//	    System.out.println(tempThread.getName()+" joins");
            tempThread.join();
//	    System.out.println("finish join");
	  }
	  catch(InterruptedException ie){
	    ie.printStackTrace();
	  }
	  tempThread = tempThread.next;
        }
	else {
	  if(tempThread != null){
	    tempThread = tempThread.next;
	  }
	}
      }
      else if(Thread.currentThread() == applThread ){
	if(tempThread.getParent() == applThread && tempThread != null){
	  try{
      	    tempThread.isStart();
    	    tempThread.join();
          }
	  catch(Exception ie){
            ie.printStackTrace();
    	  }
      	  tempThread = tempThread.next;
        }
        else {
	  if(tempThread != null){
  	    tempThread = tempThread.next;
          }
        }
      }
      else if(tempThread != null && tempThread == Thread.currentThread()){
        if(tempThread.next != null && tempThread.next.getParent() == Thread.currentThread()){
	  tempThread = tempThread.next;
	  print();
    	  try{
  	    tempThread.isStart();
    	  }
    	  catch(Exception ie){
    	    ie.printStackTrace();
    	  }
      	  try{
 //           System.out.println(tempThread.getName()+" joins");
            tempThread.join();
//    	    System.out.println("finish join");
          }
    	  catch(InterruptedException ie){
    	    ie.printStackTrace();
    	  }
    	  tempThread = tempThread.next;
    	  if( tempThread == null){
    	 //   System.out.println("it's null");
         }
    	  else
    	    System.out.println(tempThread.getName());

          while(tempThread != null && tempThread.getParent() == Thread.currentThread()){
    	    try{
//    	      System.out.println("rule already is not started"+tempThread.getName());
              tempThread.isStart();
//              System.out.println("rule already started");
            }
	    catch(Exception ie){
              ie.printStackTrace();
            }
    	    try{
//    	      System.out.println("rule before joing");
              tempThread.join();
//              System.out.println("finish ready join");
            }
    	    catch(InterruptedException ie){
  	      ie.printStackTrace();
            }
            tempThread = tempThread.next;
	  }
	}
	else{// wait for itself to finish when there is no child
          tempThread = tempThread.next;
        }
      }
      else
        tempThread = tempThread.next;
    }
    if (ruleSchedulerDebug)
      System.out.println(Thread.currentThread().getName()+" finish join operation ");
  }

  // added by wtanpisu on 24 Jan 2000
  /** This method returns the number of rule threads present in this rule queue.
   */
  int size() {
    int size = 0;
    RuleThread tempThread = head;
    for (;tempThread != null; tempThread = tempThread.next,size++);

    return size;
  }

  RuleThread deleteHead() {
    if (head == null) return null;
    RuleThread result = new RuleThread();
    result = head;
    head = head.next;
    result.next = null;
    return result;
  }

  boolean isEmpty() {
    return (head == null);
  }

  RuleThread getHead() {
    return head;
  }

  void print() {
    if (head == null) {
      if (ruleSchedulerDebug)
        System.out.println("Rule Queue is empty");
      return;
    }
    RuleThread tempThread = head;
    System.out.println();
    while (tempThread != null) {
      tempThread.print();
      tempThread = tempThread.next;
    }
    System.out.println();
  }
}

