package sentinel.led;
import sentinel.comm.*;
import java.util.*;

/** The Plus class denotes an event node for the temporal event operator PLUS. It
 *  contains the event tables for the left and right children as well as
 *  references to the left and right event nodes. The Plus event is detected when the
 *  time specified in the right event expires since the occurrence of the left event.
 *  The right event is a primitive event denoting a temporal event. The timer is set
 *  with the time specified in the right event when the left event occurs.
 */
public class Plus extends Composite {

  PCTable leftEventTable = null;
  PCTable rightEventTable = null;
  Event leftEvent;
  Primitive rightEvent;
  String timeStr;
  private boolean eventDetectionDebug = Utilities.isDebugFlagTrue("eventDetectionDebug");

  public Plus(String eventName, EventHandle leftEventHandle, String timeString) {
    super(eventName);
    leftEvent = leftEventHandle.getEventNode();
    leftEventTable = new PCTable(this);
    rightEventTable = new PCTable(this);
    leftEvent.subscribe(this);
    rightEvent = new Primitive(timeString, null);
    this.timeStr = timeString;
    rightEvent.subscribe(this);
  }

  /** This method increments the counter for the given context
   *  recursively until leaf nodes are reached from this event
   *  node in the event graph.
   */
  public void setContextRecursive(int context){
    this.setContextCurrent(context);
    leftEvent.setContextRecursive(context);
    rightEvent.setContextRecursive(context);
  }

  /** This method decrements the counter for the given context
   *  recursively until leaf nodes are reached from this event
   *  node in the event graph.
   */
  public void resetContextRecursive(int context){
    this.resetContextCurrent(context);
    leftEvent.resetContextRecursive(context);
    rightEvent.resetContextRecursive(context);
  }

  public boolean merge(Table init, Table sec) {
    return false;
  }

  /** This method returns the left or right event tables of this event
   *  node. This method is called by other event nodes to which this
   *  event has subscribed.
   */
  protected Table getTable(Notifiable e) {
    if (leftEvent.equals((Event)e))
      return (Table)leftEventTable;
    else if (rightEvent.equals((Event)e))
      return (Table)rightEventTable;
    else
      return null;
  }

  private Vector chronEntries = new Vector();
  private Vector contiEntries = new Vector();
  private Vector cumulEntries = new Vector();



  // added by wtanpisu
  // rational : to utilize the rule scheduler.
  /** This method detects the event in the four parameter contexts
   *  when the timer notifies the occurrence of the right event.
   */
  private boolean detectEvent(long currEventId,
       Thread parent, RuleScheduler ruleScheduler) {
    boolean detected = false;
    if(eventDetectionDebug)
      printDetectionMask();
    if (recentCounter != 0)
      detected = detected | detectRecent(currEventId, parent,ruleScheduler );
    if (chronCounter != 0)
      detected = detected | detectChronicle(currEventId, parent,ruleScheduler);
    if (contiCounter != 0)
      detected = detected | detectContinuous(currEventId, parent,ruleScheduler);
    if (cumulCounter != 0)
      detected = detected | detectCumulative(currEventId, parent,ruleScheduler);

    leftEventTable.clearGarbage();
    rightEventTable.clearGarbage();
    return detected;
  }
  /** This method detects the event in RECENT context. It propagates
   *  the merged event table to the subscribed events and executes the
   *  rules defined in recent context.
   */
  private boolean detectRecent(long currEventId,
      Thread parent, RuleScheduler ruleScheduler) {
    PCEntry entry = null;
    EventSet merged_set_R = new EventSet();
    long refEventId;
    boolean detected = false;
    entry = leftEventTable.getRecentSet();
//    refEventId = entry.getTS().getSequence();
    refEventId = entry.getTS().getGlobalTick();
    if (currEventId == refEventId) {
      merged_set_R = entry.getEventSet();
      if(eventDetectionDebug)
        System.out.println("\nEvent " + eventName +
			  " was triggered at the context of RECENT");
      merged_set_R.print();
      executeRules(merged_set_R, ParamContext.RECENT, parent,ruleScheduler);
      propagatePC(merged_set_R,ParamContext.RECENT);
      detected = true;
    }
    else{
      if(eventDetectionDebug)
        System.out.println("Plus: not detecting event in Recent context");
    }
    return detected;
  }

