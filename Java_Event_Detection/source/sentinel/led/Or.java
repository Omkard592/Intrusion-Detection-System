/**
 * Or.java --
 * Author          : Hyoujin Kim, Seokwon Yang
 * Created On      : Fri Jan  8 23:06:00 1999
 * Last Modified By: Seokwon Yang
 * Last Modified On: Sat Sep 18 22:00:15 1999
 * Copyright (C) University of Florida 1999
 */
package sentinel.led;

import sentinel.comm.*;
import java.util.*;

/** The Or class denotes an event node for the composite event operator OR. It
 *  contains the event tables for the left and right children as well as
 *  references to the left and right event nodes. The notify() method contains
 *  logic for detecting the event according to the semantics of the operator. The Or
 *  event is detected when either left event occurs or the right event occurs.
 */
public class Or extends Composite {

  static boolean evntNotifDebug = DebuggingHelper.isDebugFlagTrue("evntNotifDebug");

  private PCTable leftEventTable = null;
  private PCTable rightEventTable = null;
  private Event leftEvent;
  private Event rightEvent;


  //<GED>
  private SentinelComm commInterf = null;

  /** This parameter and context table is sent to server, if the forward flag
   *  of this event is set.
   */
  private PCTable gedEventTable = null;
  //</GED>


  private boolean eventDetectionDebug = Utilities.isDebugFlagTrue("eventDetectionDebug");

  //  public void notify(Notifiable child){;}

  public Or(String eventName, EventHandle leftEventHandle,EventHandle rightEventHandle) {
    super(eventName);
    initialize(eventName,leftEventHandle,rightEventHandle);
    eventCategory = EventCategory.LOCAL;
  }

  private  void initialize(String eventName, EventHandle leftEventHandle,
	   EventHandle rightEventHandle ) {

    leftEvent = leftEventHandle.getEventNode();

    // added by seyang
    // rational : Primitive Event with Filter is detected by LED.
    // and LED only propagates regular events.
    if((leftEventHandle instanceof PrimitiveEventHandle) &&
	   (((PrimitiveEventHandle)leftEventHandle).isFilterEvent())) {
      leftEvent = ((FilterEvent)leftEvent).getEventNode();
    }

    if (leftEvent == null) {
      if (eventDetectionDebug)
        System.out.println("And: Event "+leftEventHandle+" does not exist");
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
    if (rightEvent == null) {
      if (eventDetectionDebug)
        System.out.println("And: Event "+rightEventHandle+" does not exist");
      return;
    }
    leftEventTable = new PCTable(this);
    rightEventTable = new PCTable(this);
    leftEvent.subscribe(this);
    rightEvent.subscribe(this);
  }


  // added by seyang on 11 Aug 99, modified on Sept 19 99 for version control
  /**
   * Constructor with Filter. It creates the FilterEvent and subscribe to child events with that event.
   *
   * @param eventName a value of type 'String'
   * @param leftEventHandle a value of type 'EventHandle'
   * @param rightEventHandle a value of type 'EventHandle'
   */
  public Or(String eventName, EventHandle leftEventHandle,EventHandle rightEventHandle,
	      Filter filter) {
    super(eventName);
    //leftEvent = (Event) evntNamesEvntNodes.get(leftEventName);
    leftEvent = leftEventHandle.getEventNode();
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
        System.out.println("And: Event "+leftEventHandle+" does not exist");
      return;
    }

    //rightEvent = (Event) evntNamesEvntNodes.get(rightEventName);
    rightEvent = rightEventHandle.getEventNode();
    // added by seyang
    // rational : Primitive Event with Filter is detected by LED.
    // and LED only propagates regular events.
    if((rightEventHandle instanceof PrimitiveEventHandle) &&
         (((PrimitiveEventHandle)rightEventHandle).isFilterEvent())) {
      rightEvent = ((FilterEvent)rightEvent).getEventNode();
    }

    if (rightEvent == null) {
      if (eventDetectionDebug)
        System.out.println("And: Event "+rightEventHandle+" does not exist");
      return;
    }
    leftEventTable = new PCTable(this);
    rightEventTable = new PCTable(this);
    // create a FilterEvent and subscribe to the child events
    FilterEvent filterEvent = new FilterEvent(this, filter);
    if (filterEvent != null) {
      leftEvent.subscribe(filterEvent);
      rightEvent.subscribe(filterEvent);
    }
  }

  //<GED>
  public Or(String eventName, EventHandle leftEventHandle,EventHandle rightEventHandle,SentinelComm commIntf ) {
    super(eventName);
    initialize(eventName,leftEventHandle,rightEventHandle);
    eventCategory = EventCategory.GLOBAL;
    commInterf = commIntf;
  }
  //</GED>



  // added by seyang on 11 Aug 99, modified on Sept 19 99 for version control

  /** This method increments the counter for the given context
   *  recursively until leaf nodes are reached from this event
   *  node in the event graph.
   */
  public void setContextRecursive(int context) {
    this.setContextCurrent(context);
    leftEvent.setContextRecursive(context);
    rightEvent.setContextRecursive(context);
  }

  /** This method decrements the counter for the given context
   *  recursively until leaf nodes are reached from this event
   *  node in the event graph.
   */

  public  void resetContextRecursive(int context) {
    this.resetContextCurrent(context);
    leftEvent.resetContextRecursive(context);
    rightEvent.resetContextRecursive(context);
  }

  /** This method returns the left or right event tables of this event
   *  node. This method is called by other event nodes to which this
   *  event has subscribed.
   */
  public Table getTable(Notifiable e) {
    if (leftEvent.equals((Event)e))
      return (Table)leftEventTable;
    else if (rightEvent.equals((Event)e))
      return (Table)rightEventTable;
    else
      return null;
  }

