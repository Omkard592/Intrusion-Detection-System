/**
 * Aperiodic.java -
 * Author          : Seokwon Yang, H. Kim
 * Created On      : Jan ?? 1999
 * Last Modified By: Seokwon Yang
 * Last Modified On: Sun Sep 19 23:25:10 1999
 * Copyright (C) University of Florida 1999
 */


package sentinel.led;
import sentinel.comm.*;
import java.util.*;

  /** The Aperiodic class denotes an event node for the composite event operator APERIODIC. It
   *  contains the event tables for the left and middle children as well as
   *  references to the left, middle and right event nodes. There is not event table
   *  for right event because the propagated event does not contain the right event
   *  occurrences.
   *  The notify() method contains logic for detecting the event according to the semantics
   *  of the operator. The Aperiodic event is detected whenever the middle event occurs in the
   *  interval formed by the occurrences of the left and right events.
   */

public class Aperiodic extends Composite {

  PCTable leftEventTable = null;
  PCTable middleEventTable = null;
  Event leftEvent;
  Event middleEvent;
  Event rightEvent;

  static boolean evntReqstDebug = DebuggingHelper.isDebugFlagTrue("evntReqstDebug");
  static boolean evntCreatnDebug = DebuggingHelper.isDebugFlagTrue("evntCreatnDebug");
  static boolean evntNotifDebug = DebuggingHelper.isDebugFlagTrue("evntNotifDebug");



  private boolean eventDetectionDebug = Utilities.isDebugFlagTrue("eventDetectionDebug");

  /**
   * Constructs a composite event: aperidoic event
   *
   * @param eventName a value of type 'String'
   * @param leftEventHandle a value of type 'EventHandle'
   * @param middleEventHandle a value of type 'EventHandle'
   * @param rightEventHandle a value of type 'EventHandle'
   */
  public Aperiodic(String eventName, EventHandle leftEventHandle,
      EventHandle middleEventHandle,EventHandle rightEventHandle) {

    super(eventName);
    leftEvent = leftEventHandle.getEventNode();
    if (leftEvent == null) {
      if(eventDetectionDebug)
        System.out.println("Aperiodic: Event "+leftEventHandle+" does not exist");
      return;
    }
    rightEvent = rightEventHandle.getEventNode();
    if (rightEvent == null) {
      if(eventDetectionDebug)
        System.out.println("Aperiodic: Event "+rightEventHandle+" does not exist");
      return;
    }
    middleEvent = middleEventHandle.getEventNode();
    if (middleEvent == null) {
      if(eventDetectionDebug)
        System.out.println("Aperiodic: Event "+rightEventHandle+" does not exist");
      return;
    }

    leftEventTable = new PCTable(this);
    middleEventTable = new PCTable(this);
    leftEvent.subscribe(this);
    middleEvent.subscribe(this);
    rightEvent.subscribe(this);
  }

  /**
   * This method increments the counter for the given context
   * recursively until leaf nodes are reached from this event
   * node in the event graph.
   */
  public  void setContextRecursive(int context){
    this.setContextCurrent(context);
    leftEvent.setContextRecursive(context);
    middleEvent.setContextRecursive(context);
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
    middleEvent.resetContextRecursive(context);
    rightEvent.resetContextRecursive(context);
  }

  /**
   * This method returns the left or middle event tables of this event
   * node. This method is called by other event nodes to which this
   * event has subscribed.
   */
  public Table getTable(Notifiable e) {
    if (leftEvent.equals((Event)e))
      return (Table)leftEventTable;
    else if(middleEvent.equals((Event)e))
      return (Table)middleEventTable;
    else
      return null;
  }

  /**
   * This method removes all the elements from the given table
   * and sets the entries in the four contexts to null.
   */
  private void cleanTable(PCTable table) {
    table.removeAllElements();
    return;
  }

  // modified by wtanpisu
  // rational : to utilize the rule scheduler, parent and ruleSheduler parameters are added

