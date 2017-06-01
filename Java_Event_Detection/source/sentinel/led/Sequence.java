/**
 * Sequence.java --
 * Author          : Seokwon Yang
 * Created On      : Fri Jan  8 23:06:54 1999
 * Last Modified By: Seokwon Yang
 * Last Modified On: Fri Jan  8 23:06:56 1999
 * RCS             : $Id: header.el,v 1.1 1997/02/17 21:45:38 seyang Exp seyang $
 * Copyright (C) University of Florida 1999
 */

package sentinel.led;
import sentinel.comm.*;
import java.util.*;

  /** The Sequence class denotes an event node for the composite event operator SEQUENCE. It
   *  contains the event tables for the left and right children as well as
   *  references to the left and right event nodes. The notify() method contains
   *  logic for detecting the event according to the semantics of the operator. The
   *  SEQUENCE event is detected when the right event occurs following the occurrence
   *  of the left event.
   */
  public  class Sequence extends Composite {

  static boolean evntNotifDebug = DebuggingHelper.isDebugFlagTrue("evntNotifDebug");

 //<GED>
  private SentinelComm commInterf = null;

  /** This parameter and context table is sent to server, if the forward flag
   *  of this event is set.
   */
  private PCTable gedEventTable = null;
  //</GED>



  private PCTable leftEventTable = null;
  private PCTable rightEventTable = null;
  private Event leftEvent;
  private Event rightEvent;
  private boolean eventDetectionDebug = Utilities.isDebugFlagTrue("eventDetectionDebug");

  public Sequence(String eventName, EventHandle leftEventHandle,EventHandle rightEventHandle) {
    super(eventName);
    initialize(eventName,leftEventHandle,rightEventHandle);
    eventCategory = EventCategory.LOCAL;
  }

  //<GED>
  public Sequence(String eventName, EventHandle leftEventHandle,
      EventHandle rightEventHandle,SentinelComm commIntf) {
    super(eventName);
    initialize(eventName,leftEventHandle,rightEventHandle);
    eventCategory = EventCategory.GLOBAL;
    commInterf = commIntf;
  }
  //</GED>

  private void initialize(String eventName, EventHandle leftEventHandle,
        EventHandle rightEventHandle) {
    leftEvent =	leftEventHandle.getEventNode();
    // added by seyang
    // rational : Primitive Event with Filter is detected by LED.
    // and LED only propagates regular events.
    if((leftEventHandle instanceof PrimitiveEventHandle) &&
        (((PrimitiveEventHandle)leftEventHandle).isFilterEvent())) {
      leftEvent = ((FilterEvent)leftEvent).getEventNode();
    }
    if (leftEvent == null) {
      if (eventDetectionDebug)
        System.out.println("And:	Event "+leftEventHandle+" does not exist");
      return;
    }
    rightEvent = rightEventHandle.getEventNode();
    // added by seyang
    // rational : Primitive Event with Filter is detected by LED.
    // and LED only propagates regular events.
    if((rightEventHandle instanceof PrimitiveEventHandle) &&
        (((PrimitiveEventHandle)rightEventHandle).isFilterEvent())) {
      rightEvent = ((FilterEvent)rightEvent).getEventNode();
    }
    if (rightEvent == null)	{
      if (eventDetectionDebug)
        System.out.println("And:	Event "+rightEventHandle+" does	not	exist");
      return;
    }
    leftEventTable = new PCTable(this);
    rightEventTable	= new PCTable(this);
    leftEvent.subscribe(this);
    rightEvent.subscribe(this);
  }

  /**
    * Constructor with Filter. It creates the FilterEvent and subscribe to child events with that event.
    *
    * @param eventName a value of type 'String'
    * @param leftEventHandle a value of type 'EventHandle'
    * @param rightEventHandle a value of type 'EventHandle'
    */
  public Sequence(String eventName, EventHandle leftEventHandle,
        EventHandle rightEventHandle, Filter filter) {
    super(eventName);
    leftEvent =	leftEventHandle.getEventNode();
    eventCategory = EventCategory.LOCAL;
    // added by seyang
    // rational : Primitive Event with Filter is detected by LED.
    // and LED only propagates regular events.
    if((leftEventHandle instanceof PrimitiveEventHandle) &&
          (((PrimitiveEventHandle)leftEventHandle).isFilterEvent())) {
      leftEvent = ((FilterEvent)leftEvent).getEventNode();
    }
    if (leftEvent == null) {
      if (eventDetectionDebug)
        System.out.println("And:	Event "+leftEventHandle+" does not exist");
      return;
    }
    rightEvent = rightEventHandle.getEventNode();
    // added by seyang
    // rational : Primitive Event with Filter is detected by LED.
    // and LED only propagates regular events.
    if((rightEventHandle instanceof PrimitiveEventHandle) &&
        (((PrimitiveEventHandle)rightEventHandle).isFilterEvent())) {
      rightEvent = ((FilterEvent)rightEvent).getEventNode();
    }
    if (rightEvent == null)	{
      if (eventDetectionDebug)
        System.out.println("And:	Event "+rightEventHandle+" does	not	exist");
      return;
    }
    leftEventTable = new PCTable(this);
    rightEventTable	= new PCTable(this);
    // create a FilterEvent and subscribe to the child events
    FilterEvent filterEvent = new FilterEvent(this, filter);
    if (filterEvent != null) {
      leftEvent.subscribe(filterEvent);
      rightEvent.subscribe(filterEvent);
    }
  }

  /** This method increments the counter for the given context
   *  recursively until leaf nodes are reached from this event
   *  node in the event graph.
   */
  public    void setContextRecursive(int context){
    this.setContextCurrent(context);
    leftEvent.setContextRecursive(context);
    rightEvent.setContextRecursive(context);
  }

  /** This method decrements the counter for the given context
   *  recursively until leaf nodes are reached from this event
   *  node in the event graph.
   */
  public	void resetContextRecursive(int context){
    this.resetContextCurrent(context);
    leftEvent.resetContextRecursive(context);
    rightEvent.resetContextRecursive(context);
  }
  // added by seyang on 11 Aug 99, modified on Sept 19 99 for version control

  /** This method returns the left or right event tables of this event
   *  node. This method is called by other event nodes to which this
   *  event has subscribed.
   */
  protected  Table getTable(Notifiable e) {
    if (leftEvent.equals((Event)e))
      return (Table)leftEventTable;
    else if (rightEvent.equals((Event)e))
      return (Table)rightEventTable;
    else {
      System.out.println("\n ERROR! And: Unsubscribed event " +
  	   "asking for event table");
      return null;
    }
  }

  /** This method removes the entries in the right table that have a
   *  lesser time stamp than the youngest time stamp in the left event
   *  table. This is done when a left event notification comes at
   *  this node.
   */
  private void cleanRightTable() {
    TimeStamp youngestTS = leftEventTable.getYoungestTS();
    if (youngestTS == null)
      return;
    if (eventDetectionDebug)
      System.out.println("Seq: youngestTS  from left table = " + youngestTS.getGlobalTick());
    Enumeration en = rightEventTable.elements();
    if (eventDetectionDebug)
      System.out.println("rightEventTable.size() = " + rightEventTable.size());
    int size = rightEventTable.size();
    int j = 0;
    for (int i=0; i<size; i++) {
      PCEntry ety = (PCEntry) rightEventTable.elementAt(j);
      if (ety.olderThan(youngestTS)) {
        if (eventDetectionDebug) {
        System.out.print("Removing entry from right table" );

      }
      ety.print();
      rightEventTable.removeEntry(ety);
    }
    // the element at the current index is not older than youngestTS.
    // Check if any entries at a higher index are older than youngestTS.
    // This might happen when the entries come into the PCTable in out
    // of order.
    else {
      if (eventDetectionDebug)
        System.out.println("Incrementing j");
        j++;
      }
      if (eventDetectionDebug) {
        if (rightEventTable.size() != 0) {
          System.out.println("rightEventTable.size() = " + rightEventTable.size());
          System.out.println("there ARE more elements");
        }
        else{
          if (eventDetectionDebug)
            System.out.println("there are NO more elements");
        }
      }
    }
    if (eventDetectionDebug) {
      System.out.print("Printing right table after cleaning: ");
    }
    rightEventTable.print();
  }

  public boolean merge(PCTable left, PCTable right,
		  Thread parent, RuleScheduler ruleScheduler) {
    if (left.size() == 0) return false;
    if (right.size() == 0) return false;
    if (eventDetectionDebug)
      System.out.println("\nLeft Table ->>");
    left.print();
    if (eventDetectionDebug)
      System.out.println("\nRight Table ->>");
    right.print();

    boolean detected = false;
    if (eventDetectionDebug){
      printDetectionMask();

    //<GED>
    System.out.println("recentCounter::"+recentCounter);
    System.out.println("chronCounter::"+chronCounter);
    System.out.println("contiCounter::"+contiCounter);
    System.out.println("cumulCounter::"+cumulCounter);
    //</GED>
    }
    //<GED>
    /**
     * Instantiate the pctable so that we can packing the
     * parameter lists into this object.
     */
    if(sendBackFlag == true || forwardFlag == true)
      gedEventTable  = new PCTable();
    //</GED>

    if (recentCounter != 0)
      detected = detected | detectRecent(left,right, parent,ruleScheduler);
    if (chronCounter != 0)
      detected = detected | detectChronicle(left,right, parent,ruleScheduler);
    if (contiCounter != 0)
      detected = detected | detectContinuous(left,right, parent,ruleScheduler);
    if (cumulCounter != 0)
      detected = detected | detectCumulative(left,right, parent,ruleScheduler);

    //<GED>
    /**
     *  When the producer detects the composite event, it will notify
     *  the server
     **/
    if(forwardFlag){

      LEDInterface ledIntf = ECAAgent.getLEDInterface();
      if(evntNotifDebug){
        System.out.println("\nShow the pctable that will be sent to sever");
        gedEventTable.print();
      }
      NotificationMessage notifMesg = new NotificationMessage(eventName,Constant.APP_NAME,Constant.APP_URL,gedEventTable);
      commInterf =  ledIntf;

      /*For single GED, doesn't need to specify the destination*/
      //      String des = Constant.APP_ID+Constant.GED_NAME;

      String des = Constant.GED_URL+Constant.GED_NAME;
      commInterf.send(des,notifMesg);
    }

    /**
     * When the server detects the occurence of event and the sendBackFlag
     * is set
     */
    else if(sendBackFlag){
      if(evntNotifDebug){
        System.out.println("Send back the event to consumer");
        System.out.println("\nShow the pctable that will be sent to sever");
        gedEventTable.print();
        System.out.println("GED notifies the occurence to all customers");
      }
      NotificationMessage notifMesg = new NotificationMessage(eventName,"","",gedEventTable);

      commInterf.send(notifMesg);
    }
    //</GED>

    left.clearGarbage();
    right.clearGarbage();
    return detected;
  }



  // added by wtanpisu
  // rational : to utilize the rule scheduler.
  /** This method detects the SEQUENCE event in RECENT context. It propagates
   *  the merged event table to the subscribed events and executes the
   *  rules defined in recent context.
   */
  private boolean detectRecent(PCTable left, PCTable right,Thread parent,
    RuleScheduler ruleScheduler) {

    EventSet merged_set_R = new EventSet();
    PCEntry entry1, entry2;
    EventSet set1,set2;
    boolean detected = false;

    entry1 = left.getRecentSet();
    entry2 = right.getRecentSet();
    if(eventDetectionDebug){
      System.out.println("Time stamp of left event :"+ entry1.getTS().getGlobalTick());
      System.out.println("Time stamp of right event :"+ entry2.getTS().getGlobalTick());
    }
    if((entry1 != null) && (entry2 != null) && entry1.olderThan(entry2)) {
      set1 = entry1.getEventSet();
      set2 = entry2.getEventSet();
      if (eventDetectionDebug)
        System.out.println("\nEvent " + eventName +
                               " was triggered at the context of RECENT.");
      merged_set_R.union(set1, set2);
      merged_set_R.print();

      /* When this node is on local site. */
      //if(!(isOnGlbEvntGraph)){
      if(eventCategory != EventCategory.GLOBAL){
        executeRules(merged_set_R,ParamContext.RECENT, parent,ruleScheduler);
      }
      propagatePC(merged_set_R, ParamContext.RECENT);

      //<GED>
      /**
       * When the event occurs at the producer site, packing the parameter lists into PCTable
       * and send to the server. (forwardFlag is true)
       * When the event occurs at the server, packint the parameter lists into PCTable
       * and send to the consumer. (sendbackFlag is true)
       */
      if(forwardFlag || sendBackFlag){
        gedEventTable.getCompositePropagation(merged_set_R,ParamContext.RECENT.getId()) ;
         if(evntNotifDebug){
          System.out.println("Show the pctable that will sent to server");
          gedEventTable.print();
        }
      }
      //</GED>

      detected = true;
    }
    return detected;
  }



  /** This method detects the SEQUENCE event in CHRONICLE context. It propagates
   *  the merged event table to the subscribed events and executes the
   *  rules defined in chronicle context.
   */

  private boolean detectChronicle(PCTable left, PCTable right,
	  Thread parent, RuleScheduler ruleScheduler) {
    EventSet merged_set_H = new EventSet();
    PCEntry entry1, entry2;
    EventSet set1,set2;
    boolean detected = false;

    entry1 = left.getOldestChronSet();
    entry2 = right.getOldestChronSet();
    if((entry1 != null) && (entry2 != null)) {
      set1 = entry1.getEventSet();
      set2 = entry2.getEventSet();
      entry1.clearChronicle();
      entry2.clearChronicle();
      merged_set_H.union(set1, set2);
	  	if (eventDetectionDebug)
      System.out.println("\n\nEvent " + eventName +
			  	   " was triggered at the context of CHRONICLE.");
      merged_set_H.print();

      /* When this node is on local site.*/
      //if(!(isOnGlbEvntGraph)){
      if(eventCategory != EventCategory.GLOBAL){
         executeRules(merged_set_H,ParamContext.CHRONICLE, parent,ruleScheduler);
      }
      propagatePC(merged_set_H, ParamContext.CHRONICLE);

      //<GED>
      /**
       * When the event occurs at the producer site, packing the parameter lists into PCTable
       * and send to the server. (forwardFlag is true)
       * When the event occurs at the server, packint the parameter lists into PCTable
       * and send to the consumer. (sendbackFlag is true)
       */
      if(forwardFlag || sendBackFlag){
        gedEventTable.getCompositePropagation(merged_set_H, ParamContext.CHRONICLE.getId()) ;
        if(evntNotifDebug){
          System.out.println("Show the pctable that will sent to server");
          gedEventTable.print();
        }
      }
      //</GED>
      detected = true;
    }
    return detected;
  }

  /** This method detects the SEQUENCE event in CONTINUOUS context. It propagates
   *  the merged event table to the subscribed events and executes the
   *  rules defined in continuous context.
   */
  private boolean detectContinuous(PCTable left, PCTable right,
						  Thread parent, RuleScheduler ruleScheduler) {
    EventSet merged_set_O = null;
    PCEntry entry1, entry2;
    EventSet set1,set2;
    boolean detected = false;

    Vector leftSetArry = left.getContiSets();
    Vector rightSetArry = right.getContiSets();
    if ((leftSetArry.size()!=0) && (rightSetArry.size()!=0)) {
      for (int i=0; i<leftSetArry.size(); i++) {
        entry1 = (PCEntry) leftSetArry.elementAt(i);
        entry1.clearContinuous();
        set1 = entry1.getEventSet();
        for (int j=0; j<rightSetArry.size(); j++) {
          merged_set_O = new EventSet();
          entry2 = (PCEntry) rightSetArry.elementAt(j);
          entry2.clearContinuous();
          set2 = entry2.getEventSet();
          merged_set_O.union(set1,set2);
		  	if (eventDetectionDebug)
          System.out.println("\nEvent " + eventName + " was triggered" +
				           " at the context of CONTINUOUS.");
          merged_set_O.print();

          /* When this node is on local site */
          //if(!(isOnGlbEvntGraph)){
          if(eventCategory != EventCategory.GLOBAL){
            executeRules(merged_set_O,ParamContext.CONTINUOUS, parent,ruleScheduler);
          }
          propagatePC(merged_set_O, ParamContext.CONTINUOUS);

        //<GED>
        /**
         * When the event occurs at the producer site, packing the parameter lists into PCTable
         * and send to the server. (forwardFlag is true)
         * When the event occurs at the server, packint the parameter lists into PCTable
         * and send to the consumer. (sendbackFlag is true)
         */
        if(forwardFlag || sendBackFlag){
          gedEventTable.getCompositePropagation(merged_set_O, ParamContext.CONTINUOUS.getId()) ;
          if(evntNotifDebug){
            System.out.println("Show the pctable that will sent to server");
            gedEventTable.print();
          }
        }
        //</GED>
          detected = true;
        }
      }
    }
    return detected;
  }

  /** This method detects the SEQUENCE event in CUMULATIVE context. It propagates
   *  the merged event table to the subscribed events and executes the
   *  rules defined in cumulative context.
   */
  private boolean detectCumulative(PCTable left, PCTable right,
			  Thread parent, RuleScheduler ruleScheduler) {
    EventSet merged_set_U = new EventSet();
    EventSet set1;
    boolean detected = false;
    PCEntry entry1 = right.getOldestCumulSet();
    Vector setArry = left.getCumulSet();
    if((entry1 !=null) && (setArry.size() != 0)) {
      set1 = entry1.getEventSet();
      entry1.clearCumulative();
      for (int i = 0; i < setArry.size(); i++)
        merged_set_U.addElement((EventSet)(setArry.elementAt(i)));
      merged_set_U.addElement(set1);
      if (eventDetectionDebug)
        System.out.println("\n\nEvent " + eventName + " was triggered" +
					" at the context of CUMULATIVE.");
      merged_set_U.print();

      /* When this node is on local site. */
      if(eventCategory != EventCategory.GLOBAL){
        executeRules(merged_set_U,ParamContext.CUMULATIVE, parent,ruleScheduler);
      }
      propagatePC(merged_set_U, ParamContext.CUMULATIVE);

      //<GED>
      /**
       * When the event occurs at the producer site, packing the parameter lists into PCTable
       * and send to the server. (forwardFlag is true)
       * When the event occurs at the server, packint the parameter lists into PCTable
       * and send to the consumer. (sendbackFlag is true)
       */
      if(forwardFlag || sendBackFlag){
        gedEventTable.getCompositePropagation(merged_set_U, ParamContext.CUMULATIVE.getId()) ;
        if(evntNotifDebug){
          System.out.println("Show the pctable that will sent to server");
          gedEventTable.print();
        }
      }
      //</GED>
      detected = true;
    }
    return detected;
  }

  /** This method contains the detection logic according to the semantics
   *  of the SEQUENCE operator.
   */
  public void notify(Notifiable event, Thread parent, Scheduler scheduler ) {
    RuleScheduler ruleScheduler = (RuleScheduler)scheduler;
    if (leftEvent.equals((Event) event)) {
      if (eventDetectionDebug) {
        System.out.println("\nSeq: Notification received for left event");
        /*System.out.println("leftEventTable ->>");
        leftEventTable.print();
        System.out.println("rightEventTable ->>");
        rightEventTable.print(); */
      }
      if (rightEventTable.size() != 0)
        cleanRightTable();
    }
    else {
      if (eventDetectionDebug)
	System.out.println("\nSeq: Notification received for right event");
      if (merge(leftEventTable,rightEventTable, parent,ruleScheduler))
        propagateEvent( parent,ruleScheduler);
    }
  }
  // added by wtanpisu on 24 Jan 2000
}
