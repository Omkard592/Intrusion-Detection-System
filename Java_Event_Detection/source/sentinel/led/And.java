/**
 * And.java --
 * Author          : Seokwon Yang, H. Kim
 * Created On      : Jan ?? 1999
 * Last Modified By: Seokwon Yang
 * Last Modified On: Sun Sep 19 23:25:10 1999
 * Copyright (C) University of Florida 1999
 */

package sentinel.led;

import sentinel.comm.*;
import java.util.*;

/**
 * The And class denotes an event node for the composite event operator AND. It
 * contains the event tables for the left and right children as well as
 * references to the left and right event nodes. The notify() method contains
 * logic for detecting the event according to the semantics of the operator. The AND
 * event is detected when both the left and right events occur.
 */

 public class And extends Composite {

  static boolean evntNotifDebug = DebuggingHelper.isDebugFlagTrue("evntNotifDebug");

  static boolean globalNotificationDebug = DebuggingHelper.isDebugFlagTrue("globalNotificationDebug");
  static boolean contextPropagationDebug = DebuggingHelper.isDebugFlagTrue("contextPropagationDebug");

  private PCTable leftEventTable = null;
  private PCTable rightEventTable = null;
  private Event leftEvent;
  private Event rightEvent;

  //<GED>
  private SentinelComm commInterf = null;
  /**
   * This parameter and context table is sent to server, if the forward flag
   * of this event is set.
   */
  private PCTable gedEventTable = null;
  //</GED>

  private boolean eventDetectionDebug = Utilities.isDebugFlagTrue("eventDetectionDebug");


  /**
   * Constructs a composite event
   *
   * @param eventName a value of type 'String'
   * @param leftEventHandle a value of type 'EventHandle'
   * @param rightEventHandle a value of type 'EventHandle'
   */
  public And(String eventName, EventHandle leftEventHandle,
				 EventHandle rightEventHandle) {
    super(eventName);
    initialize(eventName,leftEventHandle,rightEventHandle);
    eventCategory = EventCategory.LOCAL;
  }



  //<GED>
  /**
   * Constructor for creating the AND event node on the server (GED)
   */
  public And(String eventName, EventHandle leftEventHandle,EventHandle rightEventHandle,SentinelComm commIntf ) {
    super(eventName);
    initialize(eventName,leftEventHandle,rightEventHandle);
    eventCategory = EventCategory.GLOBAL;
    commInterf = commIntf;
  }
  //</GED>

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
      if(eventDetectionDebug)
	System.out.println("And: Event "+leftEventHandle+" does not exist");
      return;
    }
    if(rightEventHandle == null){
    System.out.println("Can't find the right event handle");
    System.exit(0);
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
      if(eventDetectionDebug)
	System.out.println("And: Event "+rightEventHandle+" does not exist");
      return;
    }
    leftEventTable = new PCTable(this);
    rightEventTable = new PCTable(this);
    leftEvent.subscribe(this);
    rightEvent.subscribe(this);
  }


  // added by seyang on 10 Aug 99, modified on Sept 19 99 for version control
  /**
   * Constructor with Filter. It creates the FilterEvent and subscribe to child events with that event.
   *
   * @param eventName a value of type 'String'
   * @param leftEventHandle a value of type 'EventHandle'
   * @param rightEventHandle a value of type 'EventHandle'
   * @param filter a value of type 'Filter'
   */
  public And(String eventName, EventHandle leftEventHandle,
	     EventHandle rightEventHandle, Filter filter) {
    super(eventName);
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
      if(eventDetectionDebug)
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
      if(eventDetectionDebug)
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
  // added by seyang on 10 Aug 99, modified on Sept 19 99 for version control


  /** This method increments the counter for the given context
   *  recursively until leaf nodes are reached from this event
   *  node in the event graph.
   */
  public void setContextRecursive(int context){
    this.setContextCurrent(context);
    if(leftEvent == null && rightEvent == null){
      System.out.println("Internal Error !!! leftEvent and right Event are null");
    }
    leftEvent.setContextRecursive(context);
    rightEvent.setContextRecursive(context);
  }

  /**
   * This method decrements the counter for the given context
   * recursively until leaf nodes are reached from this event
   * node in the event graph.
   */
  public void resetContextRecursive(int context){
    this.resetContextCurrent(context);
    leftEvent.resetContextRecursive(context);
    rightEvent.resetContextRecursive(context);
  }



  /**
   * This method returns the left or right event tables of this event
   * node. This method is called by other event nodes to which this
   * event has subscribed.
   */
  protected   Table getTable(Notifiable event) {
    if (leftEvent.equals((Event)event))
      return (Table)leftEventTable;
    else if (rightEvent.equals((Event)event))
      return (Table)rightEventTable;
    else {
      System.out.println("\n ERROR! And: Unsubscribed event " + "asking for event table");
      return null;
    }
  }


  // modified by wtanpisu
  // rational : to utilize the rule scheduler, parent and ruleSheduler parameters are added

  /**
   * Notify to the And composite node
   */
  public void notify(Notifiable event, Thread parent, Scheduler scheduler ) {
    RuleScheduler ruleScheduler = (RuleScheduler)scheduler;

    boolean merged;
    // when the global event on server is raised, it calls this notify method
    if(parent == null && ruleScheduler == null){
      if(globalNotificationDebug)
        System.out.println("And::notify");
    }

    if (leftEvent.equals((Event)event)){
      if (eventDetectionDebug)
        System.out.println("And: Notification received for left event");

      merged = merge(rightEventTable, leftEventTable, parent,ruleScheduler);
      if (merged)
        propagateEvent( parent,ruleScheduler);
    }
    else if (rightEvent.equals((Event)event)){
      if (eventDetectionDebug)
        System.out.println("And: Notification received for right event");

      merged = merge(leftEventTable, rightEventTable, parent,ruleScheduler);
      if (merged){
        if (eventDetectionDebug)
	  System.out.println("\nEvent detected, propagating event");
        propagateEvent( parent,ruleScheduler);
      }
    }else
      System.out.println("\n ERROR! And: Notification received from a "+
	    "non-constituent event");
  }


  /**
   * The merge method merges the left and right event tables
   * when the AND event is detected.
   */
  boolean merge(PCTable initiator, PCTable terminator,
        Thread parent, RuleScheduler ruleScheduler) {
    if (initiator.size() == 0) return false;
    if (terminator.size() == 0) return false;

    if (eventDetectionDebug){
      System.out.println("\nInitiator table->>");
      initiator.print();
      System.out.println("\nTerminator table->>");
      terminator.print();
    }

    boolean detected = false;

    if (eventDetectionDebug){
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
      detected = detected | detectRecent(initiator,terminator, parent,ruleScheduler);
    if (chronCounter != 0)
      detected = detected | detectChronicle(initiator,terminator, parent,ruleScheduler);
    if (contiCounter != 0)
      detected = detected | detectContinuous(initiator,terminator, parent,ruleScheduler);
    if (cumulCounter != 0)
      detected = detected | detectCumulative(initiator,terminator, parent,ruleScheduler);

    //<GED>
    // When the producer detects the composite event, it will notify the server.

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

    // When the server detects the occurence of event and the sendBackFlag is set.

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

    initiator.clearGarbage();
    terminator.clearGarbage();
    return detected;
  }

  /**
   * This method detects the AND event in RECENT context. It propagates
   * the merged event table to the subscribed events and executes the
   * rules defined in recent context.
   */

  private boolean detectRecent(PCTable initiator, PCTable terminator,Thread parent, RuleScheduler ruleScheduler) {
    PCEntry entry1, entry2;
    EventSet set1, set2;
    boolean detected = false;
    EventSet merged_set_R = new EventSet();

    // Get the recent entry from the initiator table
    entry1 = initiator.getRecentSet();

    // Get the recent entry from the terminator table
    entry2 = terminator.getRecentSet();

    if ((entry1 != null) && (entry2 != null)) {
      set1 = entry1.getEventSet();
      set2 = entry2.getEventSet();

      merged_set_R.union(set1,set2);

      if(eventDetectionDebug){
        System.out.println("\nEvent " + eventName +" was triggered at the context of RECENT.");
        merged_set_R.print();
      }

      // When this node is on local site, execute the rule associated event node.
      // Since there is no rule associated with the global node on the server

      if( eventCategory != EventCategory.GLOBAL){
        executeRules(merged_set_R,ParamContext.RECENT,  parent,ruleScheduler);
      }

   //   System.out.println("propagatePC at the recent conext");
      propagatePC(merged_set_R, ParamContext.RECENT);
   //   System.out.println("Finish propagatePC at the recent conext");

      //<GED>
      // When the event occurs at the producer site, packing the parameter lists into PCTable
      // and send to the server. (forwardFlag is true)
      // When the event occurs at the server, packint the parameter lists into PCTable
      // and send to the consumer. (sendbackFlag is true)

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

  /**
   * This method detects the AND event in CHRONICLE context. It propagates
   * the merged event table to the subscribed events and executes the
   * rules defined in chronicle context.
   */
  private boolean detectChronicle(PCTable initiator, PCTable terminator, Thread parent, RuleScheduler ruleScheduler) {
    PCEntry entry1, entry2;
    EventSet set1, set2;
    boolean detected = false;
    EventSet merged_set_H = new EventSet();

    // Get the oldest chronicle entry from the initiator table
    entry1 = initiator.getOldestChronSet();
    // Get the oldest chronicle entry from the terminator table
    entry2 = terminator.getOldestChronSet();

    if ((entry1 != null) && (entry2 != null)) {
      set1 = entry1.getEventSet();
      set2 = entry2.getEventSet();
      entry1.clearChronicle();
      entry2.clearChronicle();
      merged_set_H.union(set1, set2);

      if(eventDetectionDebug){
        System.out.println("\n\nEvent " + eventName +
            " was triggered at the context of CHRONICLE.");
        merged_set_H.print();
      }

      // When this node is on local site, execute the rule associated event node.
      // Since there is no rule associated with the global node on the server
      if( eventCategory != EventCategory.GLOBAL){
        executeRules(merged_set_H,ParamContext.CHRONICLE,  parent,ruleScheduler);
      }
      propagatePC(merged_set_H, ParamContext.CHRONICLE);
      detected = true;

      //<GED>
      // When the event occurs at the producer site, packing the parameter lists into PCTable
      // and send to the server. (forwardFlag is true)
      // When the event occurs at the server, packint the parameter lists into PCTable
      // and send to the consumer. (sendbackFlag is true)

      if(forwardFlag || sendBackFlag){
        gedEventTable.getCompositePropagation(merged_set_H,ParamContext.CHRONICLE.getId()) ;
        if(evntNotifDebug){
          System.out.println("Show the pctable that will sent to server");
          gedEventTable.print();
        }
      }
      //</GED>
    }
    return detected;
  }

  /**
   * This method detects the AND event in CONTINUOUS context. It propagates
   * the merged event table to the subscribed events and executes the
   * rules defined in continuous context.
   */
  private boolean detectContinuous(PCTable initiator, PCTable terminator,
      Thread parent, RuleScheduler ruleScheduler) {

    PCEntry entry1, entry2;
    EventSet set1, set2;
    boolean detected = false;
    EventSet merged_set_O = null;

    // Get the continuous entries from the initiator table
    Vector leftSetArry = initiator.getContiSets();

    // Get the continuous entries from the terminator table
    Vector rightSetArry = terminator.getContiSets();

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
          if(eventDetectionDebug)
            System.out.println("\n\nEvent " + eventName + " was triggered at the context of CONTINUOUS.");
          merged_set_O.print();

          // When this node is on local site, execute the rule associated event node.
          // Since there is no rule associated with the global node on the server

          if( eventCategory != EventCategory.GLOBAL){
            executeRules(merged_set_O,ParamContext.CONTINUOUS,parent,ruleScheduler);
          }

          propagatePC(merged_set_O, ParamContext.CONTINUOUS);
          detected = true;

          //<GED>
          // When the event occurs at the producer site, packing the parameter lists into PCTable
          // and send to the server. (forwardFlag is true)
          // When the event occurs at the server, packint the parameter lists into PCTable
          // and send to the consumer. (sendbackFlag is true)

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
    }
    return detected;
  }

  /**
   * This method detects the AND event in CUMULATIVE context. It propagates
   * the merged event table to the subscribed events and executes the
   * rules defined in cumulative context.
   */
  private boolean detectCumulative(PCTable initiator, PCTable terminator,
        Thread parent, RuleScheduler ruleScheduler) {

    PCEntry entry1, entry2;
    EventSet set1, set2;

    boolean detected = false;
    EventSet merged_set_U = new EventSet();

    // Get all the cumulative entries from the initiator table
    Vector setArry = initiator.getCumulSet();

    // Get the cumulative entries from the terminator table
    entry1 = terminator.getOldestCumulSet();

    if ((entry1 !=null) && (setArry.size() != 0)) {

      set1 = entry1.getEventSet();
      entry1.clearCumulative();
      for (int i = 0; i < setArry.size(); i++)
        merged_set_U.addElement((EventSet)(setArry.elementAt(i)));

      merged_set_U.addElement(set1);

      if(eventDetectionDebug){
        System.out.println("\n\nEvent " + eventName + " was triggered at the context of CUMULATIVE.");
        merged_set_U.print();
      }

      // When this node is on local site, execute the rule associated event node.
      // Sinc there is no rule associated with the global node on the server
      if( eventCategory != EventCategory.GLOBAL){
        executeRules(merged_set_U,ParamContext.CUMULATIVE,parent,ruleScheduler);
      }

      propagatePC(merged_set_U, ParamContext.CUMULATIVE);
      detected = true;

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
    return detected;
  }
  // modified by wtanpisu on 24 Jan 2000
}
