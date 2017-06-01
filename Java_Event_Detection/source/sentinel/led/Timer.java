/**
 * Timer.java --
 * Author          : Seokwon Yang
 * Created On      : Fri Jan  8 23:06:54 1999
 * Last Modified By: Seokwon Yang
 * Last Modified On: Fri Jan  8 23:06:56 1999
 * RCS             : $Id: header.el,v 1.1 1997/02/17 21:45:38 seyang Exp seyang $
 * Copyright (C) University of Florida 1999
 */


package sentinel.led;

/** The Timer class is used to simulate the timer notifications from a timer. It
 *  contains two time queues - allItems and presentItems.
 *  presentItems queue stores the time items that have the same expiration time and
 *  the timer is currently set to this value.
 *  allItems queue stores all the other time items that have a greater time value
 *  than the time items in the presentItems queue. The timer thread is set with
 *  maximum priority so that it is given the highest priority to be scheduled since
 *  there are multiple threads are running in the system.
 */
class Timer extends Thread {
  TimeQueue allItems = null;
  TimeQueue presentItems = null;

  private boolean timerDebug = Utilities.isDebugFlagTrue("timerDebug");
  static Timer timer = new Timer();
  static {
      // added by weera
    timer.setName("Timer");
      // add on Feb 11

    timer.setDaemon(true);
    timer.start();
  }

  Timer() {
    allItems = new TimeQueue();
    presentItems = new TimeQueue();
    setPriority(MAX_PRIORITY);
  }

  NotifyBuffer notifyBuffer;

  void setNotifyBuffer(NotifyBuffer notifyBuffer){
	   this.notifyBuffer = notifyBuffer;
  }

  /** This method adds a relative temporal item to the timer.
    */
  void addItem(Primitive temporalNode, String timeStr, long eventId,RuleScheduler ruleScheduler) {
	  if (timerDebug)
		  System.out.println("Setting the timer with time string " + timeStr);
    TimeItem ti = new TimeItem(temporalNode,timeStr,eventId);
	  ti.setScheduler(ruleScheduler);
	  ti.setNotifyBuffer(notifyBuffer);
	  addItem(ti);
  }

  void addItem(Primitive temporalNode, String timeStr, long eventId) {
	  if (timerDebug)
		  System.out.println("Setting the timer with time string " + timeStr);
    TimeItem ti = new TimeItem(temporalNode,timeStr,eventId);
	  addItem(ti);
  }

   /** This method adds an absolute temporal item to the timer.
    */
  void addAbsoluteItem(Primitive temporalNode, String timeStr,
						long eventId,RuleScheduler ruleScheduler,NotifyBuffer notifyBuffer) {
	  if (timerDebug)
		  System.out.println("Setting the timer with time string " + timeStr);
    TimeItem ti = new TimeItem(temporalNode,timeStr,eventId);
	  ti.setScheduler(ruleScheduler);

	  // add 03/30
	  ti.setNotifyBuffer(notifyBuffer);
	  addItem(ti);
  }

  static Timer getTimer() {
    return timer;
  }

  /** This method adds a time item (absolute or relative) to the timer and places
    *  the time item in either the allItems queue or the presentItems queue depending
    *  on the value of the given time item.
    */

  private void addItem(TimeItem ti) {
    long currTime = ti.getMillis();
    long sysTime = System.currentTimeMillis();

	  if (timerDebug) {
		  System.out.println("Adding time " + currTime);
		  System.out.println("System time " + sysTime);
	  }
 //	  else
 //		  System.out.println("timerDebug flag is false");

    if (timerDebug){
      System.out.println("show the elements in all items time queue in adding funciton");
		  Timer.timer.allItems.print();
		  System.out.println("show the elements in present time queue");
      Timer.timer.presentItems.print();
    }

    if (currTime > sysTime) {
		  if (!presentItems.isEmpty()) {
			  long presentSleepTime = presentItems.getFirstTime();
    		if (timerDebug)
		  	  System.out.println("Present sleeping time = " + presentSleepTime);
        long diffTime =	currTime - presentSleepTime;
    		if (timerDebug)
          System.out.println("Difference between Present sleeping time and "
						  + " the Current time being added = "	+ diffTime);
			// The difference between current time and present sleeping time is checked.
			// If the difference is less than 150 millis, both the time values are
			// interpreted as equal. This is needed if two temporal events are very close.
			// 150 millis is somewhat arbitrarily chosen.
  			if (diffTime > 0 &&	diffTime < 150)
	    		presentItems.add(ti);
        else if (currTime > presentSleepTime) {
				  if (timerDebug)
	  				System.out.println("Current time is greater than the time to which the timer is set");
  				allItems.add(ti);
		  	}
			  else {   // currTime < presentSleepTime
				  if (timerDebug) {
					  System.out.println("Current time is greater than the time to which the timer is set");
					  System.out.println("Interrupting the timer thread...");
				  }
				  movePresItemsToAllItems();
				  presentItems.add(ti);
				  this.interrupt();
				  //timer.interrupt();
			  }
      }
		  else if(presentItems.isEmpty()   && (! allItems.isEmpty())){// presentItems is empty but allItem is not empty, let process method get the timeitem
		         // in allItem and put into presentItem
		    allItems.add(ti);
      }
		  else {    // presentItems is empty
		    if (timerDebug) {
				  System.out.println("presentItems is empty");
				  System.out.println("Resuming the timer thread...");
			  }
			  presentItems.add(ti);
			  synchronized(this) {
				  notify();  // resume the timer thread
			  }
      }
    }
	  else if (ti.isRepetitive()) {
	    if (ti.requiresUpdate()) {
		    ti.computeNext();
		    addItem(ti);
		  }
	  }
  }