  // added by wtanpisu
  private void detectEvent(PCTable table,
    Thread parent, RuleScheduler ruleScheduler) {
    if(eventDetectionDebug){
    printDetectionMask();
    System.out.println("recentCounter::"+recentCounter);
    System.out.println("chronCounter::"+chronCounter);
    System.out.println("contiCounter::"+contiCounter);
    System.out.println("cumulCounter::"+cumulCounter);
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
      detectRecent(table, parent,ruleScheduler);
    if (chronCounter != 0)
      detectChronicle(table, parent,ruleScheduler);
    if (contiCounter != 0)
      detectContinuous(table, parent,ruleScheduler);
    if (cumulCounter != 0)
      detectCumulative(table, parent,ruleScheduler);

    //<GED>
    /**
     *  When the producer detects the composite event, it will notify
     *  the server
     */
    if(forwardFlag){
      LEDInterface ledIntf = ECAAgent.getLEDInterface();
      if(evntNotifDebug){
        System.out.println("\nShow the pctable that will be sent to sever");
        gedEventTable.print();
      }
      NotificationMessage notifMesg = new NotificationMessage(eventName,Constant.APP_NAME,Constant.APP_URL,gedEventTable);
      commInterf =  ledIntf;

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

    table.clearGarbage();
  }

  /** This method detects the OR event in RECENT context. It propagates
   *  the event table to the subscribed events and executes the
   *  rules defined in recent context.
   */
  private void detectRecent(PCTable table,
	  Thread parent, RuleScheduler ruleScheduler) {
    EventSet merged_set_R = new EventSet();
    PCEntry entry = table.getRecentSet();
    if (entry != null) {
      merged_set_R = entry.getEventSet();
	  	if (eventDetectionDebug)
        System.out.println("\nEvent " + eventName +
                        " was triggered in context RECENT.");
      merged_set_R.print();

      /* When this node is on local site */
      //<d> if(!(isOnGlbEvntGraph)){
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

    }
  }


  /** This method detects the OR event in CHRONICLE context. It propagates
   *  the event table to the subscribed events and executes the
   *  rules defined in chronicle context.
   */
  private void detectChronicle(PCTable table,
		  Thread parent, RuleScheduler ruleScheduler) {
    EventSet merged_set_H = new EventSet();
    PCEntry entry = table.getOldestChronSet();
    if (entry != null) {
      entry.clearChronicle();
      merged_set_H = entry.getEventSet();
      if (eventDetectionDebug)
        System.out.println("\nEvent " + eventName +
                        " was triggered in context CHRONICLE.");
      merged_set_H.print();

      /* When this node is on local site. */

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
        gedEventTable.getCompositePropagation(merged_set_H,ParamContext.CHRONICLE.getId()) ;
        if(evntNotifDebug){
              System.out.println("Show the pctable that will sent to server");
              gedEventTable.print();
        }
      }
      //</GED>

    }
  }


  /** This method detects the OR event in CONTINUOUS context. It propagates
   *  the event table to the subscribed events and executes the
   *  rules defined in continuous context.
   */

  private void detectContinuous(PCTable table,
			  Thread parent, RuleScheduler ruleScheduler) {
    EventSet merged_set_O = null;
    Vector setArry = table.getContiSets();
    PCEntry entry = null;
    for (int i=0; i<setArry.size(); i++) {
      entry = (PCEntry) setArry.elementAt(i);
      entry.clearContinuous();
      merged_set_O = new EventSet();
      merged_set_O = entry.getEventSet();
		  if (eventDetectionDebug)
        System.out.println("\nEvent " + eventName +
                        " was triggered in context CONTINUOUS.");
      merged_set_O.print();

      /* When this node is on local site. */

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
        gedEventTable.getCompositePropagation(merged_set_O,ParamContext.CONTINUOUS.getId()) ;
        if(evntNotifDebug){
          System.out.println("Show the pctable that will sent to server");
          gedEventTable.print();
        }
      }
      //</GED>
    }
  }


  /** This method detects the OR event in CUMULATIVE context. It propagates
   *  the event table to the subscribed events and executes the
   *  rules defined in cumulative context.
   */

  private void detectCumulative(PCTable table,
	  Thread parent, RuleScheduler ruleScheduler) {
    EventSet merged_set_U = new EventSet();
    PCEntry entry = table.getOldestCumulSet();
    if (entry != null) {
      merged_set_U = entry.getEventSet();
	  	if (eventDetectionDebug)
		  System.out.println("\nEvent " + eventName +
      " was triggered in context CUMULATIVE.");
      merged_set_U.print();
      /* When this node is on local site. */
      //<d>if(!(isOnGlbEvntGraph)){
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
        gedEventTable.getCompositePropagation(merged_set_U,ParamContext.CUMULATIVE.getId()) ;
        if(evntNotifDebug){
          System.out.println("Show the pctable that will sent to server");
          gedEventTable.print();
        }
      }
      //</GED>
    }
  }


  /** This method contains the detection logic according to the semantics
   *  of the OR operator.
   */

  public void notify(Notifiable event, Thread parent, Scheduler scheduler ) {
    RuleScheduler ruleScheduler = ( RuleScheduler)scheduler;
    if (leftEvent.equals((Event)event))
      detectEvent(leftEventTable, parent,ruleScheduler);
    else if (rightEvent.equals((Event)event))
      detectEvent(rightEventTable, parent,ruleScheduler);
    else {
      System.out.println("\n ERROR! And: Notification received from a "+
                                "non-constituent event");
      return;
    }
    propagateEvent( parent,ruleScheduler);
  }
  // added by wtanpisu on 24 Jan 2000
}
