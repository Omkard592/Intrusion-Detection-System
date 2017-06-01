/**
 * DefRuleQueue.java --
 * Author          : Seokwon Yang, H. Kim
 * Created On      : Jan ?? 1999
 * Last Modified By: Seokwon Yang
 * Last Modified On: Sun Sep 19 23:25:10 1999
 * Copyright (C) University of Florida 1999
 */


package sentinel.led;

/**
 * The DefRuleQueue class is used to store deferred rule threads in a linked list.
 */
class DefRuleQueue extends ExplicitRuleQueue {

  private static boolean ruleSchedulerDebug = Utilities.isDebugFlagTrue("ruleSchedulerDebug");

  DefRuleQueue (Thread applThread, String ruleQueueType){
    super(applThread,ruleQueueType);
  }

  protected synchronized void insertRule(RuleThread newRuleThd){
    RuleThread tempThread;
    RuleThread currThread;
    RuleThread prevThread;

    int newRulePriority = newRuleThd.getRulePriority();
    Thread parent = newRuleThd.getParent();

    if (ruleSchedulerDebug) {
      System.out.println("Inserting this rule thread ");
      newRuleThd.print();
      System.out.println("Its Priority = " + newRulePriority);
      System.out.println("Its parent = " +parent.getName());
    }

    // when queue is empty, insert at head
    if (head == null) {
      head = newRuleThd;
      head.next = null;
      return;
    }

    // When the parent of new rule thread is the top level rule,
    // the placement of rules depends on its priority.
    if (parent == applThread) {
      if (ruleSchedulerDebug)
	System.out.println("This is a top level rule");
	// If the new rule has higher priority
      if (head.getRulePriority() < newRulePriority  ) {
	newRuleThd.next = head;
	head = newRuleThd;
	return;
      }
      tempThread = head;

      // Looping to find the right place for this new rule
      while (tempThread.next != null)	{
	if ( tempThread.next.getRulePriority() >= newRulePriority)
	  tempThread = tempThread.next;
	else {
	  if(tempThread == tempThread.next.getParent() ||
                (tempThread.getParent() instanceof RuleThread &&
                tempThread.next.getParent() instanceof RuleThread)){

            tempThread = tempThread.next;
	  }else{
  	    newRuleThd.next = tempThread.next;
  	    tempThread.next	= newRuleThd;
  	    return;
	  }
	}
      }

      // at the end of list
      if(tempThread.next == null){
	tempThread.next	= newRuleThd;
	newRuleThd.next = null;
      }
      return;
    }

    // If the parent of the new rule thread is NOT top level rule,
    // the placement of rules depends on its priority, its coupling mode,
    // and its parent.
    else{
      currThread = head;
      prevThread = currThread;

      // when rule has imm coupling mode
      if (newRuleThd.getCoupling ().getId() == 0){

        // need to find the right spot which is after its parent
	while (currThread != null){
	  if(currThread == parent){
      	    if (ruleSchedulerDebug){
	      System.out.println("found the parent");
              System.out.println("change the mode to be wait");
            }

            ((RuleThread) newRuleThd.getParent()).setOperatingMode(RuleOperatingMode.WAIT);

            prevThread = currThread;
            currThread = currThread.next;

            // when rule has imm coupling mode
	    // When a rule has imm coupling, it need to put right after its parent

            while(currThread != null && currThread.getParent() == parent){
              // finding the place in the queue, based on priority
              if(newRulePriority > currThread.getRulePriority()){
		newRuleThd.next = currThread;
                prevThread.next	= newRuleThd;
                return;
              }
              prevThread = currThread;
              currThread = currThread.next;
            }
            newRuleThd.next = currThread;
            prevThread.next	= newRuleThd;
            return;
	  }
	  prevThread = currThread;
	  currThread = currThread.next;
	}
      }

      // when the rule rule has Deferred coupling mode
      else if (newRuleThd.getCoupling ().getId() == 1){

      // need to find the right spot
        if (currThread != null){
          prevThread = currThread;
	  currThread = currThread.next;

          while(currThread != null ){
	    if (ruleSchedulerDebug)
	      System.out.println("finding the spot ");
            if(newRulePriority > currThread.getRulePriority()){
              newRuleThd.next = currThread;
              prevThread.next	= newRuleThd;
              return;
            }
  	    prevThread = currThread;
  	    currThread = currThread.next;
          }
          newRuleThd.next = null;
 	  prevThread.next	= newRuleThd;
	  if (ruleSchedulerDebug)
     	    System.out.println("put at the end of deff queue");
  	  return;
	}
      }
    }
  }
}
