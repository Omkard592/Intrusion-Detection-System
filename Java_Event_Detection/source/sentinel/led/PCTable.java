/**
 * PCTable.java --
 * Author          : Seokwon Yang
 * Created On      : Fri Jan  8 23:06:54 1999
 * Last Modified By: Seokwon Yang
 * Last Modified On: Fri Jan  8 23:06:56 1999
 * RCS             : $Id: header.el,v 1.1 1997/02/17 21:45:38 seyang Exp seyang $
 * Copyright (C) University of Florida 1999
 */
package sentinel.led;
import java.util.*;
import java.io.*;

/** The PCTable class denotes an event table. It stores a Vector of event entries
 *  (PCEntries). This class contains methods that return selected event entries from
 *  the event table.
 */
public class PCTable extends Table implements Serializable{

  //<GED>
  /** Flag indicates that this PCTable is belong to primitive event */
  private boolean forPrimitive = false;

  /** Set the forPrimitive flag*/
  public void setForPrimitive(boolean bValue){
    forPrimitive = bValue;
  }
  /** Return the forPrimitive flag */
  public boolean isForPrimitive(){
    return forPrimitive;
  }
  //</GED>

  private PCEntry RecentEty = null;

  /** Constructor */
  // Not sure why we have Event e as an argument here
  public PCTable(Event e) {
    pcEntries = new Vector();
  }

  // Constructor
  public PCTable() {
    pcEntries = new Vector();
  }

  /**
   * This method returns the most recent entry from the event table.
   */
  public PCEntry getRecentSet() {
    int size = pcEntries.size();
    if (size == 0) return null;

    int i;
    PCEntry entry = null;
    PCEntry RecentEty = null;
    for (i=0; i<size; i++) {
      entry = (PCEntry) pcEntries.elementAt(i);
      if (entry.isRecent()) {
	RecentEty = entry;
	break;
      }
    }
    for (;i<size; i++) {
      entry = (PCEntry) pcEntries.elementAt(i);
      if (entry.isRecent() && entry.youngerThan(RecentEty))
	RecentEty = entry;
    }
    return RecentEty;
  }

  void removeAllElements() {
    // System.out.println("Removing all elements from PCTable");
    pcEntries.removeAllElements();
  }

  /** This method returns the oldest event entry from the event table that
   *  has not participated in event detection.
   */
  PCEntry getOldestChronSet() {
    int size = pcEntries.size();
    if (size == 0) return null;
    int i;
    PCEntry entry = null;
    PCEntry ChronEty = null;
    for (i=0; i<size; i++) {
      entry = (PCEntry) pcEntries.elementAt(i);
      if (entry.isChronicle()) {
	ChronEty = entry;
	break;
      }
    }
    for (;i<size; i++) {
      entry = (PCEntry) pcEntries.elementAt(i);
      if (entry.isChronicle() && entry.olderThan(ChronEty))
        ChronEty = entry;
    }
    return ChronEty;
  }

  /** This method returns the oldest Continuous event entry from the event table that
   *  has not participated in event detection.
   */
  PCEntry getOldestContiSet() {
    int size = pcEntries.size();
    if (size == 0) return null;
    int i;
    PCEntry entry = null;
    PCEntry ContiEty = null;
    for (i=0; i<size; i++) {
      entry = (PCEntry) pcEntries.elementAt(i);
      if (entry.isContinuous()) {
        ContiEty = entry;
        break;
      }
    }
    for (;i<size; i++) {
      entry = (PCEntry) pcEntries.elementAt(i);
      if (entry.isContinuous() && entry.olderThan(ContiEty))
        ContiEty = entry;
    }
    return ContiEty;
  }

  /** This method returns the set of all continuous event entries from the event
   *  table that have not participated in event detection.
   */
  public Vector getContiSets() {
    Vector contis = new Vector();
    int size = pcEntries.size();
    PCEntry entry = null;
    for (int i=0; i<size; i++) {
      entry = (PCEntry) pcEntries.elementAt(i);
      if (entry.isContinuous())
	contis.addElement(entry);
    }
    return contis;
  }

