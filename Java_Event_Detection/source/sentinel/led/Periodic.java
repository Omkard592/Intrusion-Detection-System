package sentinel.led;
import sentinel.comm.*;
import java.util.*;

/** The Periodic class denotes an event node for the temporal event operator PERIODIC. It
 *  contains the event tables for the left and middle children as well as
 *  references to the left, right and middle event nodes. The Periodic event is detected
 *  whenever the time specified in the right event expires since the occurrence of the left event.
 *  It is continuously detected until the right event occurs.
 *  The middle event is a primitive event denoting a temporal event. The timer is set
 *  with the time specified in the middle event when the left event occurs.
 */
public class Periodic extends Composite {
  PCTable leftEventTable = null;
  PCTable middleEventTable = null;
  Event leftEvent;
  Event rightEvent;
  Primitive middleEvent;
  String timeString;
  private Vector chronEntries = new Vector();
  private Vector contiEntries = new Vector();
  private Vector cumulEntries = new Vector();

  private boolean eventDetectionDebug = Utilities.isDebugFlagTrue("eventDetectionDebug");

  public Periodic(String eventName, EventHandle leftEventHandle,
				      String timeString, Object instance,
				      EventHandle rightEventHandle) {
    super(eventName);
    leftEvent = leftEventHandle.getEventNode();
    rightEvent = rightEventHandle.getEventNode();
    leftEventTable = new PCTable(this);
    middleEventTable = new PCTable(this);
    leftEvent.subscribe(this);
    rightEvent.subscribe(this);
    this.timeString = timeString;
    middleEvent = new Primitive(timeString, instance);

    middleEvent.subscribe(this);
  }

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

  /** This method returns the left or middle event tables of this event
    *  node. This method is called by other event nodes to which this
    *  event has subscribed.
    */
  public Table getTable(Notifiable e) {
    if (leftEvent.equals((Event)e))
      return (Table)leftEventTable;
    else if (middleEvent.equals((Event)e))
      return (Table)middleEventTable;
    else
      return null;
  }


  private long getReferenceEventId(PCEntry entry) {
    EventSet eventSet = entry.getEventSet();
    ParameterList paramList = (ParameterList) eventSet.getParamLists().firstElement();
    long eventId = 0;
    try {
      eventId = paramList.getLong("eventId");
    }
    catch(ParameterNotFoundException pnfe) {
      pnfe.printStackTrace();
    }
    catch(TypeMismatchException tme) {
      tme.printStackTrace();
    }
    return eventId;
  }

  private void cleanTable(PCTable table) {
    table.removeAllElements();
    chronEntries.removeAllElements();
    contiEntries.removeAllElements();
    cumulEntries.removeAllElements();
  }



  // added by wtanpisu
	// rational : to utilize the rule scheduler.

  private boolean merge(PCTable leftTable, PCTable middleTable,
						  Thread parent, RuleScheduler ruleScheduler) {
    boolean detected = false;
    if (eventDetectionDebug)
      printDetectionMask();
    if (recentCounter != 0)
      detected = detected | detectRecent(leftTable, middleTable, parent,ruleScheduler);
    if (chronCounter != 0)
      detected = detected | detectChronicle(leftTable, middleTable, parent,ruleScheduler);
    if (contiCounter != 0)
      detected = detected | detectContinuous(leftTable, middleTable, parent,ruleScheduler);
    if (cumulCounter != 0)
      detected = detected | detectCumulative(leftTable, middleTable, parent,ruleScheduler);
    leftTable.clearGarbage();
    middleTable.clearGarbage();
    return detected;
  }

