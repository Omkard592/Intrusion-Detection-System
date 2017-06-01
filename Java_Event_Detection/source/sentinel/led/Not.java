/**
 * Not.java --
 * Author          : Seokwon Yang, H. Kim
 * Created On      : Jan ?? 1999
 * Last Modified By: Seokwon Yang
 * Last Modified On: Thu Aug 12 02:33:20 1999
 * Copyright (C) University of Florida 1999
 */

package sentinel.led;
import sentinel.comm.*;
import java.util.*;

/** The Not class denotes an event node for the composite event operator NOT. It
 *  contains the event tables for the left and right children as well as
 *  references to the left, middle and right event nodes. There is not event table
 *  for middle event because the propagated event does not contain the middle event
 *  occurrences.
 *  The notify() method contains logic for detecting the event according to the semantics
 *  of the operator. The NOT event is detected when the right event occurs and the middle
 *  event does not occur in the interval formed by the occurrences of the left and
 *  right events.
 */
public class Not extends Composite {

  static boolean evntNotifDebug = DebuggingHelper.isDebugFlagTrue("evntNotifDebug");


  private boolean eventDetectionDebug = Utilities.isDebugFlagTrue("eventDetectionDebug");


  private PCTable leftEventTable = null;
  private PCTable rightEventTable = null;
  private Event leftEvent;
  private Event middleEvent;
  private Event rightEvent;

  //<GED>
  private SentinelComm commInterf = null;

  /**
   * This PCTable contains the parameter lists and contexts of the event set.
   * The table is sent to the server, when the forward flag of this event is set.
   * The table is sent to the consumer, when the sendback flag of this event is set.
   */

  private PCTable gedEventTable = null;
  //</GED>




  public Not(String eventName, EventHandle leftEventHandle,
    EventHandle middleEventHandle, EventHandle rightEventHandle){
    super(eventName);
    initialize(eventName,leftEventHandle,middleEventHandle, rightEventHandle);
    eventCategory = EventCategory.LOCAL;
  }

  private void initialize(String eventName, EventHandle leftEventHandle,
        EventHandle middleEventHandle, EventHandle rightEventHandle){

    leftEvent = leftEventHandle.getEventNode();
    if (leftEvent == null) {
      if (eventDetectionDebug)
	System.out.println("And: Event "+leftEventHandle+" does not exist");
      return;
    }
    rightEvent = rightEventHandle.getEventNode();
    if (rightEvent == null) {
      if (eventDetectionDebug)
        System.out.println("And: Event "+rightEventHandle+" does not exist");
      return;
    }

    middleEvent = middleEventHandle.getEventNode();
    if (middleEvent == null) {
      if (eventDetectionDebug)
	System.out.println("And: Event "+middleEventHandle+" does not exist");
      return;
    }

    leftEventTable = new PCTable(this);
    rightEventTable = new PCTable(this);

    leftEvent.subscribe(this);
    middleEvent.subscribe(this);
    rightEvent.subscribe(this);
  }

  //<GED>
  public Not(String eventName, EventHandle leftEventHandle,EventHandle middleEventHandle, EventHandle rightEventHandle, SentinelComm commIntf){
    super(eventName);
    initialize(eventName,leftEventHandle,middleEventHandle, rightEventHandle);
    eventCategory = EventCategory.GLOBAL;
    commInterf = commIntf;
  }
  //</GED>



  /** This method increments the counter for the given context
   *  recursively until leaf nodes are reached from this event
   *  node in the event graph.
   */
  public void setContextRecursive(int context){
    this.setContextCurrent(context);
	leftEvent.setContextRecursive(context);
	middleEvent.setContextRecursive(context);
	rightEvent.setContextRecursive(context);
  }

  /** This method decrements the counter for the given context
   *  recursively until leaf nodes are reached from this event
   *  node in the event graph.
   */
  public void resetContextRecursive(int context){
    this.resetContextCurrent(context);
	leftEvent.resetContextRecursive(context);
	middleEvent.resetContextRecursive(context);
	rightEvent.resetContextRecursive(context);
  }