  /** This method returns the oldest Cumulative event entry from the event table that
   *  has not participated in event detection.
   */
  PCEntry getOldestCumulSet() {
    int size = pcEntries.size();
    if (size == 0) return null;

    int i;
    PCEntry entry = null;
    PCEntry CumulEty = null;
    for (i=0; i<size; i++) {
      entry = (PCEntry) pcEntries.elementAt(i);
      if (entry.isCumulative()) {
        CumulEty = entry;
        break;
      }
    }
    for (;i<size; i++) {
      entry = (PCEntry) pcEntries.elementAt(i);
      if (entry.isCumulative() && entry.olderThan(CumulEty))
        CumulEty = entry;
    }
    return CumulEty;
  }

  /** This method returns the set of all cumulative event entries from the event
   *  table that have not participated in event detection.
   */
  public Vector getCumulSet() {
    Vector cumuls = new Vector();
    for(int index = 0;index < pcEntries.size(); index++) {
      PCEntry e = (PCEntry)pcEntries.elementAt(index);
      if(e.isCumulative()) {
        cumuls.addElement(e.getEventSet());
        e.clearCumulative();
      }
    }
    return cumuls;
  }

  /** This method returns the next chronicle entry from the event table.
  */
  PCEntry findNextChronEty(PCEntry ety) {
    int index = pcEntries.indexOf(ety);
    for(index++;index < pcEntries.size(); index++) {
      PCEntry e = (PCEntry)pcEntries.elementAt(index);
      if (e.isChronicle()) {
        return e;
      }
    }
    return null;
  }

  /** This method returns the next continuous entry from the event table.
  */
  void findNextContiEty(PCEntry ety) {
    int index = pcEntries.indexOf(ety);
    for(index++;index < pcEntries.size(); index++) {
      PCEntry e = (PCEntry)pcEntries.elementAt(index);
      if (e.isContinuous()) {
        return;
      }
    }
    return;
  }

  /** This method returns the next cumulative entry from the event table.
  */
  void findNextCumulEty(PCEntry ety) {
    int index = pcEntries.indexOf(ety);
    for(index++;index < pcEntries.size(); index++) {
      PCEntry e = (PCEntry)pcEntries.elementAt(index);
      if (e.isCumulative()) {
        return;
      }
    }
    return;
  }

  /** This method removes the given entry from the event table.
   */
  void removeEntry(PCEntry entry) {
    pcEntries.removeElement(entry);
  }


  /** This method returns the PCEntry at the given position from the event table
  */
  PCEntry elementAt(int i) {
    return ((PCEntry) pcEntries.elementAt(i));
  }

  /** This method returns the time stamp of the youngest entry in the
   *  event table.
   */
  TimeStamp getYoungestTS() {
    PCEntry entry = null;
    int size = pcEntries.size();
    if (size == 0) return null;

    entry = (PCEntry) pcEntries.elementAt(0);
    TimeStamp youngTS = entry.getTS();
    for (int i=1; i<size; i++) {
      entry = (PCEntry) pcEntries.elementAt(i);
      if (entry.youngerThan(youngTS))
        youngTS = entry.getTS();
    }
    return youngTS;
  }

  /** This method returns true if the event entry contains the given
  *  event set.
  */
  private boolean contains(EventSet set) {
    if(pcEntries.size() == 0) return false;
    Enumeration en = pcEntries.elements();
    while(en.hasMoreElements()) {
      PCEntry entry = (PCEntry)en.nextElement();
      if(entry.contains(set))
       return true;
    }
    return false;
  }

  /** This method finds the event entry containing the given event set.
   */
  PCEntry findEntry(EventSet set) {
    if(pcEntries.size() == 0) return null;
      Enumeration en = pcEntries.elements();
      while(en.hasMoreElements()) {
        PCEntry entry = (PCEntry)en.nextElement();
        if(entry.contains(set))
          return entry;
      }
    return null;
  }

  private void reset_Oldest_Recent() {
    if (RecentEty != null)
      RecentEty.clearRecent();
 }