  private boolean detectRecent(PCTable leftTable, PCTable middleTable,
						  Thread parent, RuleScheduler ruleScheduler) {
    PCEntry leftEntry = null;
    EventSet merged_set_R = new EventSet();
    EventSet set1, set2;
    boolean detected = false;
    long refEventId;

    leftEntry = leftTable.getRecentSet();
    if (leftEntry == null) {
      if (eventDetectionDebug)
        System.out.println("P Recent : RecentEntry is null, not detecting");
      return false;
    }
  //    long currEventId = leftEntry.getTS().getSequence();
    long currEventId = leftEntry.getTS().getGlobalTick();
    PCEntry middleEntry = middleTable.getRecentSet();
    long referenceEventId = getReferenceEventId(middleEntry);
    if (eventDetectionDebug)
      System.out.println("currEventId = " + currEventId);
    if (eventDetectionDebug)
      System.out.println("referenceEventId = " + referenceEventId);
    if (currEventId == referenceEventId) {
      set1 = leftEntry.getEventSet();
      set2 = middleEntry.getEventSet();
      merged_set_R.union(set1, set2);
      if (eventDetectionDebug)
        System.out.println("\nEvent " + eventName + " was triggered at the context of RECENT");
      merged_set_R.print();
      executeRules(merged_set_R, ParamContext.RECENT, parent,ruleScheduler);
      propagatePC(merged_set_R,ParamContext.RECENT);

      // add 03/20
      Timer.timer.addItem(middleEvent,timeString,referenceEventId,ruleScheduler);
      detected = true;
    }
    else{
      if (eventDetectionDebug)
        System.out.println("Periodic: not detecting event in Recent context");
    }
    return detected;
  }

  private boolean detectChronicle(PCTable leftTable, PCTable middleTable,
						  Thread parent, RuleScheduler ruleScheduler) {
    PCEntry chronEntry = null;
    EventSet merged_set_H = new EventSet();
    EventSet set1, set2;
    boolean detected = false;
    long currEventId = 0;

    PCEntry middleEntry = middleTable.getRecentSet();
    long referenceEventId = getReferenceEventId(middleEntry);

    // Find the chronicle entry that has set the timer for which
    // the current notification is received
    if (eventDetectionDebug)
      System.out.println("chronEntries.size() = " + chronEntries.size());
      for (int i=0; i<chronEntries.size(); i++) {
        chronEntry = (PCEntry) chronEntries.elementAt(i);
        //currEventId = chronEntry.getTS().getSequence();
        currEventId = chronEntry.getTS().getGlobalTick();
        // System.out.println("refEventId = " + refEventId);
        if (currEventId == referenceEventId) {
          chronEntries.removeElement(chronEntry);
          break;
        }
      }
    if (chronEntry != null) {
      if (eventDetectionDebug){
        System.out.println("currEventId = " + currEventId);

        System.out.println("referenceEventId = " + referenceEventId);
      }
      set1 = chronEntry.getEventSet();
      set2 = middleEntry.getEventSet();
      merged_set_H.union(set1, set2);
      System.out.println("\nEvent " + eventName +
          " was triggered at the context of CHRONICLE");
      merged_set_H.print();
      executeRules(merged_set_H, ParamContext.CHRONICLE, parent,ruleScheduler);
      propagatePC(merged_set_H,ParamContext.CHRONICLE);
      detected = true;
    }
    else{
      if (eventDetectionDebug)
        System.out.println("Periodic: not detecting event in Chronicle context");
    }
    return detected;
  }

  private boolean detectContinuous(PCTable leftTable, PCTable middleTable,
						  Thread parent, RuleScheduler ruleScheduler) {
    PCEntry contiEntry = null;
    EventSet merged_set_O = new EventSet();
    EventSet set1, set2;
    boolean detected = false;
    long currEventId = 0;

    PCEntry middleEntry = middleTable.getRecentSet();
    long referenceEventId = getReferenceEventId(middleEntry);

    // Find the continuous entry that has set the timer for which
    // the current notification is received
    //System.out.println("contiEntries.size() = " + contiEntries.size());
    for (int i=0; i<contiEntries.size(); i++) {
      contiEntry = (PCEntry) contiEntries.elementAt(i);
//			currEventId = contiEntry.getTS().getSequence();
      currEventId = contiEntry.getTS().getGlobalTick();
      // System.out.println("refEventId = " + refEventId);
      if (currEventId == referenceEventId) {
        contiEntries.removeElement(contiEntry);
        break;
      }
    }

    if (contiEntry != null) {
      if (eventDetectionDebug){
        System.out.println("currEventId = " + currEventId);
        System.out.println("referenceEventId = " + referenceEventId);
      }
      set1 = contiEntry.getEventSet();
      set2 = middleEntry.getEventSet();
      merged_set_O.union(set1, set2);
      if (eventDetectionDebug)
        System.out.println("\nEvent " + eventName +
            " was triggered at the context of CONTINUOUS");
      merged_set_O.print();
      executeRules(merged_set_O, ParamContext.CONTINUOUS, parent,ruleScheduler);
      propagatePC(merged_set_O,ParamContext.CONTINUOUS);
      detected = true;
    }
    else{
      if (eventDetectionDebug)
        System.out.println("Periodic: not detecting event in Continuous context");
    }
    return detected;
  }