  /**
   * The merge method merges the left and middle event tables
   * when the APERIODIC event is detected.
   */
  public boolean merge(PCTable initiator, PCTable terminator,
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
    if(evntNotifDebug)
      printDetectionMask();
    if (recentCounter != 0)
      detected = detected | detectRecent(initiator,terminator, parent,ruleScheduler);
    if (chronCounter != 0)
      detected = detected | detectChronicle(initiator,terminator, parent,ruleScheduler);
    if (contiCounter != 0)
      detected = detected | detectContinuous(initiator,terminator, parent,ruleScheduler);
    if (cumulCounter != 0)
      detected = detected | detectCumulative(initiator,terminator, parent,ruleScheduler);

    initiator.clearGarbage();
    terminator.clearGarbage();
    return detected;
  }


  /**
   * This method detects the APERIODIC event in RECENT context. It propagates
   * the merged event table to the subscribed events and executes the
   * rules defined in recent context.
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
      if(eventDetectionDebug)
        System.out.println("\nEvent " + eventName +
                        " was triggered at the context of RECENT");
      merged_set_R.union(set1, set2);
      merged_set_R.print();
      executeRules(merged_set_R,ParamContext.RECENT, parent,ruleScheduler);
      propagatePC(merged_set_R, ParamContext.RECENT);
      detected = true;
    }
    return detected;
  }


  /**
   * This method detects the APERIODIC event in CHRONICLE context. It propagates
   * the merged event table to the subscribed events and executes the
   * rules defined in chronicle context.
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
      if(eventDetectionDebug)
        System.out.println("\n\nEvent " + eventName +
                              " was triggered at the context of CHRONICLE.");
      merged_set_H.print();
      executeRules(merged_set_H,ParamContext.CHRONICLE, parent,ruleScheduler);
      propagatePC(merged_set_H, ParamContext.CHRONICLE);
      detected = true;
    }
    return detected;
  }


  /** This method detects the  APERIODIC event in CONTINUOUS context. It propagates
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
          if(eventDetectionDebug)
            System.out.println("\nEvent " + eventName +
                                " was triggered at the context of CONTINUOUS.");
          merged_set_O.print();
          executeRules(merged_set_O,ParamContext.CONTINUOUS, parent,ruleScheduler);
          propagatePC(merged_set_O, ParamContext.CONTINUOUS);
          detected = true;
        }
      }
    }
    return detected;
  }

  /**
   * This method detects the APERIODIC event in CUMULATIVE context. It propagates
   * the merged event table to the subscribed events and executes the
   * rules defined in cumulative context.
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
      if(eventDetectionDebug)
        System.out.println("\nEvent " + eventName +
            " was triggered at the context of CUMULATIVE");
      merged_set_U.print();
      executeRules(merged_set_U,ParamContext.CUMULATIVE, parent,ruleScheduler);
      propagatePC(merged_set_U, ParamContext.CUMULATIVE);
      detected = true;
    }
    return detected;
  }

  /**
   * This method contains the detection logic according to the semantics
   * of the APERIODIC operator.
   */
  public void notify(Notifiable event, Thread parent, Scheduler scheduler ) {
    RuleScheduler ruleScheduler = (RuleScheduler)scheduler;

    if (event == rightEvent) {
      if(eventDetectionDebug)
        System.out.println("Notification received for rightEvent");
      cleanTable(leftEventTable);
      return;
    }
    else if (event == leftEvent) {
      if (middleEventTable.size() != 0)
      cleanTable(middleEventTable);
    }
    else if (event == middleEvent) {
      if(eventDetectionDebug)
        System.out.println("Notification received for middleEvent");
      if (leftEventTable.size() != 0) {
        boolean merged = merge(leftEventTable,middleEventTable, parent,ruleScheduler);
      if (merged)
	propagateEvent( parent,ruleScheduler);
      }
    }
  }
  // modified on 01/24/2000
}
