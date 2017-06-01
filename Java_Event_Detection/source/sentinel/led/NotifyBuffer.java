/**
 * NotifyBuffer.java --
 * Author          : Seokwon Yang, H. Kim
 * Created On      : Jan ?? 1999
 * Last Modified By: Seokwon Yang
 * Last Modified On: Thu Aug 12 02:33:20 1999
 * Copyright (C) University of Florida 1999
 */

package sentinel.led;
import java.util.LinkedList;

/** The NotifyBuffer class is used as a buffer that stores notifyObjects. The application
 *  thread and rule threads put notifyObjects in notify buffer. The led thread reads
 *  the notify objects from the notify buffer and invokes the notify() method contained
 *  in the notify object.
 *
 *  LED thread, application thread and the rule threads access the notify buffer
 *  simultaneously through its put() and get() methods. Therefore, these two methods
 *  are defined using the keyword 'synchronized'.
 *
 */


 // modified by wtanpisu

 public class NotifyBuffer {

  private LinkedList buffer;
  private boolean ledThreadDebug = Utilities.isDebugFlagTrue("ledThreadDebug");

  public  NotifyBuffer( ) {
    buffer = new LinkedList();
  }

  /** This method gets a notify object from the notify buffer.
   */

  // The LED thread calls the get() method. It waits if the notify buffer
  // is empty, else it returns
  synchronized public NotifyObject get() {
    if (ledThreadDebug)
      System.out.println("\n" + Thread.currentThread().getName() + " : calling get");

    //  make sure that there is a notify object in the queue
    while(buffer.size() == 0){
      //wait if there is no notify object in the queue
      try{
	wait();
      }
      catch(InterruptedException ie) {
        System.out.println("InterruptedException caught");
        ie.printStackTrace();
      }
    }
    NotifyObject tempNotObj;
    // get the notify object from the linked list
    tempNotObj = (NotifyObject)  buffer.removeFirst();
    return tempNotObj;
  }

  /** This method puts a notify object from the notify buffer.
   */



    synchronized public void put(NotifyObject notifyObj) {
    buffer.add(notifyObj);
    // when size equals 1, it means LED is waitinf for notify
    if (buffer.size() == 1)
      notifyAll();
 }
}

// added on 01/24/2000