  /** This method propagates a primitive event occurrence.
   */
  public void getPropagation(ParameterList paramList, byte context) {
//    System.out.println("PCTable::getPropagation");
//    System.out.println("Context"+context);
    EventSet set = new EventSet();
//    System.out.println("add the paramList in the set vector in eventset");
    set.addElement(paramList);
    TimeStamp ts = paramList.getTS();
    set.setTS(ts);
    getPropagation(set,context);
  }

  /**
   * This method propagates a composite event occurrence.
   */
  void getPropagation(EventSet set, byte context) {
    PCEntry entry = new PCEntry(set);
    entry.setContext(context);
 //   System.out.println("add the entry in the pcEntries vector!");
    pcEntries.addElement(entry);
    if (entry.isRecent()) {
      if (RecentEty != null) {
	if (entry.youngerThan(RecentEty)) {
   //       System.out.println("clear the recent entry and update the new one");
	  RecentEty.clearRecent();
	  RecentEty = entry;
	}
      }
      else
	RecentEty = entry;
    }
  }


  //<GED>
  /**
   * This method propagates a global event occurrence.
   */
  public void getPropagation(Vector pcEntriesFromProd) {
    //System.out.println("PCTable::getPropagation");

    // The parameter lists are in pcEntries when the global event is notified.
    // Propagate the parameter list from the pcEntries into its parent PCTable

    for(int i=0; i < pcEntriesFromProd.size(); ++i){
      PCEntry entryFromProd = (PCEntry) pcEntriesFromProd.elementAt(i);
      EventSet evntSetFromProd = entryFromProd.getEventSet();

      PCEntry entry = findEntry(evntSetFromProd); // not exists, return null

      pcEntries.addElement(entryFromProd);

      if(entryFromProd.isRecent()){
       if (RecentEty != null) {
	if (entryFromProd.youngerThan(RecentEty)) {
	  RecentEty.clearRecent();
	  RecentEty = entryFromProd;
        }
      }
      else
        RecentEty = entryFromProd;
    }

    // if the entry exist, adjust the context bits
    if(entry != null)
      adjustBits(entry,entryFromProd);
    }
  }
  //</GED>



  /** This method propagates a composite event occurrence and also adjusts the
  *  context bits of the event entries accordingly.
  */
  public void getCompositePropagation(EventSet set, int context) {
   // System.out.println("PCTable::getCompositePropagation");
    PCEntry entry;
    entry = findEntry(set); // not exists, return null
    if (entry == null) {
      entry = new PCEntry(set);
      pcEntries.addElement(entry);
      entry.initializeContext();
    }
    if (context == ParamContext.recentContext) {
      if (RecentEty != null) {
        if (entry.youngerThan(RecentEty)) {
          RecentEty.clearRecent();
          RecentEty = entry;
        }
      }
      else
        RecentEty = entry;
    }
    // adjust existing bits
    adjustBits(entry, context);
  }

  private void adjustBits(PCEntry entry, int context) {
    entry.setBit(context);
  }

  //<GED>
  /**
   * Adjust the context bits PCTable of the global event.
   */
  private void adjustBits(PCEntry entry,PCEntry entryFromProd) {
    if(entryFromProd.isRecent())
      entry.setBit(ParamContext.recentContext);
    if(entryFromProd.isChronicle())
      entry.setBit(ParamContext.contiContext);
    if(entryFromProd.isContinuous())
      entry.setBit(ParamContext.chronContext);
    if(entryFromProd.isCumulative())
      entry.setBit(ParamContext.cumulContext );
  }
  //</GED>

  /** This method removes all those event entries from the event table that
   *  have all the four associated context bits reset.
   */
  void clearGarbage() {
    Enumeration en = this.elements();
    Stack stk = new Stack();
    while(en.hasMoreElements()) {
      PCEntry entry2 = (PCEntry) en.nextElement();
      if(!isGarbage(entry2))
	stk.push(entry2);
    }
    this.removeAllElements();
    while(!(stk.empty())) {
      this.add(stk.pop());
    }
  }

  private boolean isGarbage(PCEntry ety) {
    return ety.isGarbage();
  }

  private void collect_Garbage(PCEntry ety) {
    pcEntries.removeElement(ety);
  }

  //<GED>
  /**
   * Return pcEntries
   */
  public Vector getPCEntries(){
    return pcEntries;
  }
  //</GED>
}