  /** This method returns the left or right event tables of this event
   *  node. This method is called by other event nodes to which this
   *  event has subscribed.
   */
  protected Table getTable(Notifiable e) {
    if (leftEvent.equals((Event)e))
	  return (Table)leftEventTable;
	else if(rightEvent.equals((Event)e))
	  return (Table)rightEventTable;
	else
	  return null;
  }

  /** This method removes all the elements from the given table
   *  and sets the entries in the four contexts to null.
   */
  private void cleanTable(PCTable table) {
    table.removeAllElements();
    return;
  }

  // added by wtanpisu
  /** The merge method merges the left and right event tables
   *  when the NOT event is detected.
   */
  public boolean merge(PCTable initiator, PCTable terminator,
      Thread parent, RuleScheduler ruleScheduler) {
    if(initiator.size() == 0) return false;
    if(terminator.size() == 0) return false;
    if(eventDetectionDebug)
	  System.out.println("\nInitiator table->>");
    initiator.print();
    if (eventDetectionDebug)
      System.out.println("\nTerminator table->>");
    terminator.print();

    boolean detected = false;
    if(evntNotifDebug)
      printDetectionMask();



    //<GED>
    /**
     * Instantiate the pctable so that we can packing the
     * parameter lists into this object.
     */
    if(sendBackFlag == true || forwardFlag == true)
      gedEventTable  = new PCTable();
    //</GED>

    if (recentCounter != 0) {
      detected = detected | detectRecent(initiator,terminator, parent,ruleScheduler);
    }
    if (chronCounter != 0) {
      detected = detected | detectChronicle(initiator,terminator, parent,ruleScheduler);
    }
    if (contiCounter != 0) {
      detected = detected | detectContinuous(initiator,terminator, parent,ruleScheduler);
    }
    if (cumulCounter != 0) {
      detected = detected | detectCumulative(initiator,terminator, parent,ruleScheduler);
    }

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
     * When the server detects the occurence of event and the sendBackFlag is set
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
    initiator.clearGarbage();
    terminator.clearGarbage();
    return detected;
  }

