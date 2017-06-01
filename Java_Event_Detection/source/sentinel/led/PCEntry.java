/**
 * PCEntry.java --
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

/** The PCEntry class denotes an event entry in the event table. It contains an
 *  event set and a byte. The lower most four bits of the byte are used to
 *  denote the four parameter contexts. This byte is manipulated to detect an event
 *  in different parameter contexts.
 */
public class PCEntry implements Serializable{
  private byte bits;
  private EventSet set;

  PCEntry() {
    set = new EventSet();
  }

  PCEntry(EventSet s) {
    set = s;
  }

  public void print() {
    set.print();
    System.out.println (":::");
    ContextBit.print(bits);
  }

  EventSet getEventSet() {
    return set;
  }

  TimeStamp getTS() {
    return set.getTS();
  }

  boolean youngerThan(TimeStamp ts) {
    return this.getTS().youngerThan(ts);
  }

  boolean youngerThan(PCEntry entry) {
    return this.getTS().youngerThan(entry.getTS());
  }

  boolean olderThan(PCEntry entry) {
    return this.getTS().olderThan(entry.getTS());
  }

  boolean olderThan(TimeStamp ts) {
    return this.getTS().olderThan(ts);
  }

  boolean addElement(ParameterList paramList) {
    return set.addElement(paramList);
  }

  void clearRecent() {
    bits = ContextBit.clearRecent(bits);
  }

  boolean isRecent() {
    return ContextBit.isRecent(bits);
  }

  void clearChronicle() {
    bits = ContextBit.clearChronicle(bits);
  }

  boolean isChronicle() {
    return ContextBit.isChronicle(bits);
  }

  void clearContinuous() {
    bits = ContextBit.clearContinuous(bits);
  }

  boolean isContinuous() {
    return ContextBit.isContinuous(bits);
  }

  void clearCumulative() {
    bits = ContextBit.clearCumulative(bits);
  }

  boolean isCumulative() {
    return ContextBit.isCumulative(bits);
  }

  void setAllContext() {
    bits = ContextBit.initialize();
  }

  void setContext(byte bits) {
    this.bits = bits;
  }

  void initializeContext() {
    bits = (byte)0;
  }

  void setBit(int context) {
    if(context == ParamContext.RECENT.getId())
      bits = ContextBit.setRecent(bits);
    else if(context == ParamContext.CHRONICLE.getId())
      bits = ContextBit.setChronicle(bits);
    else if(context == ParamContext.CONTINUOUS.getId())
      bits = ContextBit.setContinuous(bits);
    else if(context == ParamContext.CUMULATIVE.getId())
      bits = ContextBit.setCumulative(bits);
  }

  void updateContextBits(byte b) {
    bits = b;
  }

  boolean contains(EventSet s) {
    return set.equals(s);
  }

  boolean isGarbage() {
    return ContextBit.isAllZero(bits);
  }
}



