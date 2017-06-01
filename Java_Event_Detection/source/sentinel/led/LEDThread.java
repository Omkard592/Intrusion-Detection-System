/**
 * LEDThread.java --
 * Author          : Seokwon Yang, H. Kim
 * Created On      : Jan ?? 1999
 * Last Modified By: Seokwon Yang
 * Last Modified On: Thu Aug 12 02:33:20 1999
 * Copyright (C) University of Florida 1999
 */

package sentinel.led;
import java.util.Vector;
import java.util.Enumeration;

/** The LEDThread class denotes the event detector thread that processes event
 *  notifications. It gets notify objects from the notify buffer and invokes the
 *  methods contained in the notify objects.
 */

class LEDThread extends Thread {

  private boolean evntNotifDebug = Utilities.isDebugFlagTrue("evntNotifDebug");

  private NotifyBuffer notifyBuffer;
  private boolean ledThreadDebug = Utilities.isDebugFlagTrue("ledThreadDebug");

  //  changed by wtanpisu
  private RuleScheduler ruleScheduler;

  /** LED communication interface */
  // <GED>
  private LEDInterface ledInterf;
  // <\GED>


  public LEDThread(NotifyBuffer nb, RuleScheduler ruleScheduler) {
    notifyBuffer = nb;
    this.ruleScheduler = ruleScheduler;
    setDaemon(true);
  }
   // added by wtanpisu on 24 Jan 2000

  void process(NotifyObject notifyObj) {
    notifyObj.invokeMethod();
  }

  /** The run method of the event detector thread processes event
   *  notifications. It gets notify objects from the notify buffer and invokes the
   *  methods contained in the notify objects. It calls the wakeup() method on
   *  the notify buffer if the event being notified is a SYNCHRONOUS event. This is
   *  because if the event is synchronous, the application thread would be waiting
   *  by calling the wait() method on the notify buffer object.
   *  The wakeup() method calls the notify() method on the notify buffer object.
   */

  //changed by wtanpisu

  public void run() {
    NotifyObject notifyObj = null;
    while (true) {
      if (ledThreadDebug || evntNotifDebug)
	System.out.println("\n" + Thread.currentThread().getName() + " calling get NotifObject");

      // get the notify object from the notify buffer
      notifyObj = notifyBuffer.get();

      process(notifyObj);
    	ruleScheduler.wakeup();

       // notify the notify object that all its associated rule(s) is already set up in rule queue
      synchronized (notifyObj){
    		notifyObj.notify();
  	  }
    }
  }

  /** The method set the led interface instance. When the event raises, and
   *  the forward flag is set, we can propagate parameterlist and context to
   *  server through LEDInterface
   */

  void setLEDInterface(LEDInterface ledInterf){
    this.ledInterf = ledInterf;
  }

  /** The method returns the led interface instance. */
  LEDInterface getLEDInterface(){
    return ledInterf;
  }
}


