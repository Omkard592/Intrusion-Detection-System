package sentinel.led;
import sentinel.comm.*;
import java.util.*;

/**
 * The AperiodicStar class denotes an event node for the composite event operator
 * APERIODICSTAR. It contains the event tables for the left, right and middle
 * children as well as references to the left, middle and right event nodes.
 * The propagated event contains the event occurrences from left, middle
 * and right event tables. The notify() method contains logic for detecting
 * the event according to the semantics of the operator.
 */

public class AperiodicStar extends Composite {

  PCTable leftEventTable = null;
  PCTable middleEventTable = null;
  PCTable rightEventTable = null;
  Event leftEvent;
  Event middleEvent;
  Event rightEvent;

  private boolean eventDetectionDebug = Utilities.isDebugFlagTrue("eventDetectionDebug");

  /**
   * Constructs a composite event: aperidoic* event
   *
   * @param eventName a value of type 'String'
   * @param leftEventHandle a value of type 'EventHandle'
   * @param middleEventHandle a value of type 'EventHandle'
   * @param rightEventHandle a value of type 'EventHandle'
   */
  public AperiodicStar(String eventName, EventHandle leftEventHandle,
                       EventHandle middleEventHandle,EventHandle rightEventHandle) {
    super(eventName);
    leftEvent = leftEventHandle.getEventNode();
    if (leftEvent == null) {
      if(eventDetectionDebug)
        System.out.println("AperiodicStar: Event "+leftEventHandle+" does not exist");
      return;
    }
    rightEvent = rightEventHandle.getEventNode();
    if (rightEvent == null) {
      if(eventDetectionDebug)
        System.out.println("AperiodicStar: Event "+rightEventHandle+" does not exist");
      return;
    }
    middleEvent = middleEventHandle.getEventNode();
    if (middleEvent == null) {
      if(eventDetectionDebug)
        System.out.println("AperiodicStar: Event "+rightEventHandle+" does not exist");
      return;
    }

    leftEventTable = new PCTable(this);
    middleEventTable = new PCTable(this);
    rightEventTable = new PCTable(this);
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


  /** This method clears the entries in the middle table that have
   *  participated in detection in the given context
   */
  private void clearMiddleTable(PCEntry entry, Vector middleEventSets, ParamContext context) {
    if (middleEventSets == null) {
      if(eventDetectionDebug)
	System.out.println("AperiodicStar: middleEventSets is null");
      return;
    }
    PCEntry currEntry = null;
    TimeStamp ts = null;
    for (int i=0; i<middleEventSets.size(); i++) {
      currEntry = (PCEntry) middleEventSets.elementAt(i);
      if (entry != null)
	ts = entry.getTS();
      else
	//	ts = new TimeStamp(TimeStamp.getSequenceCounter());
      	ts = new TimeStamp(TimeStamp.getTime());
      if (currEntry.olderThan(ts)) {
        switch (context.getId()) {
          case ParamContext.recentContext :
            currEntry.clearRecent();
            break;
          case ParamContext.chronContext :
            currEntry.clearChronicle();
            break;
          case ParamContext.contiContext :
            currEntry.clearContinuous();
            break;
          case ParamContext.cumulContext :
            currEntry.clearCumulative();
        }
      }
    }
  }

  /**
   * This method returns the left, right or middle event tables of this event
   * node. This method is called by other event nodes to which this
   * event has subscribed.
   */
  protected  Table getTable(Notifiable e) {
    if (leftEvent.equals((Event)e))
      return (Table)leftEventTable;
    else if(middleEvent.equals((Event)e))
      return (Table)middleEventTable;
    else if(rightEvent.equals((Event)e))
      return (Table)rightEventTable;
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

  // added by wtanpisu
  // rational : to utilize the rule scheduler.

  public boolean merge(PCTable leftTable,PCTable middleTable,PCTable rightTable,
	  Thread parent, RuleScheduler ruleScheduler) {
    if (leftTable.size() == 0) return false;
    if (rightTable.size() == 0) return false;
    boolean detected = false;


    if (eventDetectionDebug){
      printDetectionMask();
      System.out.println("\nLeft table->>");
      leftTable.print();
      System.out.println("\nRight table->>");
      rightTable.print();
    }
    if (middleTable != null) {
      if (eventDetectionDebug){
	System.out.println("\nMiddle table->>");
	middleTable.print();
      }
    }

    Vector middleEventSets = null;
    if (middleTable != null) {
      middleEventSets = middleTable.getEventSets();
      if(eventDetectionDebug)
        System.out.println("middleEventSets.size = " + middleEventSets.size());
    }
    if (recentCounter != 0) {
      detected = detected | detectRecent(leftTable,middleEventSets,rightTable, parent,ruleScheduler);
    }
    if (chronCounter != 0) {
      detected = detected | detectChronicle(leftTable,middleEventSets,rightTable, parent,ruleScheduler);
    }
    if (contiCounter != 0) {
      detected = detected | detectContinuous(leftTable,middleEventSets,rightTable, parent,ruleScheduler);
    }
    if (cumulCounter != 0) {
      detected = detected | detectCumulative(leftTable,middleEventSets,rightTable, parent,ruleScheduler);
    }

    leftTable.clearGarbage();
    rightTable.clearGarbage();
    return detected;
  }

  /**
   * This method detects the APERIODICSTAR event in RECENT context. It propagates
   * the merged event table to the subscribed events and executes the
   * rules defined in recent context.
   */
  private boolean detectRecent(PCTable leftTable, Vector middleEventSets,
	  	PCTable rightTable, Thread parent, RuleScheduler ruleScheduler) {
    EventSet merged_set_R = new EventSet();
    PCEntry entry1, entry2;
    EventSet set1, set2;
    boolean detected = false;

    entry1 = leftTable.getRecentSet();
    entry2 = rightTable.getRecentSet();

    if ((entry1 != null) && (entry2 != null)) {
      set1 = entry1.getEventSet();
      set2 = entry2.getEventSet();
      if(eventDetectionDebug)
        System.out.println("\nEvent " + eventName +
				  " was triggered at the context of RECENT");
      merged_set_R.union(set1, set2);
      PCEntry entry = null;

      // Find the middle event occurrences that have
      // occurred after the recent occurrence of the
      // left event
      if (middleEventSets != null) {
        for (int i=0; i<middleEventSets.size(); i++) {
          entry = (PCEntry) middleEventSets.elementAt(i);
          if (entry.youngerThan(entry1))
            merged_set_R.addElement(entry.getEventSet());
        }
      }
      clearMiddleTable(entry1, middleEventSets, ParamContext.RECENT);
      merged_set_R.print();
      executeRules(merged_set_R,ParamContext.RECENT, parent,ruleScheduler);
      propagatePC(merged_set_R, ParamContext.RECENT);
      detected = true;
    }
    return detected;
  }


  /** This method detects the APERIODICSTAR event in CHRONICLE context. It propagates
   *  the merged event table to the subscribed events and executes the
   *  rules defined in chronicle context.
   */
  private boolean detectChronicle(PCTable leftTable, Vector middleEventSets,
	  PCTable rightTable, Thread parent, RuleScheduler ruleScheduler) {

    EventSet merged_set_H = new EventSet();
    PCEntry entry1, entry2, newChronEty = null;
    EventSet set1, set2;
    boolean detected = false;

    entry1 = leftTable.getOldestChronSet();
    entry2 = rightTable.getOldestChronSet();

    if ((entry1 != null) && (entry2 != null)) {
      set1 = entry1.getEventSet();
      set2 = entry2.getEventSet();
      entry1.clearChronicle();
      entry2.clearChronicle();
      newChronEty = leftTable.findNextChronEty(entry1);
      // System.out.print("new ChronEty ->>  ");
      // newChronEty.print();
      merged_set_H.union(set1, set2);

      PCEntry entry = null;
      // Find the middle event occurrences that have
      // occurred after the chronicle occurrence of the left event
      if (middleEventSets != null) {
        for (int i=0; i<middleEventSets.size(); i++) {
          entry = (PCEntry) middleEventSets.elementAt(i);
          if (entry.isChronicle() && entry.youngerThan(entry1))
            merged_set_H.addElement(entry.getEventSet());
          }
        }if(eventDetectionDebug)
        System.out.println("\nEvent " + eventName +
				   " was triggered at the context of CHRONICLE.");
           merged_set_H.print();
           executeRules(merged_set_H,ParamContext.CHRONICLE, parent,ruleScheduler);
           propagatePC(merged_set_H, ParamContext.CHRONICLE);
           detected = true;
        }
        clearMiddleTable(newChronEty, middleEventSets, ParamContext.CHRONICLE);
        return detected;
    }

  /**
   * This method detects the  APERIODICSTAR event in CONTINUOUS context. It propagates
   * the merged event table to the subscribed events and executes the
   *  rules defined in continuous context.
   */
  private boolean detectContinuous(PCTable leftTable, Vector middleEventSets,
	PCTable rightTable,Thread parent, RuleScheduler ruleScheduler) {
    EventSet merged_set_O = null;
    PCEntry entry1, entry2;
    EventSet set1, set2;
    boolean detected = false;

    Vector leftSetArry = leftTable.getContiSets();
    Vector rightSetArry = rightTable.getContiSets();

    for (int i=0; i<leftSetArry.size(); i++) {
      entry1 = (PCEntry) leftSetArry.elementAt(i);
      entry1.clearContinuous();
    }
    for (int i=0; i<rightSetArry.size(); i++) {
      entry1 = (PCEntry) rightSetArry.elementAt(i);
      entry1.clearContinuous();
    }

    PCEntry currEntry;
    EventSet middleSet;
    EventSet tempSet;

    if ((leftSetArry.size()!=0) && (rightSetArry.size()!=0)) {
      for (int i=0; i<leftSetArry.size(); i++) {
        entry1 = (PCEntry) leftSetArry.elementAt(i);
        set1 = entry1.getEventSet();
        middleSet = new EventSet();
        // Find the middle event occurrences that have
	// occurred after the continuous occurrence of the left event
        for (int j=0; j<middleEventSets.size(); j++) {
          currEntry = (PCEntry) middleEventSets.elementAt(j);
          if (currEntry.isContinuous() && currEntry.youngerThan(entry1.getTS()))
            middleSet.addElement(currEntry.getEventSet());
        }
        tempSet = new EventSet();
        tempSet.union(set1,middleSet);

        for (int k=0; k<rightSetArry.size(); k++) {
          merged_set_O = new EventSet();
          entry2 = (PCEntry) rightSetArry.elementAt(k);
          entry2.clearContinuous();
          set2 = entry2.getEventSet();
          merged_set_O.union(tempSet,set2);
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
    clearMiddleTable(null,middleEventSets,ParamContext.CONTINUOUS);
    return detected;
  }

  /**
   * This method detects the APERIODICSTAR event in CUMULATIVE context. It propagates
   * the merged event table to the subscribed events and executes the
   * rules defined in cumulative context.
   */
  private boolean detectCumulative(PCTable leftTable, Vector middleEventSets,
       PCTable rightTable, Thread parent, RuleScheduler ruleScheduler) {

    EventSet merged_set_U = new EventSet();
    PCEntry entry1, entry2;
    EventSet set1, set2;
    boolean detected = false;

    entry1 = rightTable.getOldestCumulSet();
    Vector setArry = leftTable.getCumulSet();
    EventSet oldestLeftCumulEty = (EventSet) setArry.elementAt(0);
    if((entry1 != null) && (setArry.size() != 0)) {
      set1 = entry1.getEventSet();
      entry1.clearCumulative();
      PCEntry entry = null;
			// Find the middle event occurrences that have
			// occurred after the cumulative occurrence of the
			// left event
      if (middleEventSets != null) {
        for (int i=0; i<middleEventSets.size(); i++) {
          entry = (PCEntry) middleEventSets.elementAt(i);
          if (entry.isCumulative() && entry.youngerThan(oldestLeftCumulEty.getTS()))
            merged_set_U.addElement(entry.getEventSet());
        }
      }

      for(int i = 0; i < setArry.size(); i++)
        merged_set_U.addElement((EventSet)(setArry.elementAt(i)));
      merged_set_U.addElement(set1);
      if(eventDetectionDebug)
        System.out.println("\nEvent " + eventName + " was triggered at the context of CUMULATIVE.");
      merged_set_U.print();
      executeRules(merged_set_U,ParamContext.CUMULATIVE, parent,ruleScheduler);
      propagatePC(merged_set_U, ParamContext.CUMULATIVE);
      detected = true;
    }
    clearMiddleTable(null,middleEventSets,ParamContext.CUMULATIVE);
    return detected;
  }

  /** This method contains the detection logic according to the semantics
   *  of the APERIODICSTAR operator.
   */
  public void notify(Notifiable event, Thread parent, Scheduler scheduler ) {
    RuleScheduler ruleScheduler = (RuleScheduler) scheduler;
    if (event == middleEvent) {
      if (leftEventTable.size() == 0)
	cleanTable(middleEventTable);
      return;
    }
    boolean merged;
    if (event == rightEvent) {
      if (eventDetectionDebug) {
	System.out.println("Notification received for rightEvent");
	System.out.println("middleEventTable.size()="+middleEventTable.size());
      }
      if (middleEventTable.size() != 0)
	merged = merge(leftEventTable,middleEventTable,rightEventTable, parent,ruleScheduler);
      else
	merged = merge(leftEventTable,null,rightEventTable, parent,ruleScheduler);
      if (merged)
	propagateEvent( parent,ruleScheduler);
    }
  }
  // added by wtanpisu on 24 Jan 2000
}
