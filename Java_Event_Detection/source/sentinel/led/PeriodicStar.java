package sentinel.led;
import sentinel.comm.*;
import java.util.*;

/** The PeriodicStar class denotes an event node for the temporal event operator PERIODICSTAR. It
 *  contains the event tables for the left, middle and right children as well as
 *  references to the left, right and middle event nodes. The PeriodicStar event is detected
 *  when the right event occurs. All the middle event notifications are accumulated since the
 *  occurrence of the left event and propagated to the parent nodes along with the entries
 *  in the left and right event tables.
 *  The middle event is a primitive event denoting a temporal event. The timer is set
 *  with the time specified in the middle event when the left event occurs.
 */
public class PeriodicStar extends Composite {

  static boolean evntReqstDebug = DebuggingHelper.isDebugFlagTrue("evntReqstDebug");
  static boolean evntCreatnDebug = DebuggingHelper.isDebugFlagTrue("evntCreatnDebug");
  static boolean evntNotifDebug = DebuggingHelper.isDebugFlagTrue("evntNotifDebug");

  PCTable leftEventTable = null;
  PCTable middleEventTable = null;
  PCTable rightEventTable = null;
  Event leftEvent;
  Event rightEvent;
  Primitive middleEvent;
  String timeString;

	private boolean eventDetectionDebug = Utilities.isDebugFlagTrue("eventDetectionDebug");

  public PeriodicStar(String eventName, EventHandle leftEventHandle,
					  String timeString, Object instance,
					  EventHandle rightEventHandle) {
		super(eventName);
		leftEvent = leftEventHandle.getEventNode();
		if (leftEvent == null) {
      if(eventDetectionDebug)
  			System.out.println("PStar: Event "+leftEventHandle+" does not exist");
			return;
		}
		rightEvent = rightEventHandle.getEventNode();
		if (rightEvent == null) {
			if(eventDetectionDebug)
	  		System.out.println("PStar: Event "+rightEventHandle+" does not exist");
			return;
		}
		middleEvent = new Primitive(timeString, instance);

		leftEventTable = new PCTable(this);
		middleEventTable = new PCTable(this);
		rightEventTable = new PCTable(this);
		leftEvent.subscribe(this);
		middleEvent.subscribe(this);
		rightEvent.subscribe(this);
		this.timeString = timeString;
	}

	/** This method increments the counter for the given context
	 *  recursively until leaf nodes are reached from this event
	 *  node in the event graph.
	 */
    public  void setContextRecursive(int context){
		this.setContextCurrent(context);
		leftEvent.setContextRecursive(context);
		middleEvent.setContextRecursive(context);
	  rightEvent.setContextRecursive(context);
  }

	/** This method decrements the counter for the given context
	 *  recursively until leaf nodes are reached from this event
	 *  node in the event graph.
	 */
    public	void resetContextRecursive(int context){
		this.resetContextCurrent(context);
		leftEvent.resetContextRecursive(context);
		middleEvent.resetContextRecursive(context);
		rightEvent.resetContextRecursive(context);
  }

	/** This method returns the left, middle or right event tables of this event
	 *  node. This method is called by other event nodes to which this
	 *  event has subscribed.
	 */
  protected  Table getTable(Notifiable e) {
		if(leftEvent.equals((Event)e))
			return (Table)leftEventTable;
		else if(rightEvent.equals((Event)e))
			return (Table) rightEventTable;
		else if(middleEvent.equals((Event)e))
			return (Table) middleEventTable;
		else {
			System.out.println("PStar SERIOUS ERROR : getTable returning null");
			return null;
		}
  }