  private boolean detectCumulative(PCTable leftTable, PCTable middleTable,
						  Thread parent, RuleScheduler ruleScheduler) {
    PCEntry cumulEntry = null;
    EventSet merged_set_U = new EventSet();
    EventSet set1, set2;
    boolean detected = false;
    long currEventId = 0;

    PCEntry middleEntry = middleTable.getRecentSet();
    long referenceEventId = getReferenceEventId(middleEntry);
    // Find the cumulative entry that has set the timer for which
    // the current notification is received
    if (eventDetectionDebug)
      System.out.println("cumulEntries.size() = " + cumulEntries.size());
      for (int i=0; i<cumulEntries.size(); i++) {
        cumulEntry = (PCEntry) cumulEntries.elementAt(i);
//			currEventId = cumulEntry.getTS().getSequence();
        currEventId = cumulEntry.getTS().getGlobalTick();
        // System.out.println("refEventId = " + refEventId);
        if (currEventId == referenceEventId) {
          cumulEntries.removeElement(cumulEntry);
          break;
      }
    }
    if (cumulEntry != null) {
      if (eventDetectionDebug){
        System.out.println("currEventId = " + currEventId);
        System.out.println("referenceEventId = " + referenceEventId);
      }
      set1 = cumulEntry.getEventSet();
      set2 = middleEntry.getEventSet();
      merged_set_U.union(set1, set2);
      if (eventDetectionDebug)
        System.out.println("\nEvent " + eventName + " was triggered " +
							   "in CUMULATIVE context");
        merged_set_U.print();
        executeRules(merged_set_U, ParamContext.CUMULATIVE, parent,ruleScheduler);
        propagatePC(merged_set_U,ParamContext.CUMULATIVE);
        detected = true;
    }
    else{
      if (eventDetectionDebug)
        System.out.println("Periodic: not detecting event in Cumulative context");
    }

    return detected;
  }

  public void notify(Notifiable event, Thread parent, Scheduler scheduler ) {
    RuleScheduler ruleScheduler = (RuleScheduler)scheduler;

    PCEntry entry = null;
    if (event == leftEvent) {
      if (eventDetectionDebug)
        System.out.println("Notification received for left event");
      if (recentCounter != 0)
        entry = leftEventTable.getRecentSet();
      if (chronCounter != 0) {
        // Find the chronicle entry that is setting the
        // timer. It is later required to match the
        // time stamps of this entry and the time stamp
        // of the temporal event that is notified by
        // the timer. The same is to be done for continuous
        // and cumulative detections also.
        entry = leftEventTable.getOldestChronSet();
        chronEntries.addElement(entry);
        entry.clearChronicle();
      }
      if (contiCounter != 0) {
        entry = leftEventTable.getOldestContiSet();
        contiEntries.addElement(entry);
        entry.clearContinuous();
      }
      if (cumulCounter != 0) {
        entry = leftEventTable.getOldestCumulSet();
        cumulEntries.addElement(entry);
        entry.clearCumulative();
      }
      if (entry == null)
        return;
      TimeStamp ts = entry.getTS();
//  		long eventId = ts.getSequence();
      long eventId = ts.getGlobalTick();
      if (eventDetectionDebug)
        System.out.println("Setting the timer with timeString " + timeString);

//## add 03/20
      Timer.timer.addItem(middleEvent,timeString,eventId,ruleScheduler);
    }
    else if (event == middleEvent) {
      if (eventDetectionDebug)
        System.out.println("Notification received for middle event");
      if (merge(leftEventTable,middleEventTable, parent,ruleScheduler))
        propagateEvent( parent,ruleScheduler);
    }
    else if (event == rightEvent) {
      if (eventDetectionDebug)
        System.out.println("Notification received for right event");
        cleanTable(leftEventTable);
        return;
    }
  }
  // added by wtanpisu on 24 Jan 2000
}
