/**
 * TimeStamp.java --
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

//<added> by weera
import java.util.Date;
import java.sql.Timestamp;
//</added>


/** An event (primitive or composite) is associated with a TimeStamp that
 *  denotes the time of occurrence of the event. The TimeStamp currently
 *  stores a sequence number and the system time in milliseconds when the
 *  event occurred.
 *
 *  The sequence number is a long integer. A global sequence counter is used to
 *  assign the sequence number of a TimeStamp. This counter is incremented for
 *  every primitive event occurrence.
 */

public class TimeStamp implements Serializable {
  private int PRECESION = 1 ;//precesion using in TRUNC function to compute the global function.

  //<added> by weera
  private Date date;
  private long localTick;
  private long globalTick;
  private String siteName;
  //</added>

  private static long sequenceCounter = 1;
  private long sequenceNo;


  /** This constructor is used to construct a TimeStamp object when a
   *  primitive event is raised.
   */

  public TimeStamp() {
    date = new Date();
    localTick = date.getTime();
    globalTick = localTick/PRECESION;
    sequenceNo = sequenceCounter;
    siteName = Utilities.getHostName();
	sequenceCounter++;
  }

  /** This constructor is used to construct a TimeStamp object from a
   *  given sequence number.
   */

   /*  public TimeStamp(long seqNo) {
    sequenceNo = seqNo;
    localTick = System.currentTimeMillis();
  }*/

  public TimeStamp(long glbTime) {

    sequenceNo = getSequenceCounter();
    localTick = System.currentTimeMillis();
    globalTick = localTick/PRECESION;
  }

  public  long getGlobalTick() {
    return globalTick;
  }



  public  long getLocalTick() {
    return localTick;
  }

  public long getSequence() {
    return sequenceNo;
  }

  static long getSequenceCounter() {
    return sequenceCounter;
  }

  public static long getTime() {
    return (new Date()).getTime();
    //date.getTime();
  }
  public String getSiteName(){
    return siteName;
  }

  /** This method compares the current TimeStamp with another TimeStamp ts
   *  to see which is older. An older TimeStamp has a lesser value for
   *  the sequence number than a younger TimeStamp.
   */
  boolean olderThan(TimeStamp ts) {
    if(this.siteName.equals(ts.getSiteName())) {
      //	return sequenceNo < ts.getSequence();
      return  this.localTick < ts.getLocalTick();
    }else{
      return  this.globalTick < ts.getGlobalTick();
    }
  }

  /** This method compares the current TimeStamp with another TimeStamp ts
   *  to see which is younger.
   */

  boolean youngerThan(TimeStamp ts) {
    if(this.siteName.equals(ts.getSiteName())) {
      //	return sequenceNo > ts.getSequence();
      return  this.localTick > ts.getLocalTick();
    }else{
      return  this.globalTick > ts.getGlobalTick();
    }
  }

  public static void main(String argv[]) {
    TimeStamp t1 = new TimeStamp();
	TimeStamp t2 = new TimeStamp();

	//System.out.println("ts1 = " + t1.getLocalTick());
	//System.out.println("ts2 = " + t2.getLocalTick());
	if(t1.olderThan(t2))
	  System.out.println("YOU ARE RIGHT");
	if(t2.youngerThan(t1))
	  System.out.println("YOU ARE RIGHT");
  }
}