   /** This method moves all the time items from presentItems queue to allItems
    *  queue.
    */
  void movePresItemsToAllItems() {
    TimeItem timeItem = null;
    while (!presentItems.isEmpty()) {
      timeItem = presentItems.deleteHead();
		  allItems.add(timeItem);
    }
  }

   /** This method gets the time value to be scheduled from the presentItems queue.
    *  if the presentItems queue is empty, it returns an invalid value, -1.
    */
  long schedule() {
    long sysTime = System.currentTimeMillis();
    long presentTime;
    if (presentItems.isEmpty())
		  return -1;
    else {
  		presentTime = presentItems.getFirstTime();
	  	return(presentItems.getFirstTime()- sysTime);
    }
  }

   /** This method is called when a timer notification comes for a time item. The timer
    *  thread calls this method when it finishes a sleep() period for a scheduled
    *  time.
    */
  void process() {
    TimeItem timeItem;
	  boolean updated = false;

	  // process all time items in the presentItems list
     while (!presentItems.isEmpty()
			     && presentItems.getFirstTime()<=System.currentTimeMillis()) {
    //    while (!presentItems.isEmpty()) {

	    // this.print();

      if(timerDebug){
        System.out.println("Delete the head of present queue and trigger the rule");
        System.out.println("show the elements in all items time queue in deleing funciton");
  			Timer.timer.allItems.print();
	  		System.out.println("show the elements in present time queue in deleting fuction");
  			Timer.timer.presentItems.print();
      }

      timeItem = presentItems.deleteHead();
  		timeItem.notifyEvent();
      updated = timeItem.process();

	    if (updated){
        if(timerDebug)
			    System.out.println("it 's updating the all item queue and add"+timeItem.getMillis());
        allItems.add(timeItem);
      }
    }

	  if (!allItems.isEmpty()) {
      long lowestTime = allItems.getFirstTime();
      while (!allItems.isEmpty() && allItems.getFirstTime() == lowestTime) {
        timeItem = allItems.deleteHead();
        presentItems.add(timeItem);
      }
    }
  }

   /** The run method of the timer thread sleeps for the scheduled time period and
    *  then processes the corresponding time item by calling the notify() method
    *  on the corresponding temporal event node.
    */
  public void run() {

    long delay;
    while (true) {
      delay = schedule();
      if (timerDebug)
        System.out.println("DELAY is "+delay);
      try {
        /*	 if (delay == -1) {
				 if (timerDebug)
					 System.out.println("Timer thread is waiting for scheduling a time item");
				 synchronized(this) {
					 wait();	// suspend the timer thread
				 }
			 }
			 else if(delay >= 0 ){
				 if (timerDebug)
					System.out.println("Timer thread sleeping for = " + delay + " millis");
				 sleep(delay);
				 process();
			 }
			 else{process();}
		 */

        if (delay < 0) {
          if (timerDebug)
	    System.out.println("Timer thread is waiting for scheduling a time item");
          synchronized(this) {
            if( presentItems.getNoOfEvntInQue() == 0)
	      wait();	// suspend the timer thread
	  }
          process();
  	}
        else {
	  if (timerDebug)
	    System.out.println("Timer thread sleeping for = " + delay + " millis");
          sleep(delay);
	  process();
          if (timerDebug)
	    System.out.println("Finish process");
	}
      }
      catch(InterruptedException e) {
        if (timerDebug){
	  System.out.println("Timer Thread interrupted");
        }
      }
    }
  }

   void print() {
      System.out.print("\nallItems ->> ");
      allItems.print();
      System.out.print("\npresentItems ->> ");
      presentItems.print();
   }

   public static void main(String[] args) {

       //Timer tqh = new Timer();
       // tqh.setDaemon(true);
       //tqh.start();
       //tqh.addItem("0 hrs 0 min 35 sec",0);
       //tqh.addItem("0 hrs 0 min 3 sec",0);
       //tqh.addItem("0 hrs 0 min 6 sec",0);
       //tqh.addItem("0 hrs 0 min 1 sec",0);
       //tqh.addItem("0 hrs 0 min 4 sec",0);
	   //tqh.addItem("01:12:30/07/04/1999",0);
	   //tqh.addItem("16:1?:?0/07/04/1999",0);
	   //tqh.addItem("13:*:?5/7/06/1999",0);
	   //timer.addItem("14:*:*/7/16/1999",0);
	   //tqh.addItem("0:*:5/7/03/*",0);
    }
}