  /** This method detects the NOT event in RECENT context. It propagates
   *  the merged event table to the subscribed events and executes the
   *  rules defined in recent context.
   */
  private boolean detectRecent(PCTable initiator, PCTable terminator,
	        			  Thread parent, RuleScheduler ruleScheduler) {
    PCEntry entry1, entry2;
    EventSet set1, set2;
    boolean detected = false;
    EventSet merged_set_R = new EventSet();
    entry1 = initiator.getRecentSet();
    entry2 = terminator.getRecentSet();
    if ((entry1 != null) && (entry2 != null)) {
      set1 = entry1.getEventSet();
      set2 = entry2.getEventSet();
      if (eventDetectionDebug)
        System.out.println("\nEvent " + eventName + " was triggered at the context of RECENT");
      merged_set_R.union(set1, set2);
      merged_set_R.print();

      /* When this node is on local site */

      if( eventCategory != EventCategory.GLOBAL){
        executeRules(merged_set_R,ParamContext.RECENT, parent,ruleScheduler);
      }
      propagatePC(merged_set_R, ParamContext.RECENT);
      detected = true;

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
    return detected;
  }

  /** This method detects the NOT event in CHRONICLE context. It propagates
   *  the merged event table to the subscribed events and executes the
   *  rules defined in chronicle context.
   */
  private boolean detectChronicle(PCTable initiator, PCTable terminator,
						  Thread parent, RuleScheduler ruleScheduler) {
    PCEntry entry1, entry2;
    EventSet set1, set2;
    boolean detected = false;
    EventSet merged_set_H = new EventSet();

    entry1 = initiator.getOldestChronSet();
    entry2 = terminator.getOldestChronSet();
    if ((entry1 != null) && (entry2 != null)) {
      set1 = entry1.getEventSet();
      set2 = entry2.getEventSet();
      entry1.clearChronicle();
      entry2.clearChronicle();
      merged_set_H.union(set1, set2);
      if (eventDetectionDebug)
        System.out.println("\n\nEvent " + eventName +
                              " was triggered at the context of CHRONICLE.");
      merged_set_H.print();

      /* When this node is on local site */

      if( eventCategory != EventCategory.GLOBAL){
        executeRules(merged_set_H,ParamContext.CHRONICLE, parent,ruleScheduler);
      }
      propagatePC(merged_set_H, ParamContext.CHRONICLE);
      detected = true;

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
    return detected;
  }

  /** This method detects the NOT event in CONTINUOUS context. It propagates
   *  the merged event table to the subscribed events and executes the
   *  rules defined in continuous context.
   */
  private boolean detectContinuous(PCTable initiator, PCTable terminator,
						  Thread parent, RuleScheduler ruleScheduler) {
    PCEntry entry1, entry2;
    EventSet set1, set2;
    boolean detected = false;
    EventSet merged_set_O = null;
    Vector leftSetArry = initiator.getContiSets();
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
		  if (eventDetectionDebug)
            System.out.println("\nEvent " + eventName +
			    " was triggered at the context of CONTINUOUS.");
          merged_set_O.print();

          /* When this node is on local site */

          if( eventCategory != EventCategory.GLOBAL){
            executeRules(merged_set_O,ParamContext.CONTINUOUS, parent,ruleScheduler);
          }
          propagatePC(merged_set_O, ParamContext.CONTINUOUS);
          detected = true;

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
    }
    return detected;
  }

  /** This method detects the NOT event in CUMULATIVE context. It propagates
   *  the merged event table to the subscribed events and executes the
   *  rules defined in cumulative context.
   */
  private boolean detectCumulative(PCTable initiator, PCTable terminator,
      Thread parent, RuleScheduler ruleScheduler) {

    PCEntry entry1, entry2;
    EventSet set1, set2;
    boolean detected = false;
    EventSet merged_set_U = new EventSet();
    entry1 = terminator.getOldestCumulSet();
    Vector setArry = initiator.getCumulSet();

    if((entry1 !=null) && (setArry.size() != 0)) {
      set1 = entry1.getEventSet();
      entry1.clearCumulative();
      for(int i = 0; i < setArry.size(); i++)
        merged_set_U.addElement((EventSet)(setArry.elementAt(i)));
      merged_set_U.addElement(set1);
      if (eventDetectionDebug)
        System.out.println("\nEvent " + eventName +
		    " was triggered at the context of CUMULATIVE");
      merged_set_U.print();

      /* When this node is on local site */
      if( eventCategory != EventCategory.GLOBAL){
        executeRules(merged_set_U,ParamContext.CUMULATIVE, parent,ruleScheduler);
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


  /** This method contains the detection logic according to the semantics
   *  of the NOT operator. When the middle event is notified, the left
   *  event table is cleaned. The right event table is cleaned when the
   *  left event is notified.
   */
  public void notify(Notifiable event, Thread parent, Scheduler scheduler ) {
    RuleScheduler ruleScheduler = (RuleScheduler)scheduler;

    if (event == middleEvent) {
    if (eventDetectionDebug)
      System.out.println("Not: Notification received for middleEvent");
      if (leftEventTable.size() != 0)
	cleanTable(leftEventTable);
      return;
    }
    else if (event == leftEvent) {
      if (rightEventTable.size() != 0)
	cleanTable(rightEventTable);
      return;
    }
    else if (event == rightEvent) {
      if (eventDetectionDebug)
	System.out.println("Not: Notification received for rightEvent");
      if (leftEventTable.size() != 0) {
	if (eventDetectionDebug)
	  System.out.println("leftEventTable.size()="+leftEventTable.size());
	boolean merged = merge(leftEventTable,rightEventTable, parent,ruleScheduler);
	if (merged)
	  propagateEvent( parent,ruleScheduler);
      }
    }
  }
  // added by wtanpisu on 24 Jan 2000
}
