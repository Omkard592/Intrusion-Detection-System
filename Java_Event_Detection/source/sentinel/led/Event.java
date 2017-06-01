/**
 * Event.java --
 * Author          : Seokwon Yang, H. Kim
 * Created On      : Jan ?? 1999
 * Last Modified By: Seokwon Yang
 * Last Modified On: Sun Sep 19 23:25:10 1999
 * Copyright (C) University of Florida 1999
 */

package sentinel.led;
import java.util.*;
import sentinel.comm.*;

/**
 *  The abstract Event class is extended by the Primitive class and all the
 *  composite operator classes. It contains attributes and methods that are common to
 *  both primitive and composite event nodes in the event graph. It also contains
 *  some abstract methods that are to be implemented by all event nodes.
 */

//public abstract class Event extends Notifiable {
public abstract class Event implements Notifiable {

  static boolean evntReqstDebug = DebuggingHelper.isDebugFlagTrue("evntReqstDebug");
 static boolean glbEvntReqstDebug = DebuggingHelper.isDebugFlagTrue("glbEvntReqstDebug");

  // Debugging helper
  private static boolean eventDetectionDebug = Utilities.isDebugFlagTrue("eventDetectionDebug");
  private static boolean glbDetctnReqstDebug = Utilities.isDebugFlagTrue("glbDetctnReqstDebug");

  public abstract void setContextRecursive(int context);
  public abstract void resetContextRecursive(int context);
  protected  abstract Table getTable(Notifiable e);

  /** Name of the event */
  protected String eventName;

  /**
   * Counters indicating whether to detect the event in a particular context or not
   */

  protected int recentCounter;
  protected int contiCounter;
  protected int chronCounter;
  protected int cumulCounter;

  /**
   * Event category is used to classify the event, and to indicate the location
   * of the event node whether it is in the local event graph or the global
   * event graph.
   */
  protected int eventCategory;

  protected EventType eventType;

  protected Vector subscribedRules = new Vector();

  protected Vector subscribedEvents = new Vector();


  /* Default Constructor */
  public Event() {}

  /* Constructor */
  public Event(String eName) {
    eventName = eName;
    recentCounter = 0;
    contiCounter = 0;
    chronCounter = 0;
    cumulCounter = 0;
  }

  /* This method returns the event name */
  public String getEventName() {
    return eventName;
  }

  /* This method the vector of rules associated with this event */
  Vector getSubscribedRules() {
    return subscribedRules;
  }

  protected void printDetectionMask() {
//    if(eventDetectionDebug ){
      System.out.print("DetectionMask at event " + eventName + ": ");
      System.out.print(recentCounter);
      System.out.print(chronCounter);
      System.out.print(contiCounter);
      System.out.println(cumulCounter);
//    }
  }

  /** This method increments the context counter for the given context
   *  for an event node.
   */
  protected  void setContextCurrent(int context) {
    switch(context) {
      case ParamContext.recentContext:
      recentCounter++;
      break;

      case ParamContext.chronContext:
      chronCounter++;
      break;

      case ParamContext.contiContext:
      contiCounter++;
      break;

      case ParamContext.cumulContext:
      cumulCounter++;
      break;
    }
    if(evntReqstDebug || glbEvntReqstDebug)
    printDetectionMask();
  }

  /**
   * This method returns the context counter for the given context
   *  for an event node.
   */
  int getContextCurrent(int context){
    int returnContext=0;
    switch(context) {
      case ParamContext.recentContext:
      	returnContext = recentCounter;
      case ParamContext.chronContext:
      	returnContext = chronCounter;
      case ParamContext.contiContext:
      	returnContext = contiCounter;
      case ParamContext.cumulContext:
      	returnContext = cumulCounter;
    }
    return(returnContext);
  }

  /** This method decrements the context counter for the given context
   *  for an event node.
   */
  protected void resetContextCurrent(int context){
    switch(context) {
      case ParamContext.recentContext:
      	recentCounter--;
      	break;
      case ParamContext.chronContext:
      	chronCounter--;
      	break;
      case ParamContext.contiContext:
      	contiCounter--;
      	break;
      case ParamContext.cumulContext:
      	cumulCounter--;
      	break;
    }
  }