	private boolean currEntryMatchesWithRefEventId(PCEntry middleTableEntry, long refEventId) {
		EventSet eventSet = middleTableEntry.getEventSet();
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
		middleTableEntry.print();
		if(eventDetectionDebug)
		System.out.print("eventId = " + eventId);
		if (eventId == refEventId)
			return true;
		else
			return false;
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


	/** This method clears the entries in the middle table that have
	 *  participated in detection in the given context
	 */
    private void clearMiddleTable(PCEntry entry, Vector middleEventSets, ParamContext context) {
		if (middleEventSets == null) {
		if(	eventDetectionDebug)
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

    private Vector chronEntries = new Vector();
    private Vector contiEntries = new Vector();
    private Vector cumulEntries = new Vector();


  // added by wtanpisu
	// rational : to utilize the rule scheduler.
  /** This method detects the event in the four parameter contexts
	 *  when the right event occurs.
	 */

	public boolean merge(PCTable leftTable,PCTable middleTable,PCTable rightTable,
						  Thread parent, RuleScheduler ruleScheduler) {
    if (leftTable.size() == 0) return false;
    if (rightTable.size() == 0) return false;

    boolean detected = false;
    if(evntNotifDebug)
      printDetectionMask ();

    if (eventDetectionDebug){
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
		middleTable.clearGarbage();
    rightTable.clearGarbage();
    return detected;
  }

  /** This method detects the PERIODICSTAR event in RECENT context. It propagates
	 *  the merged event table to the subscribed events and executes the
	 *  rules defined in recent context.
	 */
  private boolean detectRecent(PCTable leftTable, Vector middleEventSets,
						  	PCTable rightTable,
	  Thread parent, RuleScheduler ruleScheduler) {
    EventSet merged_set_R = new EventSet();
    PCEntry entry1, entry2;
    EventSet set1, set2;
    boolean detected = false;
    entry1 = leftTable.getRecentSet();
    entry2 = rightTable.getRecentSet();

	  if ((entry1 != null) && (entry2 != null)) {
		  PCEntry middleEntry = middleEventTable.getRecentSet();
		  long referenceEventId = getReferenceEventId(middleEntry);
		  if(eventDetectionDebug)
		  System.out.println("Detecting RECENT ->>");
//			long currEventId = entry1.getTS().getSequence();
			long currEventId = entry1.getTS().getGlobalTick();
			if(eventDetectionDebug){
			System.out.println("currEventId = " + currEventId);
			System.out.println("referenceEventId = " + referenceEventId);
			}
			if (currEventId == referenceEventId) {
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
						if (!currEntryMatchesWithRefEventId(entry,referenceEventId))
							continue;
						if (entry.youngerThan(entry1))
							merged_set_R.addElement(entry.getEventSet());
					}
				}
				merged_set_R.print();
				executeRules(merged_set_R,ParamContext.RECENT, parent,ruleScheduler);
				propagatePC(merged_set_R, ParamContext.RECENT);
				detected = true;
				clearMiddleTable(entry1, middleEventSets, ParamContext.RECENT);
				Timer.timer.addItem(middleEvent,timeString,currEventId);
			}
    }
    return detected;
  }
/** This method detects the PERIODICSTAR event in CHRONICLE context. It propagates
	 *  the merged event table to the subscribed events and executes the
	 *  rules defined in chronicle context.
	 */
  private boolean detectChronicle(PCTable leftTable, Vector middleEventSets,
							  PCTable rightTable,
	  Thread parent, RuleScheduler ruleScheduler) {
    EventSet merged_set_H = new EventSet();
    PCEntry leftChronEntry = null;
	  PCEntry rightChronEntry, newChronEty = null;
    EventSet set1, set2;
    boolean detected = false;
    rightChronEntry = rightTable.getOldestChronSet();
	  PCEntry middleEntry = middleEventTable.getOldestChronSet();
	  long referenceEventId = getReferenceEventId(middleEntry);
	  if(eventDetectionDebug)
    System.out.println("chronEntries.size() = " + chronEntries.size());

	  for (int i=0; i<chronEntries.size(); i++) {
			leftChronEntry = (PCEntry) chronEntries.elementAt(i);
//  		long currEventId = leftChronEntry.getTS().getSequence();
  		long currEventId = leftChronEntry.getTS().getGlobalTick();
			// System.out.println("refEventId = " + refEventId);
			if (currEventId == referenceEventId) {
				chronEntries.removeElement(leftChronEntry);
				break;
			}
		}

		if ((leftChronEntry != null) && (rightChronEntry != null)) {
			if(eventDetectionDebug)
			System.out.println("Detecting CHRONICLE ->>");
//			long currEventId = leftChronEntry.getTS().getSequence();
			long currEventId = leftChronEntry.getTS().getGlobalTick();
			if(eventDetectionDebug){
			System.out.println("currEventId = " + currEventId);
			System.out.println("referenceEventId = " + referenceEventId);
			}
			if (currEventId == referenceEventId) {
				set1 = leftChronEntry.getEventSet();
				set2 = rightChronEntry.getEventSet();
				leftChronEntry.clearChronicle();
				rightChronEntry.clearChronicle();
				newChronEty = leftTable.findNextChronEty(leftChronEntry);
				merged_set_H.union(set1, set2);

				// Find the middle event occurrences that have
				// occurred after the chronicle occurrence of the
				// left event
				PCEntry entry = null;
				if (middleEventSets != null) {
					for (int i=0; i<middleEventSets.size(); i++) {
						entry = (PCEntry) middleEventSets.elementAt(i);
						if (!currEntryMatchesWithRefEventId(entry,referenceEventId))
							continue;
						if (entry.isChronicle() && entry.youngerThan(leftChronEntry))
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
		}
    return detected;
  }
	/** This method detects the  PERIODICSTAR event in CONTINUOUS context. It propagates
	 *  the merged event table to the subscribed events and executes the
	 *  rules defined in continuous context.
	 */
  private boolean detectContinuous(PCTable leftTable, Vector middleEventSets,
						PCTable rightTable,Thread parent, RuleScheduler ruleScheduler) {
    EventSet merged_set_O = null;
    PCEntry leftContiEntry = null;
		PCEntry rightContiEntry = null;
    EventSet set1, set2;
    boolean detected = false;
		long currEventId = 0;
    PCEntry middleEntry = middleEventTable.getOldestContiSet();
		long referenceEventId = getReferenceEventId(middleEntry);
	  for (int i=0; i<contiEntries.size(); i++) {
			leftContiEntry = (PCEntry) contiEntries.elementAt(i);
//			currEventId = leftContiEntry.getTS().getSequence();
			currEventId = leftContiEntry.getTS().getGlobalTick();
			// System.out.println("refEventId = " + refEventId);
			if (currEventId == referenceEventId) {
				contiEntries.removeElement(leftContiEntry);
				break;
			}
		}

		if (leftContiEntry == null) return false;
		Vector leftSetArry = new Vector();
		leftSetArry.addElement(leftContiEntry);
      Vector rightSetArry = rightTable.getContiSets();
      for (int i=0; i<rightSetArry.size(); i++) {
        rightContiEntry = (PCEntry) rightSetArry.elementAt(i);
        rightContiEntry.clearContinuous();
      }
      PCEntry currEntry;
      EventSet middleSet;
      EventSet tempSet;
      if ((leftSetArry.size()!=0) && (rightSetArry.size()!=0)) {
		  if(eventDetectionDebug){
			  System.out.println("Detecting CONTINUOUS ->>");
  			System.out.println("currEventId = " + currEventId);
	  		System.out.println("referenceEventId = " + referenceEventId);
		  }
		  	if (currEventId == referenceEventId) {
			  	for (int i=0; i<leftSetArry.size(); i++) {
				  	leftContiEntry = (PCEntry) leftSetArry.elementAt(i);
					  set1 = leftContiEntry.getEventSet();
  					middleSet = new EventSet();
	  				// Find the middle event occurrences that have
		  			// occurred after the continuous occurrence of the
  					// left event
	  				for (int j=0; j<middleEventSets.size(); j++) {
		  				currEntry = (PCEntry) middleEventSets.elementAt(j);
						if (!currEntryMatchesWithRefEventId(currEntry,referenceEventId))
							continue;
						if (currEntry.isContinuous() &&
							currEntry.youngerThan(leftContiEntry.getTS()))
							middleSet.addElement(currEntry.getEventSet());
					}
					tempSet = new EventSet();
					tempSet.union(set1,middleSet);
					for (int k=0; k<rightSetArry.size(); k++) {
						merged_set_O = new EventSet();
						rightContiEntry = (PCEntry) rightSetArry.elementAt(k);
						rightContiEntry.clearContinuous();
						set2 = rightContiEntry.getEventSet();
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
		}
		return detected;
  }
	/** This method detects the PERIODICSTAR event in CUMULATIVE context. It propagates
	 *  the merged event table to the subscribed events and executes the
	 *  rules defined in cumulative context.
	 */
  private boolean detectCumulative(PCTable leftTable, Vector middleEventSets,
				   PCTable rightTable,Thread parent, RuleScheduler ruleScheduler) {
    EventSet merged_set_U = new EventSet();
    PCEntry leftCumulEntry = null;
		PCEntry rightCumulEntry = null;
    EventSet set;
    boolean detected = false;
		long currEventId = 0;
    rightCumulEntry = rightTable.getOldestCumulSet();
		PCEntry middleEntry = middleEventTable.getOldestCumulSet();
		long referenceEventId = getReferenceEventId(middleEntry);

		for (int i=0; i<cumulEntries.size(); i++) {
			leftCumulEntry = (PCEntry) cumulEntries.elementAt(i);
//			currEventId = leftCumulEntry.getTS().getSequence();
			currEventId = leftCumulEntry.getTS().getGlobalTick();
			// System.out.println("refEventId = " + refEventId);
			if (currEventId == referenceEventId) {
				cumulEntries.removeElement(leftCumulEntry);
				break;
			}
		}
		Vector setArry = new Vector();
		setArry.addElement(leftCumulEntry);
    if((rightCumulEntry != null) && (setArry.size() != 0)) {
		if(eventDetectionDebug){
		  System.out.println("Detecting CUMULATIVE ->>");
			System.out.println("currEventId = " + currEventId);
			System.out.println("referenceEventId = " + referenceEventId);
		}
			if (currEventId == referenceEventId) {
				set = rightCumulEntry.getEventSet();
				rightCumulEntry.clearCumulative();

				// Find the middle event occurrences that have
				// occurred after the recent occurrence of the
				// left event
				PCEntry entry = null;
				if (middleEventSets != null) {
					for (int i=0; i<middleEventSets.size(); i++) {
						entry = (PCEntry) middleEventSets.elementAt(i);
						if (!currEntryMatchesWithRefEventId(entry,referenceEventId))
							continue;
						if (entry.isCumulative() &&
									entry.youngerThan(leftCumulEntry.getTS()))
							merged_set_U.addElement(entry.getEventSet());
					}
				}

				for(int i = 0; i < setArry.size(); i++)
					merged_set_U.addElement(((PCEntry)(setArry.elementAt(i))).getEventSet());
				merged_set_U.addElement(set);
				if(eventDetectionDebug)
				System.out.println("\nEvent " + eventName +
								   " was triggered at the context of CUMULATIVE.");
				merged_set_U.print();
				executeRules(merged_set_U,ParamContext.CUMULATIVE, parent,ruleScheduler);
				propagatePC(merged_set_U, ParamContext.CUMULATIVE);
				detected = true;
			}
			clearMiddleTable(null,middleEventSets,ParamContext.CUMULATIVE);
		}
		return detected;
  }

	public void notify(Notifiable event, Thread parent, Scheduler scheduler ) {
        RuleScheduler ruleScheduler = (RuleScheduler)scheduler;
			PCEntry entry = null;
		//long eventId = 0;

		if (event == leftEvent) {
			if (eventDetectionDebug) {
				System.out.println("rightEventTable.size= " + rightEventTable.size());
				System.out.println("leftEventTable.size= " + leftEventTable.size());
			}
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
			System.out.println("Setting the timer with timeString,eventId " + timeString + "," + eventId);
			Timer.timer.addItem(middleEvent,timeString,eventId);
		}
		else if (event == middleEvent) {
			if(eventDetectionDebug)
			System.out.println("Notification received for middleEvent");
			middleEventTable.print();
			PCEntry recentEntry = middleEventTable.getRecentSet();
			EventSet eventSet = null;
			long eventId = getReferenceEventId(recentEntry);
			if (leftEventTable.size() != 0) {
				if(eventDetectionDebug)
				System.out.println("Setting the timer with timeString,eventId " + timeString + "," + eventId);
				Timer.timer.addItem(middleEvent,timeString,eventId);
			}
		}

		else if (event == rightEvent) {
			if(eventDetectionDebug)
			System.out.println("Notification received for right event ..");
			boolean merged;
			if (middleEventTable.size() != 0)
				merged = merge(leftEventTable,middleEventTable,rightEventTable, parent,ruleScheduler);
			else
				merged = merge(leftEventTable,null,rightEventTable, parent,ruleScheduler);

			if (merged)
				propagateEvent( parent,ruleScheduler);
			return;
		}

	}
  // added by wtanpisu on 24 Jan 2000




}