  /** This method detects the event in CHRONICLE context. It propagates
   *  the merged event table to the subscribed events and executes the
   *  rules defined in chronicle context.
   */
  private boolean detectChronicle(long currEventId,
      Thread parent, RuleScheduler ruleScheduler) {
    PCEntry chronEntry = null;
    EventSet merged_set_H = new EventSet();
    long refEventId;
    boolean detected = false;

    // Find the chronicle entry that has set the timer for which
    // the current notification is received
    if(eventDetectionDebug)
      System.out.println("chronEntries.size() = " + chronEntries.size());
    for (int i=0; i<chronEntries.size(); i++) {
      chronEntry = (PCEntry) chronEntries.elementAt(i);
//		  refEventId = chronEntry.getTS().getSequence();
      refEventId = chronEntry.getTS().getGlobalTick();
      if (currEventId == refEventId)
        break;
    }
    if (chronEntry != null) {
      merged_set_H = chronEntry.getEventSet();
      if(eventDetectionDebug)
        System.out.println("\nEvent " + eventName +
            " was triggered at the context of CHRONICLE");
        merged_set_H.print();
        executeRules(merged_set_H, ParamContext.CHRONICLE, parent,ruleScheduler);
        propagatePC(merged_set_H,ParamContext.CHRONICLE);
        detected = true;
    }
    else{
      if(eventDetectionDebug)
        System.out.println("Plus: not detecting event in Chronicle context");
    }
    return detected;
  }

  /** This method detects the event in CONTINUOUS context. It propagates
   *  the merged event table to the subscribed events and executes the
   *  rules defined in continuous context.
   */
  private boolean detectContinuous(long currEventId,
      Thread parent, RuleScheduler ruleScheduler) {
    PCEntry contiEntry = null;
    EventSet merged_set_O = new EventSet();
    long refEventId;
    boolean detected = false;

    // Find the continuous entry that has set the timer for which
    // the current notification is received
    int contiEntryIndex = 0;
    for (int i=0; i<contiEntries.size(); i++) {
      contiEntry = (PCEntry) contiEntries.elementAt(i);
//		   refEventId = contiEntry.getTS().getSequence();
      refEventId = contiEntry.getTS().getGlobalTick();
      if (currEventId == refEventId) {
        contiEntryIndex = i;
        break;
      }
    }
    contiEntries.removeElementAt(contiEntryIndex);
    if (contiEntry != null) {
      merged_set_O = contiEntry.getEventSet();
      if(eventDetectionDebug)
        System.out.println("\nEvent " + eventName +
             " was triggered at the context of CONTINUOUS");
      merged_set_O.print();
      executeRules(merged_set_O, ParamContext.CONTINUOUS, parent,ruleScheduler);
      propagatePC(merged_set_O,ParamContext.CONTINUOUS);
      detected = true;
    }
    else{
      if(eventDetectionDebug)
        System.out.println("Plus: not detecting event in Continuous context");
    }
    return detected;
  }
  /** This method detects the event in CUMULATIVE context. It propagates
   *  the merged event table to the subscribed events and executes the
   *  rules defined in cumulative context.
   */
  private boolean detectCumulative(long currEventId,
      Thread parent, RuleScheduler ruleScheduler) {
    PCEntry cumulEntry = null;
    EventSet merged_set_U = new EventSet();
    long refEventId;
    boolean detected = false;

    for (int i=0; i<cumulEntries.size(); i++) {
      cumulEntry = (PCEntry) cumulEntries.elementAt(i);
      merged_set_U.addElement((EventSet)cumulEntry.getEventSet());
    }
    if (cumulEntries.size() != 0) {
      if(eventDetectionDebug)
        System.out.println("\nEvent " + eventName +
            " was triggered at the context of CUMULATIVE");
      merged_set_U.print();
      executeRules(merged_set_U, ParamContext.CUMULATIVE, parent,ruleScheduler);
      propagatePC(merged_set_U,ParamContext.CUMULATIVE);
      cumulEntries.removeAllElements();
      detected = true;
    }
    else{
      System.out.println("Plus: not detecting event in Cumulative context");
    }
    return detected;
  }

  public void notify(Notifiable event, Thread parent, Scheduler scheduler ) {
    RuleScheduler ruleScheduler = (RuleScheduler)scheduler;
    PCEntry entry = null;
    if (event == leftEvent) {
      if(eventDetectionDebug)
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
      if (entry == null) return;
        TimeStamp ts = entry.getTS();
//			long eventId = ts.getSequence();
        long eventId = ts.getGlobalTick();
      if(eventDetectionDebug)
        System.out.println("Setting the timer with timeString " + timeStr);

        // add 03/20
        Timer.timer.addItem(rightEvent,timeStr,eventId,ruleScheduler);
    }
    else if (event == rightEvent) {
      if(eventDetectionDebug)
        System.out.println("Notification received for right event");
        // Get the eventId stored in the parameter list denoting the
        // the left event that has set the timer
      PCEntry recentEntry = rightEventTable.getRecentSet();
      EventSet eventSet = null;
      if (recentEntry != null)
        eventSet = recentEntry.getEventSet();
      ParameterList paramList  = (ParameterList) eventSet.getParamLists().firstElement();
      long referenceEventId = -1;
      try {
        referenceEventId = paramList.getLong("eventId");
      }
      catch(ParameterNotFoundException pnfe) {
        pnfe.printStackTrace();
      }
      catch(TypeMismatchException tme) {
        tme.printStackTrace();
      }

      if (detectEvent(referenceEventId,parent,ruleScheduler))
        propagateEvent( parent,ruleScheduler);
    }
  }
  // added by wtanpisu on 24 Jan 2000
}
