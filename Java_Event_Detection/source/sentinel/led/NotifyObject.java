/**
 * NotifyObject.java --
 * Author          : Seokwon Yang, H. Kim
 * Created On      : Jan ?? 1999
 * Last Modified By: Seokwon Yang
 * Last Modified On: Thu Aug 12 02:33:20 1999
 * Copyright (C) University of Florida 1999
 */

package sentinel.led;
import java.lang.reflect.Method;

/** The NotifyObject class denotes an object that stores a method reference
 *  to the notify() method of a primitive event node. It also stores the parameters
 *  to be passed to the notify method. Instances of NotifyObject are placed in the
 *  notify buffer by the application thread.
 *  When a primitive event is raised by the application, it is coverted into a notify
 *  object and placed in the notify buffer by the application thread. The LED thread
 *  reads these notify objects from the notify buffer and invokes the methods
 *  contained in these notify objects.
 *  A notify object also stores a reference to the parent thread that has invoked the
 *  event method corresponding to the primitive event associated with this notify
 *  object.
 */

public class NotifyObject {
  Method notifyMethod;
  Primitive primitiveEvent;
  Object[] methodParams;
  Thread parent;
  boolean waitFlag;
  private static boolean eventDetectionDebug = Utilities.isDebugFlagTrue("eventDetectionDebug");
    private static boolean evntNotifDebug = Utilities.isDebugFlagTrue("evntNotifDebug");

  ParameterList paramList;
  Thread parentT;
  RuleScheduler ruleScheduler;


  public  NotifyObject(Primitive primEvent, ParameterList paramList,
	      			 Thread parent, RuleScheduler ruleScheduler) {
    this.notifyMethod = getNotifyMethod();
    this.primitiveEvent = primEvent;
    methodParams = new Object[3];
    this.methodParams[0] = paramList;
    this.methodParams[1] = parent;
    this.methodParams[2] = ruleScheduler;
    this.paramList = paramList;
    this.parent = parent;
    this.ruleScheduler = ruleScheduler;


    int detectMode = primEvent.getDetectionMode();
    if (detectMode == DetectionMode.synchronousMode)
      waitFlag = true;
    else
      waitFlag = false;
  }

  //<GED>

  RemoteEvent remEvent;
  PCTable remPcTable;
  boolean notifyRemoteNd = false;

  public  NotifyObject(RemoteEvent remEvent, PCTable remPcTable,
	      			 Thread parent, RuleScheduler ruleScheduler) {

    this.remEvent = remEvent;
    this.remPcTable = remPcTable;
    this.parent = parent;
    this.ruleScheduler = ruleScheduler;
    notifyRemoteNd = true;
  }

  //</GED>


   /** This method gets a reference to the notify() method of a primitive event node
    *  using Java reflections.
    */
  Method getNotifyMethod() {
    Class[] formalParams = new Class[3];
    try {
      formalParams[0] = Class.forName("sentinel.led.ParameterList");
      formalParams[1] = Class.forName("java.lang.Thread");
      formalParams[2] = Class.forName("sentinel.led.RuleScheduler");
    }
    catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
    Method method = Utilities.getMethodObject("sentinel.led.Primitive",	"notify",formalParams);
    return method;
  }

  void print() {
    if(eventDetectionDebug){
	    System.out.println("\nPrinting notifyObject ->>");
      System.out.print("eventName: " + primitiveEvent.eventName);
	    System.out.print("method: " + primitiveEvent.getMethodSignature());
      System.out.print(" waitFlag: " + waitFlag);
    }
  }

  /** This method invokes the method contained in this notify object.
    */
  void invokeMethod() {

    try {
      if(! notifyRemoteNd){
        if(eventDetectionDebug || evntNotifDebug)
  	  System.out.println("Notifying event prim node ::" + primitiveEvent.eventName);
        notifyMethod.invoke(primitiveEvent,methodParams);
        // primitiveEvent.notify (paramList, parentT,ruleScheduler);
      }
      //<GED>
      else{
        if(evntNotifDebug)
  	  System.out.println("Notifying event remote node ::" + remEvent.getEventName());

        remEvent.notify (remPcTable, parent,ruleScheduler);
      }
      //<\GED>
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
}
