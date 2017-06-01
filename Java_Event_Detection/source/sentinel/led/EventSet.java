/**
 * EventSet.java --
 * Author          : Seokwon Yang, H. Kim
 * Created On      : Jan ?? 1999
 * Last Modified By: Seokwon Yang
 * Last Modified On: Sun Sep 19 23:25:10 1999
 * Copyright (C) University of Florida 1999
 */
package sentinel.led;

import java.util.*;
import java.io.*;
/** The EventSet class is used to store a composite event occurrence (a set of ParameterLists)
 *  and its associated time stamp. It contains methods to make a union of two or threee composite
 *  event occurrences.
 */
class EventSet implements Serializable{

  private static boolean eventDetectionDebug = Utilities.isDebugFlagTrue("eventDetectionDebug");

  private Vector set;
  private TimeStamp ts;

  EventSet () {
    set = new Vector();
    ts = null;
  }

  int getSize() {
    return set.size();
  }

  Enumeration elements() {
    return set.elements();
  }

  private boolean contains(Object o) {
    return set.contains(o);
  }

  void setTS(TimeStamp ts) {
    this.ts = ts;
  }

  TimeStamp getTS() {
    return ts;
  }

  Vector getParamLists() {
    return set;
  }

  boolean addElement(Object obj) {
    set.addElement(obj);
    return true;
  }

  /** This method adds an EventSet es to this event set.
   */
   boolean addElement(EventSet es) {
    if (es instanceof EventSet) {
      TimeStamp t = ((EventSet)es).getTS();
      if (set.size() == 0)
	ts = t;
      else if(ts.olderThan(t))
	ts = t;

      Enumeration en = ((EventSet)es).elements();
      while (en.hasMoreElements()) {
	ParameterList paramList = (ParameterList)en.nextElement();
	addElement(paramList);
      }
      return true;
    }
    else {
      if(eventDetectionDebug)
	System.out.println("EventSet.addElement(): Wrong type passed as arg");
      return false;
    }
  }

  /** This method checks if the current event set contains the same elements as
   *  the given inputset.
   */
  boolean equals (EventSet inputset) {
    int size = this.getSize();
    if(size == 0) return false;
    else if(size != inputset.getSize()) return false;

    //System.out.print("This EventSet: ");
    //this.print();
    //System.out.print("Input EventSet: ");
    //inputset.print();

    Enumeration e1 = set.elements();
    while(e1.hasMoreElements()) {
      Object o = e1.nextElement();

      if(!inputset.contains(o))
	return false;
    }
    //System.out.println("This set and inputset are equal");
    return true;
  }

  /** This method forms a union of two event sets s1 and s2. The timestamp of
   *  the combined set will be the higher of the timestamps of s1 and s2.
   */
  void union(EventSet s1, EventSet s2) {
    Enumeration en = s1.elements();
    while(en.hasMoreElements()) {
      Object o = en.nextElement();
      addElement(o);
    }
    en = s2.elements();
    while(en.hasMoreElements()) {
      Object o = en.nextElement();
      addElement(o);
    }
    TimeStamp t1 = s1.getTS();
    TimeStamp t2 = s2.getTS();
    if (t1 == null) {
      setTS(t2);
      return;
    }
    if (t2 == null) {
      setTS(t1);
      return;
    }
    if(t1.youngerThan(t2))
      setTS(t1);
    else
      setTS(t2);
  }

  /** This method forms a union of three event sets s1, s2 and s3. The timestamp of
   *  the combined set will be the highest among the timestamps of s1, s2 and s3.
   */
  void union(EventSet s1, EventSet s2, EventSet s3) {
    Enumeration en = s1.elements();
    while(en.hasMoreElements()) {
      Object o = en.nextElement();
      addElement(o);
    }
    en = s2.elements();
    while(en.hasMoreElements()) {
      Object o = en.nextElement();
      addElement(o);
    }
    en = s3.elements();
    while(en.hasMoreElements()) {
      Object o = en.nextElement();
      addElement(o);
    }
    TimeStamp t1 = s1.getTS();
    TimeStamp t2 = s2.getTS();
    TimeStamp t3 = s3.getTS();
    TimeStamp newTS;
    if(t1.youngerThan(t2) && t1.youngerThan(t3))
      newTS = t1;
    else if(t2.youngerThan(t1) && t2.youngerThan(t3))
      newTS = t2;
    else
      newTS = t3;
    setTS(newTS);
  }

  void print() {
    ParameterList paramList;
    String methodSig;
    String eventName;
    TimeStamp tsObj;
    for (int i=0; i<set.size(); i++) {
      paramList = (ParameterList) set.elementAt(i);
      methodSig = paramList.getMethodSignature();
      tsObj = paramList.getTS();
      if(eventDetectionDebug){

        System.out.print("\n" + methodSig);
        //          System.out.print("get Sequence " + tsObj.getSequence() + " ");
        System.out.print("get timestamp " + tsObj.getGlobalTick() + " ");
      }
    }
    //System.out.print(" TS = " + ts.getSequence() + " ");
  }

 /* public static void main(String argv[]) {
    Integer a = new Integer(1);
    Integer b = new Integer(2);
    Integer c = new Integer(3);

    EventSet s1 = new EventSet();
    s1.addElement(b);
    s1.addElement(c);
    s1.addElement(a);

    EventSet s2 = new EventSet();
    s2.addElement(a);
    s2.addElement(c);
    s2.addElement(b);

    EventSet s3 = new EventSet();
    s3.addElement(a);
    s3.addElement(b);

    if(s1.equals(s2))
      System.out.println("YOU ARE RIGHT");
    if(!s1.equals(s3))
      System.out.println("YOU ARE RIGHT");
  }*/
}