  /** This method subscribes a composite event to this event.
   */
  void subscribe(Event compositeEvent) {
    subscribedEvents.addElement(compositeEvent);
  }


  /** This method executes the rules subscribed to an event.
	 */
  void executeRules(EventSet eventSet,ParamContext context) {
    Executable rule;
    ListOfParameterLists parameterLists = new ListOfParameterLists();
    Vector paramListSet = eventSet.getParamLists();
    ParameterList paramList = null;
    for (int i=0; i<paramListSet.size(); i++) {
      paramList = (ParameterList) paramListSet.elementAt(i);
      parameterLists.add(paramList);
    }
    for (int i=0; i<subscribedRules.size(); i++) {
      rule = (Executable) subscribedRules.elementAt(i);
      // add 03/20
      if(eventDetectionDebug)
        System.out.println("Event call rule execute");
      rule.execute(parameterLists, context.getId());
    }
  }

  /** This method returns the event tables of all the subscribed events for this
   *  event.
   */
  protected Vector getParentTables() {
    Vector array = new Vector();
    Event event = null;
    if (eventDetectionDebug)
      System.out.println("\n Event.java:getParentTables():subscribedEvents.size = " + subscribedEvents.size());
    for (int i = 0; i < subscribedEvents.size(); i++) {
      event = (Event) subscribedEvents.elementAt(i);
      Table table = event.getTable(this);
      if (table != null)
        array.addElement(table);
    }
    return array;
  }

  /** This method propagates the parameter list in the given context to all
   *  the subscribed events. This method is used for propagating a primitive
   *  event occurrence to the subscribed composite events.
   */
  void propagatePC(ParameterList paramList, byte context) {
    if (eventDetectionDebug){
      System.out.println("Event::propagatePC");
      System.out.println("Context "+context);
    }
    Vector parentTables = getParentTables();
    Table table =null;
    if (eventDetectionDebug)
      System.out.println("\nEvent.java:parentTables.size = " + parentTables.size());
    for(int i = 0; i < parentTables.size(); i++) {
      table = (Table) parentTables.elementAt(i);
      if (eventDetectionDebug)
        System.out.println("Put the param and context into the parent table");
      table.getPropagation(paramList, context); // in pcTable
      if (eventDetectionDebug)
        table.print();
    }
  }

  /** This method propagates an event set in the given context to all
   *  the subscribed events. This method is used for propagating a composite
   *  event occurrence to the subscribed composite events.
   */
  void propagatePC(EventSet s,ParamContext context) {
    Vector parentTables = getParentTables();
    Table table = null;
    for (int i=0; i<parentTables.size(); i++) {
      table = (Table) parentTables.elementAt(i);
      table.getCompositePropagation(s,context.getId());
    }
  }

  //<GED>
  /**
   * The forward flag indicates whether the producer needs to inform
   * the GED about the occurence of this event
   */
  boolean forwardFlag;// Not use in global event

  /**
   * The send back flag indicates whether the server (GED) needs to inform
   * the consumber about the occurence of this event.
   */
  protected boolean sendBackFlag; // Only use in global and composite event


  /** This method sets the forward flag, if this event is a global event. */
  public void setForwardFlag(){
    forwardFlag = true;
  }

  /** This method resets the forward flag. */
  void resetForwardFlag(){
    forwardFlag = false;
  }

  /** This method returns the state of the forward flag. */
  boolean getForwardFlag(){
    return forwardFlag;
  }

  /**
   * This method is used to set the send back flag
   */
  public void setSendBackFlag(boolean flag){
//    if(glbDetctnReqstDebug)
//      System.out.println("Set the send back to be "+flag);
    sendBackFlag = flag;
  }

  /**
   * This method returns the sand back flag
   */
  public boolean getSendBackFlag(){
    return sendBackFlag;
  }
  //</GED>
}


