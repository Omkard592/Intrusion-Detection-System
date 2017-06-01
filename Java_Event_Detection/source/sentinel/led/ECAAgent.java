/**
 * ECAAgent.java --
 * Author          : Seokwon Yang, H. Kim
 * Created On      : Jan ?? 1999
 * Last Modified By: Seokwon Yang
 * Last Modified On: Sun Sep 19 23:25:10 1999
 * Copyright (C) University of Florida 1999
 */

package sentinel.led;

import java.util.*;
import java.lang.reflect.Method;

/**
 *  This class contains the API to be used for Sentinel applications. With this
 *  API, the user can create class level and instance level Primitive events,
 *  Composite events and define rules on those events in different
 *  parameter contexts (RECENT, CHRONICLE, CONTINUOUS, CUMULATIVE). The user
 *  also raises the events through the 'raiseEvent' API.
 *
 *  This class is a public class since it is accessed by all user applications.
 *
 */

public class ECAAgent {


  static boolean evntReqstDebug = DebuggingHelper.isDebugFlagTrue("evntReqstDebug");
  static boolean evntCreatnDebug = DebuggingHelper.isDebugFlagTrue("evntCreatnDebug");
  static boolean evntNotifDebug = DebuggingHelper.isDebugFlagTrue("evntNotifDebug");
  static boolean glbEvntReqstDebug = DebuggingHelper.isDebugFlagTrue("glbEvntReqstDebug");



  // Debugging helper
  static boolean eventDetectionDebug = DebuggingHelper.isDebugFlagTrue("eventDetectionDebug");
  static boolean ruleSchedulerDebug = DebuggingHelper.isDebugFlagTrue("ruleSchedulerDebug");
  static boolean mainDebug = DebuggingHelper.isDebugFlagTrue("mainDebug");
  static boolean Debug = DebuggingHelper.isDebugFlagTrue("Debug");
  static boolean glbEvntCreatnDebug = DebuggingHelper.isDebugFlagTrue("glbEvntCreatnDebug");
  static boolean glbNotifDebug = DebuggingHelper.isDebugFlagTrue("glbNotifDebug");


  /** This Hashtable stores the mapping from the event names to the event
   * nodes. Used for getting the event node when the user wants to
   * disable/enable or delete a rule on a given event name.
   */
  Hashtable eventNamesEventNodes;

  /**  This Hashtable stores the mapping from the event signatures to the event
   *   nodes. Used for getting the event nodes at the time of raising events.
   */
  Hashtable eventSignaturesEventNodes;

  /** This Hashtable stores the mapping from rule names to the rule nodes.
   *  Used for searching a rule node from the rule name.
   */
  Hashtable  ruleNamesRuleNodes;

  /** This NotifyBuffer queues the occurenence of notifiable object
   */
  NotifyBuffer notifyBuffer;


  Hashtable eventNamesEventHandles;

  /** This scheduler is responsible for scheduling the rules in the process rule list.
   */
  RuleScheduler ruleScheduler;

  /** This ledThread takes care event detection process.
   */
  LEDThread ledThread;

  /** Predefined primitive event is used in process-deferred-rule method
   *  The event will be raised automatically when the processDefRule() is called.
   */
  Primitive commitEvent;


  //<GED>
  /**
   * The hashmap maps a producer event name with event node whose event is
   * consumed by remote clients.
   */

  // Hash Map can contain key and null
  // When the event is requested by client, but event hasn't been created yet.
  // The eventName and null will be put into the hash map.

  static protected HashMap prodEventName_ReqstContextsHt = new HashMap();


  /**
   * This method is invoked when the event is created to check whether the given
   * event node is consumed by any client or not.
   */

  private  static boolean isConsumed(Event eventNode){

    String eventNm = eventNode.getEventName();
    if(prodEventName_ReqstContextsHt.containsKey(eventNm) ){

      // When the consumer requested the event detection before the creation of
      // this event. The prodEventName_ReqstContextsHt.get(eventNm) returns
      // the requested context in byte format

      //The prodEventName_ReqstContextsHt.get(eventNm) is null
      //if(prodEventName_ReqstContextsHt.get(eventNm) == null)
      if(prodEventName_ReqstContextsHt.get(eventNm) instanceof Byte)
        return true;
      else
       return false;   // if the gedfwdFlag is already set
    }
    else{

      // No producer event name exists in this hashmap. This event may be a
      // local event or the event hasn't been created yet.

      if(glbEvntReqstDebug){
        System.out.println("This event name is not global event, or");
        System.out.println("This event name "+eventNm+ " doesn't exist.");
      }
      return false;
    }
  }
  //</GED>


  static Hashtable ecaAgentNamesEcaAgentInstances = new Hashtable();
  static Hashtable eventHandlesEcaAgentInstances = new Hashtable();
  private static ECAAgent defaultECAAgent = null;

  protected String agentName;
  static Thread applThread = null;


  // added by seyang on 8/6/99, adapting singleton & factory pattern

  /** The constructor for an ECAAgent.
   */
  protected	ECAAgent(String agentName) {

    //<GED>
    if(Constant.SCOPE.equals ("GLOBAL")){
      remNdMngr = new RemoteNodeManager();
    }
    //</GED>

    this.agentName = agentName;
    notifyBuffer = new NotifyBuffer();
    applThread = Thread.currentThread();
    applThread.setName("applicationThread");

    // modified by wtanpisu
    ruleScheduler = new RuleScheduler(applThread);

    Timer.timer.setNotifyBuffer(notifyBuffer);
    // added on 03/31/2000

    ruleScheduler.setName("Scheduler Thread");
    ruleScheduler.start();

    // modified by wtanpisu
    ledThread = new LEDThread(notifyBuffer,ruleScheduler);
    // modified on 01/24/2000

    ledThread.setName("LEDThread");
    ledThread.setDaemon(true);
    ledThread.start();

    eventNamesEventHandles = new Hashtable();
    eventNamesEventNodes = new Hashtable();
    eventSignaturesEventNodes = new Hashtable();
    ruleNamesRuleNodes = new Hashtable();


    // added by wtanpisu
    // rational : define the predifined commit event and commit rule is used
    //            to process the deferred rules

    EventHandle commitTransaction = createPrimitiveEvent("commitTransaction","ECAAgent",EventModifier.BEGIN,"void processDefRules()",DetectionMode.SYNCHRONOUS);

    try{
      createRule("commitRule",commitTransaction,"sentinel.led.Utilities.True", "sentinel.led.ECAAgent.executeProcessDeffRules",-1);
      commitEvent = (Primitive) commitTransaction.getEventNode();
    }catch(Exception e){
      System.out.println(" ALERT !! Can't create the predifined rule for process-def-rule");
    }
    // added on 03/31/2000
  }

  /** This constructor is to be used in later versions.
   */
  /*public ECAAgent(int port) {
    this.port = port;
  }*/


  //<GED>
  /**
   * Application id is concatenated among application name and "_" and
   * Virtual Machine Name
   */

  private static String APPID;

  /**
   * The LED communicaiton interface is responsible to transmit the requests
   * to the global event detector and receive the response from the global event
   * detector.
   */
  private static LEDInterface ledIntf;

  /**
   * A remote event factory is responsible for creating the remote object on
   * the local site. The remote object represents the global event.
   */
  private static RemoteEventFactory remEvntFactry;

  /**
   * A remote node manager is used to manage the remote nodes
   */
  private static RemoteNodeManager remNdMngr;

  /**
   * This method returns the applicaiton id.
   */
  static String getAppID(){
    return APPID;
  }

  /**
   * Return the remote node manager associated with this ECAAgent
   */
  static RemoteNodeManager getRemNodeManager(){
    return remNdMngr;
  }

  /**
   * This method is to set a forward flag of the primitive event node to be true.
   */
  static synchronized void makeGlobal(String eventName, int context){
    Enumeration en = ecaAgentNamesEcaAgentInstances.elements();
    ECAAgent ecaAgent;
    EventHandle eventHandle;

    boolean foundFlag = false;

    /*  Find the location of the event node */
    if(glbEvntReqstDebug){
      System.out.println("Searching for the event handle of this event : "+ eventName);
      System.out.println("Request to detect in"+ ParamContext.getContext(context) +"context");
    }

    while(en.hasMoreElements()){

      ecaAgent = (ECAAgent) en.nextElement();
      Hashtable eventNamesEventHandles = ecaAgent.getEventNamesEventHandles();
      eventHandle = (EventHandle) eventNamesEventHandles.get(eventName);

      // Found the event handle
      if (eventHandle != null){

        Event evntOfIntrst  =  eventHandle.getEventNode();

        // Check whether the gedFwdFlag is not set or not. If it is not set,
        // the glbEvntNm_ProducerEventNd doesn't contain this eventName.


        if(!evntOfIntrst.getForwardFlag()){
          if(glbEvntReqstDebug)
            System.out.println("Set the global forward flag of this event ->"+ eventName);

          prodEventName_ReqstContextsHt.put (eventName, new Byte((byte)0));
          evntOfIntrst.setForwardFlag();

          // Increase the context counter to keep tracking how many elements are
          // interested in this event (direct/indirect).
          evntOfIntrst.setContextRecursive(context);

        }
        else{

          // Consumer might request for the different context.
          evntOfIntrst.setContextRecursive(context);

        }
        foundFlag = true;
        break;
      }
    }
    // Event hasn't been created yet
    if(foundFlag == false){ // no event

      // Update the detection context.
      if( prodEventName_ReqstContextsHt.get(eventName) == null  ){
        if(context == ParamContext.recentContext){
          prodEventName_ReqstContextsHt.put (eventName,new Byte(ContextBit.setRecent((byte) 0)));
          if(glbEvntReqstDebug)
          System.out.println("Request to detect in recent context");
        }
        else if(context == ParamContext.contiContext){
          prodEventName_ReqstContextsHt.put (eventName,new Byte(ContextBit.setContinuous((byte)0)));
          if(glbEvntReqstDebug)
            System.out.println("Request to detect in continuous context");
        }
        else if(context == ParamContext.chronContext){
          prodEventName_ReqstContextsHt.put (eventName,new Byte(ContextBit.setChronicle((byte)0)));
          if(glbEvntReqstDebug)
            System.out.println("Request to detect in chronical context");
        }
        else if(context == ParamContext.cumulContext){
          prodEventName_ReqstContextsHt.put (eventName,new Byte(ContextBit.setCumulative((byte)0)));
          if(glbEvntReqstDebug)
            System.out.println("Request to detect in cumulative context");
        }
        else{
          System.err.println("ERROR :: makeGlobal");
        }
      }else{

        byte currContext = ((Byte) prodEventName_ReqstContextsHt.get(eventName)).byteValue();

        // if the requested context is recent, and the recent context hasn't been set. Set recent context
        if(context == ParamContext.recentContext && (!ContextBit.isRecent(currContext)))
          prodEventName_ReqstContextsHt.put (eventName,new Byte(ContextBit.setRecent(currContext)));
        else if(context == ParamContext.contiContext && (!ContextBit.isContinuous(currContext)))
          prodEventName_ReqstContextsHt.put (eventName,new Byte(ContextBit.setContinuous(currContext)));
        else if(context == ParamContext.chronContext&& (!ContextBit.isChronicle(currContext)))
          prodEventName_ReqstContextsHt.put (eventName,new Byte(ContextBit.setChronicle(currContext)));
        else if(context == ParamContext.cumulContext && (!ContextBit.isCumulative(currContext)))
          prodEventName_ReqstContextsHt.put (eventName,new Byte(ContextBit.setCumulative(currContext)));
        else{
          System.err.println("ERROR :: makeGlobal *");

        }

      }

      //prodEventName_ReqstContextsHt.put (eventName,null);

      if(glbEvntReqstDebug)
        System.out.println("This event "+ eventName+" hasn't registered to LED.");
    }
  }
  //</GED>

  /** This method initializes some of the data structures used by the Local
   * Event Detector (LED).
   * It is essential that this method is called in the user application before
   * creating any events and rules. LED is not started if this method is not
   * called. Since it is a static method, it can be directly called on the
   * ECAAgent class.
   */

  // modified by wt  : avoid to create the duplicate ECAAgent
  public static ECAAgent initializeECAAgent() {

    if(defaultECAAgent == null){
      Utilities.readAppConfig();

      if(mainDebug)
		System.out.println("Create new agent names :: defaultECAAgent");

      String ecaAgtNm =  "defaultECAAgent";
      defaultECAAgent = new ECAAgent(ecaAgtNm);
      ecaAgentNamesEcaAgentInstances.put(ecaAgtNm,defaultECAAgent);

      //<GED>
      if(Constant.SCOPE.equals ("GLOBAL")){

        // APPID is concatenated among APPNAME and "_" and VMC
        // eg. APPID = consumer_newdelhi
        // VMC (Virtual Machine) is an incoming queue name(JMQ) for this
        // application.

        APPID = Constant.APP_ID;

        if(mainDebug)
          System.out.println("Application ID :: "+APPID);

        // Instatiate remote nodes manager

        remNdMngr.putAgentName_remManager (ecaAgtNm,remNdMngr);

        // ECAAagents share the ELED Interface and RemoteEventFatory.
        // They will be instatiated only one time.

        if(ledIntf == null){
          ledIntf = new LEDInterface();
          remEvntFactry = new RemoteEventFactory();
          defaultECAAgent.ledThread.setLEDInterface(ledIntf);
        }
      }
      //</GED>
      return defaultECAAgent;
    }

    if(mainDebug)
      System.out.println("This defaultECAAgent is already initialized.");
    return defaultECAAgent;
  }
  // modified on 04/20/2000


  /** This method initializes and returns a named ECAAgent.
   */
  public static ECAAgent initializeECAAgent(String agentName) {

    Utilities.readAppConfig();

    // added by wt  : avoid to create the duplicate ECAAgent
    if(ecaAgentNamesEcaAgentInstances.containsKey(agentName)){
      if(mainDebug)
	System.out.println("This "+agentName+" is already initialezed.");
      return (ECAAgent) ecaAgentNamesEcaAgentInstances.get(agentName);
    }
    // added on 04/20/2000

    if(mainDebug)
      System.out.println("Create new agent names : "+agentName);
    ECAAgent ecaAgent = new ECAAgent(agentName);
    ecaAgentNamesEcaAgentInstances.put(agentName,ecaAgent);
    return ecaAgent;
  }


  // added by seyang
  /**
   * Return the default ECAAgent
   *
   * @return a value of type 'ECAAgent'
   */
  public static ECAAgent getDefaultECAAgent() {
    if(defaultECAAgent == null) {
	    defaultECAAgent = new ECAAgent("defaultECAAgent");
      ecaAgentNamesEcaAgentInstances.put("defaultECAAgent",defaultECAAgent);
    }
    return defaultECAAgent;
  }

  /** This method is used to obtain a named ECAAgent instance from the
   *  application.
   */
  public static ECAAgent getAgentInstance(String agentName) {
    return((ECAAgent) ecaAgentNamesEcaAgentInstances.get(agentName));
  }

  /** This method is used to obtain the event handles for the event
   *  specified by eventName. It returns an array of event handles
   *  since the same event could be created in multiple ECAAgent instances.
   */
  public static EventHandle[] getEventHandles(String eventName) {
    if (eventDetectionDebug){
      System.out.println("Number of ECAAgents ::"+  ecaAgentNamesEcaAgentInstances.size()) ;
    }
    Enumeration en = ecaAgentNamesEcaAgentInstances.elements();
    ECAAgent ecaAgent;
    Hashtable eventNamesEventHandles;
    Vector eventHandles = new Vector();
    EventHandle eventHandle;
    String agentName;
    while(en.hasMoreElements()){
      ecaAgent = (ECAAgent) en.nextElement();
      eventNamesEventHandles = ecaAgent.getEventNamesEventHandles();
      eventHandle = (EventHandle) eventNamesEventHandles.get(eventName);
      if (eventHandle != null)
        eventHandles.addElement(eventHandle);

    }

    int size = eventHandles.size();
    EventHandle[] eventHandleArray = new EventHandle[size];

    // added by weera
    if(size ==0){
      System.err.println("ERROR:: No EventHandle of this event ::"+ eventName);
      System.err.println("The event name doesn't exit in this system");
      System.exit(0);
    }
    // end added by weera

    if (eventDetectionDebug){
      System.out.println("ECAAgent::: eventHandles.size() = "+ eventHandles.size());
    }


    eventHandles.copyInto(eventHandleArray);
    return eventHandleArray;
  }

  /**
   * Returns the evennt name event handle hashtable
   */
  protected Hashtable getEventNamesEventHandles() {
    return eventNamesEventHandles;
  }

  /**
   * Return the event signatures event node hashtable
   */
  protected Hashtable getEventSignaturesEventNodes() {
    if(mainDebug)
      System.out.println("return eventSignaturesEventNodes hashtable ");
    return eventSignaturesEventNodes;
  }

  /**
   * Return rule scheduler
   */
  protected RuleScheduler getRuleScheduler() {
    return ruleScheduler;
  }

  /**
   * Return led communication interface
   */
  static LEDInterface getLEDInterface() {
    return ledIntf;
  }


  /** This method creates a Primitive event with the given event name.
   *
   * @param eventName        The name of the Primitive event.
   * @param className        The name of the Java class raising the event.
   * @param eventModifier    The point of event occurence (begin or end).
   * @param methodSignature  The method signature for this event.
   * <br>The event modifier specifies whether the event occurs in the beginning of
   * the method or the end of the method.
   */
  public EventHandle createPrimitiveEvent(String eventName,
                                          String className,
                                          EventModifier eventModifier,
                                          String methodSignature){

    Primitive primEvent = new Primitive(eventName,className,eventModifier,methodSignature);
    if ((eventNamesEventNodes.put(eventName, primEvent)) != null){
      System.err.println("ERROR: Event with name " + eventName +"has already been registered");
      System.exit(0);
      return null;
    }

    String eventSignature = Utilities.remWhiteSpaces(className+eventModifier.getId()+methodSignature);
    Vector eventNodeVector = null;
    eventNodeVector	= (Vector) eventSignaturesEventNodes.get(eventSignature);
    if (eventNodeVector	== null) {
      eventNodeVector	= new Vector();
      eventSignaturesEventNodes.put(eventSignature,eventNodeVector);
    }
    eventNodeVector.addElement(primEvent);
    PrimitiveEventHandle eh = new PrimitiveEventHandle(primEvent,eventName,className,methodSignature);

    eventNamesEventHandles.put(eventName,eh);
    eventHandlesEcaAgentInstances.put(eh,this);

    //<GED>
    // Check if there is a consumer subscribed to this event before this event registers to LED //
    if(glbNotifDebug){
      System.out.println("Check whether there is a consumer subscribed to this event");
      System.out.println("before this event registers to LED");
    }

    // Is this event subscribed by remote client ?//

   /* if(isConsumed(primEvent)){
      // If yes, set the forward flag
      prodEventName_ReqstContextsHt.put (eventName,primEvent);
      if(evntCreatnDebug)
        System.out.println("Set the global flag at " + eventName);
      primEvent.setForwardFlag();
    }*/
    setFwdFlagAndContextIfAny(primEvent);
    //</GED>
    return eh;
  }
  private synchronized void setFwdFlagAndContextIfAny(Event event){

    if(isConsumed(event)){
      // If yes, set the forward flag

      byte context =  ((Byte) prodEventName_ReqstContextsHt.get(event.getEventName())).byteValue();
      if(ContextBit.isRecent(context))
        event.setContextRecursive(ParamContext.recentContext);
      if(ContextBit.isChronicle(context))
        event.setContextRecursive(ParamContext.chronContext);
      if(ContextBit.isContinuous(context))
        event.setContextRecursive(ParamContext.contiContext);
      if(ContextBit.isCumulative(context))
        event.setContextRecursive(ParamContext.cumulContext);

      prodEventName_ReqstContextsHt.put (event.getEventName(),new Byte((byte)0));
      if(evntCreatnDebug)
        System.out.println("Set the global flag at " + event.getEventName());
      event.setForwardFlag();

    }
  }

  /**
   * This method creates a Primitive event with the given event name.
   * @param eventName         The name of the Primitive event.
   * @param className         The name of the Java class raising the event.
   * @param eventModifier     The point of event occurence (begin or end).
   * @param methodSignature   The method signature for this event.
   * @param detectMode	      This argument specifies whether the primitive event
   *			      should be detected in SYNCHRONOUS or PARALLEL mode.
   */

  public EventHandle createPrimitiveEvent(String eventName,
                                          String className,
                                          EventModifier eventModifier,
                                          String methodSignature,
                                          DetectionMode detectMode ) {

    Primitive primEvent = new Primitive(eventName, className,eventModifier,
   		methodSignature, detectMode);
    if ((eventNamesEventNodes.put(eventName, primEvent)) != null) {
	System.out.println("ERROR: Event with name " + eventName +
        "has already been registered");
      return null;
    }

    String eventSignature = Utilities.remWhiteSpaces(className+eventModifier.getId()+methodSignature);
    Vector eventNodeVector = null;
    eventNodeVector	= (Vector) eventSignaturesEventNodes.get(eventSignature);
    if (eventNodeVector	== null) {
      eventNodeVector	= new Vector();
      eventSignaturesEventNodes.put(eventSignature,eventNodeVector);
    }
    eventNodeVector.addElement(primEvent);
    //<GED>
    setFwdFlagAndContextIfAny(primEvent);
    //<GED>

    PrimitiveEventHandle eh = new PrimitiveEventHandle(primEvent,eventName,className,methodSignature);
    eventNamesEventHandles.put(eventName,eh);
    eventHandlesEcaAgentInstances.put(eh,this);
    return eh;
  }

  /** This method creates a Primitive event with the given event name.
   * @param eventName	      The name of the Primitive event.
   * @param className	      The name of the Java class raising the event.
   * @param eventModifier     The point of event occurence (begin or end).
   * @param methodSignature   The method signature for this event.
   * @param instance	      The instance over which this event is defined.
   */

  public EventHandle createPrimitiveEvent(String eventName,
                                          String className,
                                          EventModifier eventModifier,
                                          String methodSignature,
                                          Object instance) {

    Primitive primEvent = new Primitive(eventName, className,eventModifier,
      	methodSignature,instance, DetectionMode.SYNCHRONOUS);
    if ((eventNamesEventNodes.put(eventName, primEvent)) != null) {
      System.out.println("ERROR: Event with name " + eventName + "has already been registered");
      return null;
    }
    String eventSignature = Utilities.remWhiteSpaces(className+eventModifier.getId()+methodSignature);
    Vector eventNodeVector = null;
    eventNodeVector	= (Vector) eventSignaturesEventNodes.get(eventSignature);

    if (eventNodeVector	== null) {
      eventNodeVector	= new Vector();
      eventSignaturesEventNodes.put(eventSignature,eventNodeVector);
    }
    eventNodeVector.addElement(primEvent);
    PrimitiveEventHandle eh = new PrimitiveEventHandle(primEvent,
          eventName,className,methodSignature);
    eventNamesEventHandles.put(eventName,eh);
    eventHandlesEcaAgentInstances.put(eh,this);
        //<GED>
    setFwdFlagAndContextIfAny(primEvent);
    //<GED>

    return eh;
  }

  /**
   * This method creates a Primitive event with the given event name.
   * @param eventName         The name of the Primitive event.
   * @param className	      The name of the Java class raising the event.
   * @param eventModifier     The point of event occurence (begin or end).
   * @param methodSignature   The method signature for this event.
   * @param instance	      The instance over which this event is defined.
   * @param detectMode	      This argument specifies whether the primitive event
   *			      should be detected in SYNCHRONOUS or PARALLEL mode.
   */

  public EventHandle createPrimitiveEvent(String eventName,
                                          String className,
                                          EventModifier eventModifier,
                                          String methodSignature,
                                          Object instance,
                                          DetectionMode detectMode) {

    Primitive primEvent = new Primitive(eventName, className,eventModifier,
											                  methodSignature,instance, detectMode);
    if ((eventNamesEventNodes.put(eventName, primEvent)) != null) {
      System.out.println("ERROR: Event with name " + eventName + "has already been registered");
      return null;
    }

    String eventSignature = Utilities.remWhiteSpaces(className+eventModifier.getId()+methodSignature);
    Vector eventNodeVector = null;
    eventNodeVector	= (Vector) eventSignaturesEventNodes.get(eventSignature);
    if (eventNodeVector	== null) {
      eventNodeVector	= new Vector();
      eventSignaturesEventNodes.put(eventSignature,eventNodeVector);
    }
    eventNodeVector.addElement(primEvent);
    PrimitiveEventHandle eh = new PrimitiveEventHandle(primEvent,eventName,
														      className,methodSignature);
    eventNamesEventHandles.put(eventName,eh);
    eventHandlesEcaAgentInstances.put(eh,this);
        //<GED>
    setFwdFlagAndContextIfAny(primEvent);
    //<GED>

    return eh;
  }


  // modified by wtanpisu
  // what : add className parameter
  // rational: use for reflection mechanism to get the instance of runtime class

  /**
   * This method creates an absolute temporal event.
   * @param eventName	The name of the absolute temporal event
   * @param className	The name of the Java class raising the event.
   * @param timeString	The time expression for the absolute event.
   */
  public EventHandle createPrimitiveEvent(String eventName,
                                          String className,
                                          String timeString ) {

    Object temporalInstance = null;
    try{
      Class reflectedClass = Class.forName(className);
      temporalInstance = reflectedClass.newInstance();
    }
    catch(ClassNotFoundException cnfe){
      System.out.println("ERROR !! Class name for temporal event is not found");
      cnfe.printStackTrace();
    }
    catch(IllegalAccessException iacce) {
      iacce.printStackTrace();
    }
    catch(InstantiationException ie) {
      ie.printStackTrace();
    }

    Primitive absoluteEvent = new Primitive(timeString, null , temporalInstance);
    Timer.timer.addAbsoluteItem(absoluteEvent,timeString,0,ruleScheduler,notifyBuffer);
    PrimitiveEventHandle peh = new PrimitiveEventHandle(absoluteEvent,eventName,"ABSOLUTE",timeString);
    eventNamesEventHandles.put(eventName,peh);
    eventHandlesEcaAgentInstances.put(peh,this);
        //<GED>
    setFwdFlagAndContextIfAny(absoluteEvent);
    //<GED>


    return peh;
  }
  // modified on 03/31/2000



  // modified by wtanpisu
  // what : add className parameter
  // rational: use for reflection mechanism to get the instance of runtime class

  /** This method creates an absolute temporal event.
   * @param eventName	The name of the absolute temporal event
   * @param className	The name of the Java class raising the event.
   * @param timeString	The time expression for the absolute event.
   * @param instance	The object whose value is monitored when the absolute event occurs.
   */
  public EventHandle createPrimitiveEvent(String eventName,
                                          String className,
            	                          String timeString,
					  Object instance ) {

    Object temporalInstance = null;
    try{
      Class reflectedClass = Class.forName(className);
      temporalInstance = reflectedClass.newInstance();
    }
    catch(ClassNotFoundException cnfe){
      System.out.println("ERROR !! Class name for temporal event is not found");
      cnfe.printStackTrace();
    }
    catch(IllegalAccessException iacce) {
      iacce.printStackTrace();
    }
    catch(InstantiationException ie) {
      ie.printStackTrace();
    }

    Primitive absoluteEvent = new Primitive(timeString, instance, temporalInstance);
    Timer.timer.addAbsoluteItem(absoluteEvent,timeString,0,ruleScheduler,notifyBuffer);
    PrimitiveEventHandle peh = new PrimitiveEventHandle(absoluteEvent,eventName,"ABSOLUTE",timeString);
    eventNamesEventHandles.put(eventName,peh);
    eventHandlesEcaAgentInstances.put(peh,this);
    //<GED>
    setFwdFlagAndContextIfAny(absoluteEvent);
    //<GED>

    return peh;
  }
  // modified on 03/31/2000



  /** This method will creates a primitive event of class level with filter
   *  Before the event propation, the filter expression will be evaluated.
   *  added by @author seyang on 19 Dec 99
   *
   * @param reactive a value of type 'Reactive'
   * @param eventName String
   * @param className String
   * @param eventModifier a value of type 'EventModifier'
   * @param methodSignature a value of type 'String'
   * @param detectMode a value of type 'DetectionMode'
   * @param Filter a value of type 'String'
   * @return a value of type 'EventHandle'
   */
  // modified on 19 Dec 99 to get event Name and class name
  public  EventHandle createPrimitiveEvent (String eventName,
                                            String className,
                                            EventModifier eventModifier,
                                            String methodSignature,
					    DetectionMode detectMode,
					    String Filter){

    // pipe the primitive event into filterEvent
    // following decorator design pattern and minimizing the code change for filtering
    // last modified on 7-Oct-99 to put primtive event to eventNodeVector
    Primitive primEvent = null;
    FilterEvent filterEvent = new FilterEvent(
    primEvent = new Primitive(eventName,className,eventModifier,
        methodSignature,detectMode),new StringFilter(Filter));
    // load the JavaScript only when it is needed.
    // modified on 19 Dec 99
    // The java script engine will be loaded at run time.
    // reactive.loadEngine();
    // modified on 19 Dec 99
    // added by seyang, copied from other block, which was put by rajesh. 19 Sept 99
    String eventSignature = Utilities.remWhiteSpaces(className +eventModifier.getId()+methodSignature);
    if (eventDetectionDebug)
  	System.out.println("EVENT SIGNATURE = " + eventSignature);
    Vector eventNodeVector = null;
    eventNodeVector	= (Vector) eventSignaturesEventNodes.get(eventSignature);
    if (eventNodeVector	== null) {
      eventNodeVector	= new Vector();
      eventSignaturesEventNodes.put(eventSignature,eventNodeVector);
    }

    // modified on 23 Dec 99
    // eventNodeVector.addElement(primEvent);
    eventNodeVector.addElement(filterEvent);
    // modified on 23 Dec 99
    // added by seyang, copied from other block, which was put by rajesh. 19 Sept 99
    // Note that event handle contains the filtering event with filter string, not primitive event
    PrimitiveEventHandle eh = new PrimitiveEventHandle(filterEvent,
    	   eventName,className,methodSignature);
    // added on 19 Dec 99, copying from other API.
    eventNamesEventHandles.put(eventName, eh);
    eventHandlesEcaAgentInstances.put(eh, this);

        //<GED>
    setFwdFlagAndContextIfAny(primEvent);
    //<GED>

    return eh;
  }

  /** This method will creates a primitive event of intance level with Filter
   *  added by @author seyang on 24 July 99
   *
   * @param eventName String
   * @param className String
   * @param eventModifier a value of type 'EventModifier'
   * @param methodSignature a value of type 'String'
   * @param detectMode a value of type 'DetectionMode'
   * @param filter a value of type 'FilterAdapter'
   * @return a value of type 'EventHandle'
   */
  public  EventHandle createPrimitiveEvent( String eventName,
                                            String className,
                                            EventModifier eventModifier,
                                            String methodSignature,
					    DetectionMode detectMode,
					    FilterAdapter filter){

    // pipe the primitive event into filterEvent
    // following decorator design pattern and minimizing the code change for filtering
    // last modified on 7-Oct-99 to put primtive event to eventNodeVector
    Primitive primEvent = null;
    FilterEvent filterEvent = new FilterEvent(primEvent = new Primitive(eventName,
        className, eventModifier,methodSignature,detectMode),filter);

    // We dont need to load the JavaScript in this case.
    // expecting performance improvements.

    // added by seyang, copied from other block, which was put by rajesh. 19 Sept 99
    String eventSignature = Utilities.remWhiteSpaces(className+eventModifier.getId()+methodSignature);
    if (eventDetectionDebug)
      System.out.println("EVENT SIGNATURE = " + eventSignature);

    Vector eventNodeVector = null;
    eventNodeVector	= (Vector) eventSignaturesEventNodes.get(eventSignature);
    if (eventNodeVector	== null) {
      eventNodeVector	= new Vector();
      eventSignaturesEventNodes.put(eventSignature,eventNodeVector);
    }

    // modifed on 23 Dec 99
    eventNodeVector.addElement(filterEvent);

    // added by seyang, copied from other block, which was put by rajesh. 19 Sept 99
    // Note that event handle contains the filtering event with filter string, not primitive event
    PrimitiveEventHandle eh = new PrimitiveEventHandle(filterEvent,
      	   eventName,className,methodSignature);
    // added on 19 Dec 99, copying from other API.
    eventNamesEventHandles.put(eventName, eh);
    eventHandlesEcaAgentInstances.put(eh, this);

    //<GED>
    setFwdFlagAndContextIfAny(primEvent);
    //<GED>

    return eh;
  }

  /** This method will creates a primitive event of instance level with filter
   *  Before the event propation, the filter expression will be evaluated.
   *  added by @author seyang on 9 Aug 99
   *
   * @param reactive a value of type 'Reactive'
   * @param eventName String
   * @param className String
   * @param eventModifier a value of type 'EventModifier'
   * @param methodSignature a value of type 'String'
   * @param detectMode a value of type 'DetectionMode'
   * @param Filter a value of type 'String'
   * @return a value of type 'EventHandle'
   */
  // modified on 19 Dec 99 to get event Name and class name
  public  EventHandle createPrimitiveEvent( Reactive reactive,
                                            String eventName,
                                            String className,
                                            EventModifier eventModifier,
                                            String methodSignature,
                                            DetectionMode detectMode,
                                            String Filter) {

    // pipe the primitive event into filterEvent
    // following decorator design pattern and minimizing the code change for filtering
    // last modified on 7-Oct-99 to put primtive event to eventNodeVector
    Primitive primEvent = null;
    FilterEvent filterEvent = new FilterEvent(primEvent = new Primitive(eventName,
        className,eventModifier,methodSignature,reactive, detectMode),
        new StringFilter(reactive, Filter));

    // load the JavaScript only when it is needed.
    reactive.loadEngine();

    // added by seyang, copied from other block, which was put by rajesh. 19 Sept 99
    String eventSignature = Utilities.remWhiteSpaces(className +eventModifier.getId()+methodSignature);
    if (eventDetectionDebug)
      System.out.println("EVENT SIGNATURE = " + eventSignature);

    Vector eventNodeVector = null;
    eventNodeVector	= (Vector) eventSignaturesEventNodes.get(eventSignature);
    if (eventNodeVector	== null) {
      eventNodeVector	= new Vector();
      eventSignaturesEventNodes.put(eventSignature,eventNodeVector);
    }

    // modified on 23 Dec
    eventNodeVector.addElement(filterEvent);
    // added by seyang, copied from other block, which was put by rajesh. 19 Sept 99
    // Note that event handle contains the filtering event with filter string, not primitive event
    PrimitiveEventHandle eh = new PrimitiveEventHandle(filterEvent,
	eventName,className,methodSignature);
    // added on 19 Dec 99, copying from other API.
    eventNamesEventHandles.put(eventName, eh);
    eventHandlesEcaAgentInstances.put(eh, this);
    return eh;
  }

  /** This method will creates a primitive event of intance level with Filter
   *  added by @author seyang on 24 July 99
   *
   * @param reactive a value of type 'Reactive'
   * @param eventName String
   * @param className String
   * @param eventModifier a value of type 'EventModifier'
   * @param methodSignature a value of type 'String'
   * @param detectMode a value of type 'DetectionMode'
   * @param filter a value of type 'FilterAdapter'
   * @return a value of type 'EventHandle'
   */
  public  EventHandle createPrimitiveEvent( Reactive reactive,
                                            String eventName,
                                            String className,
                                            EventModifier eventModifier,
                                            String methodSignature,
                                            DetectionMode detectMode,
                                            FilterAdapter filter) {

    // pipe the primitive event into filterEvent
    // following decorator design pattern and minimizing the code change for filtering
    // last modified on 7-Oct-99 to put primtive event to eventNodeVector
    Primitive primEvent = null;
    FilterEvent filterEvent = new FilterEvent(primEvent = new Primitive(eventName,
          className,eventModifier,methodSignature,reactive, detectMode), filter);
    // We dont need to load the JavaScript in this case.
    // expecting performance improvements.

    // added by seyang, copied from other block, which was put by rajesh. 19 Sept 99
    String eventSignature = Utilities.remWhiteSpaces(className+eventModifier.getId()+methodSignature);
    if (eventDetectionDebug)
      System.out.println("EVENT SIGNATURE = " + eventSignature);

    Vector eventNodeVector = null;
    eventNodeVector	= (Vector) eventSignaturesEventNodes.get(eventSignature);
    if (eventNodeVector	== null) {
      eventNodeVector	= new Vector();
      eventSignaturesEventNodes.put(eventSignature,eventNodeVector);
    }
    eventNodeVector.addElement(filterEvent);

    // added by seyang, copied from other block, which was put by rajesh. 19 Sept 99
    // Note that event handle contains the filtering event with filter string, not primitive event
    PrimitiveEventHandle eh = new PrimitiveEventHandle(filterEvent,
                              eventName,className, methodSignature);
    // added on 19 Dec 99, copying from other API.
    eventNamesEventHandles.put(eventName, eh);
    eventHandlesEcaAgentInstances.put(eh, this);
    return eh;
  }

  //<GED>
  /**
   * This method will creates a global event
   *
   * @author Weera Tanpisuth
   *
   * @param consEventName a event name.
   * @param className     a class name in which the global event is created
   * @param prodEventName a event name at the producer site
   * @param appName       a application name in which the event of interest is defined
   * @param machName      a machine name
   * @return a event handler of event
   */

  public  EventHandle createPrimitiveEvent(String consEventName, String className, String prodEventName, String appName,String machName){
    if(glbEvntCreatnDebug)
      System.out.println("\nECAAgent::createGlobalPrimitiveEvent");

    ledIntf.consumePrimGlbEvnt (prodEventName, appName, machName,this);
    return remEvntFactry.createRemEvnt (consEventName,prodEventName,appName,machName,remNdMngr);
  }
  //</GED>


  /** This method creates a composite event for a binary operator.
   *  (AND, SEQUENCE, OR)
   *
   * @param eventType   The binary operator used for the composite event.
   * @param eventName	The name of the composite event.
   * @param leftEvent	The event handle of the left event.
   * @param rightEvent	The event handle of the right event.
   */
  public EventHandle createCompositeEvent(EventType eventType,
                                          String eventName,
                                          EventHandle leftEvent,
                                          EventHandle rightEvent) {

    // <GED>
    if(Constant.SCOPE.equals ("GLOBAL")){
      if(glbEvntCreatnDebug)
        System.out.println("\nCreate AND composite global");

      /**
       *  When both of constituent events are global event, create the composite
       *  event node at the server site.
       */
      if((leftEvent instanceof RemoteEventHandle) &&(rightEvent instanceof RemoteEventHandle)){

        if(evntCreatnDebug)
        System.out.println("This event node will be detected in server");

        // Remark !!! need to create one remote node which represents the composite event on the local site

        RemoteEventHandle lEvnt = (RemoteEventHandle) leftEvent;

        // primitive global event name = producerEvntNm+appNm+"_"+machNm

        StringBuffer lGlbNm = new StringBuffer(lEvnt.getProdEvntNm());
        lGlbNm.append (lEvnt.getAppNm ());
        lGlbNm.append (lEvnt.getMachNm ());

        RemoteEventHandle rEvnt = (RemoteEventHandle) rightEvent;
        StringBuffer rGlbNm = new StringBuffer(rEvnt.getProdEvntNm());
        rGlbNm.append (rEvnt.getAppNm ());
        rGlbNm.append (rEvnt.getMachNm ());

        // composite global event name = "evntType"+leftGlbEvnt+rightGlbEvnt

        String glbEvntName = eventType.getName()+lGlbNm+rGlbNm;

        ledIntf.consumeCompGlbEvnt (eventType,glbEvntName,lGlbNm.toString(),rGlbNm.toString(),this);

        /*Compostie Event Name on the server
         *  (eventType)(leftEventName)(rightEventName)
         *  eg. AND event , G1 , G2
         *  compositeGlobalEventName = ANDG1G2
         */


        EventHandle eh =  remEvntFactry.createRemEvnt (eventName,glbEvntName,"","",remNdMngr);
        setFwdFlagAndContextIfAny(eh.getEventNode());
        return eh;
      }
    }
    // <\GED>

    CompositeEventHandle ceh = null;
    Event compEvnt = null;

    if(eventType == EventType.AND) {
      And andEvent = new And(eventName,leftEvent, rightEvent);
      compEvnt = andEvent;
	  if ((eventNamesEventNodes.put(eventName, andEvent)) != null)
	    System.out.println("ERROR: Event with name " + eventName +
		  "has already been registered");
      ceh = new CompositeEventHandle(andEvent, eventName);
    }
    else if(eventType == EventType.SEQ) {
      Sequence seqEvent = new Sequence(eventName,leftEvent, rightEvent);
      compEvnt = seqEvent;
	  if ((eventNamesEventNodes.put(eventName, seqEvent)) != null)
	    System.out.println("ERROR: Event with name " + eventName +
				 "has already been registered");
      ceh = new CompositeEventHandle(seqEvent, eventName);
    }
    else if(eventType == EventType.OR) {
      Or orEvent = new Or(eventName,leftEvent, rightEvent);
      compEvnt = orEvent;
	  if ((eventNamesEventNodes.put(eventName, orEvent)) != null)
	    System.out.println("ERROR: Event with name " + eventName +
        "has already been registered");
      ceh = new CompositeEventHandle(orEvent, eventName);
    }
    else {
      System.err.println("ERROR:: Can't create an composite event ::"+ eventName);
      System.err.println("Check the parameters in createCompositeEvent( )");
      System.exit(0);
    }


    // <GED>
    // Check whether this event is subcribed by the remote applicication
//    if(Constant.SCOPE.equals ("GLOBAL")){
      setFwdFlagAndContextIfAny(compEvnt);
      /*if(isConsumed(compEvnt)){
        prodEventName_ReqstContextsHt.put (eventName,compEvnt);
        if(evntCreatnDebug)
          System.out.println("Set the global flag at " + eventName);
        compEvnt.setForwardFlag();
      }*/
  //  }
    //</GED>

    eventNamesEventHandles.put(eventName,ceh);
    return ceh;
  }

  // added by seyang on 24 July 99
  // activated by seyang on 19 Sept 99
  /**
   * create binary composite event on reactive class, based on pre-defined eventhandle
   *
   * @param reactive a value of type 'Reactive'
   * @param eventname a value of type 'String'
   * @param eventType a value of type 'EventType'
   * @param leftEvent a value of type 'EventHandle'
   * @param rightEvent a value of type 'EventHandle'
   * @return a value of type 'EventHandle'
   */
  public EventHandle createCompositeEvent(Reactive reactive,
	                                        String eventname,
                                          EventType eventType,
                                          EventHandle leftEvent,
                                          EventHandle rightEvent) {

    CompositeEventHandle ceh = null;
    String eventName = reactive.toString() + eventname;

    if (leftEvent == rightEvent) {
      return null;
    }

    if(eventType == EventType.AND) {
      And andEvent = new And(eventName,leftEvent, rightEvent);
      setFwdFlagAndContextIfAny(andEvent);
      // added by seyang, copied from other block, which was put by rajesh. 19 Sept 99
      if ((eventNamesEventNodes.put(eventName, andEvent)) != null)
        System.out.println("ERROR: Event with name " + eventName +
		 "has already been registered");
      // added by seyang, copied from other block, which was put by rajesh. 19 Sept 99

      ceh = new CompositeEventHandle(andEvent, eventName);
    }
    else if(eventType == EventType.SEQ) {
      Sequence seqEvent = new Sequence(eventName,leftEvent, rightEvent);
      setFwdFlagAndContextIfAny(seqEvent);
      // added by seyang, copied from other block, which was put by rajesh. 19 Sept 99
      if ((eventNamesEventNodes.put(eventName, seqEvent)) != null)
        System.out.println("ERROR: Event with name " + eventName +
		 "has already been registered");
      // added by seyang, copied from other block, which was put by rajesh. 19 Sept 99
      ceh = new CompositeEventHandle(seqEvent, eventName);
    }
    else if(eventType == EventType.OR) {
      Or orEvent = new Or(eventName,leftEvent, rightEvent);
      setFwdFlagAndContextIfAny(orEvent);
      // added by seyang, copied from other block, which was put by rajesh. 19 Sept 99
      if ((eventNamesEventNodes.put(eventName, orEvent)) != null)
        System.out.println("ERROR: Event with name " + eventName +
		 "has already been registered");
      // added by seyang, copied from other block, which was put by rajesh. 19 Sept 99
      ceh = new CompositeEventHandle(orEvent, eventName);
    }
     else {
      System.err.println("ERROR:: Can't create an composite event ::"+ eventName);
      System.err.println("Check the parameters in createCompositeEvent( )");
      System.exit(0);
    }

    eventNamesEventHandles.put(eventName,ceh);
    return ceh;
  }
  // activated by seyang on 19 Sept 99

  /**
   * create binary composite event on reactive class with string filter, based on pre-defined eventhandle
   * added on by seyang on 9 Aug 99
   *
   * @param reactive a value of type 'Reactive'
   * @param eventname a value of type 'String'
   * @param eventType a value of type 'EventType'
   * @param leftEvent a value of type 'EventHandle'
   * @param rightEvent a value of type 'EventHandle'
   * @param filter a value of type 'String'
   * @return a value of type 'EventHandle'
   */
  public EventHandle createCompositeEvent(Reactive reactive,
						 String eventname,
						 EventType eventType,
						 EventHandle leftEvent,
						 EventHandle rightEvent,
						 String filter) {

    CompositeEventHandle ceh = null;
    String eventName = reactive.toString() + eventname;

    if (leftEvent == rightEvent) {
      // throw new IllegalEventDefinitionException(eventType, eventName);
      return null;
    }
    // load a JavaScript Engine, only if StringFilter is passed.
    reactive.loadEngine();

    if(eventType == EventType.AND) {
      And andEvent = new And(eventName,leftEvent, rightEvent,new StringFilter(reactive, filter));
      setFwdFlagAndContextIfAny(andEvent);
      // added by seyang, copied from other block, which was put by rajesh. 19 Sept 99
      if ((eventNamesEventNodes.put(eventName, andEvent)) != null)
        System.out.println("ERROR: Event with name " + eventName +
	 "has already been registered");
      // added by seyang, copied from other block, which was put by rajesh. 19 Sept 99
      ceh = new CompositeEventHandle(andEvent, eventName);
    }
    else if(eventType == EventType.SEQ) {
      Sequence seqEvent = new Sequence(eventName,leftEvent, rightEvent,
          new StringFilter(reactive, filter));
      setFwdFlagAndContextIfAny(seqEvent);
      // added by seyang, copied from other block, which was put by rajesh. 19 Sept 99
      if ((eventNamesEventNodes.put(eventName, seqEvent)) != null)
        System.out.println("ERROR: Event with name " + eventName +
	   "has already been registered");

      // added by seyang, copied from other block, which was put by rajesh. 19 Sept 99
      ceh = new CompositeEventHandle(seqEvent, eventName);
    }
    else if(eventType == EventType.OR) {
      Or orEvent = new Or(eventName,leftEvent, rightEvent,
          new StringFilter(reactive, filter));
      setFwdFlagAndContextIfAny(orEvent);
      // added by seyang, copied from other block, which was put by rajesh. 19 Sept 99
      if ((eventNamesEventNodes.put(eventName, orEvent)) != null)
	System.out.println("ERROR: Event with name " + eventName +
            "has already been registered");

      // added by seyang, copied from other block, which was put by rajesh. 19 Sept 99
      ceh = new CompositeEventHandle(orEvent, eventName);
    }
    eventNamesEventHandles.put(eventName,ceh);
    return ceh;
  }

  public EventHandle createCompositeEvent(Reactive reactive,
                                          String eventname,
                                          EventType eventType,
                                          EventHandle leftEvent,
                                          EventHandle rightEvent,
                                          FilterAdapter filter) {

    CompositeEventHandle ceh = null;
    String eventName = reactive.toString() + eventname;

    if (leftEvent == rightEvent) {
      // throw new IllegalEventDefinitionException(eventType, eventName);
      return null;
    }
    // does not need to load Javascript engin for each reactive class

    if(eventType == EventType.AND) {
      And andEvent = new And(eventName,leftEvent, rightEvent,filter);
      setFwdFlagAndContextIfAny(andEvent);
      // added by seyang, copied from other block, which was put by rajesh. 19 Sept 99
      if ((eventNamesEventNodes.put(eventName, andEvent)) != null)
        System.out.println("ERROR: Event with name " + eventName +"has already been registered");

      // added by seyang, copied from other block, which was put by rajesh. 19 Sept 99
      ceh = new CompositeEventHandle(andEvent, eventName);
    }
    else if(eventType == EventType.SEQ) {
      Sequence seqEvent = new Sequence(eventName,leftEvent, rightEvent,filter);
      setFwdFlagAndContextIfAny(seqEvent);
      // added by seyang, copied from other block, which was put by rajesh. 19 Sept 99
      if ((eventNamesEventNodes.put(eventName, seqEvent)) != null)
        System.out.println("ERROR: Event with name " + eventName +"has already been registered");

      // added by seyang, copied from other block, which was put by rajesh. 19 Sept 99
      ceh = new CompositeEventHandle(seqEvent, eventName);
    }
    else if(eventType == EventType.OR) {
      Or orEvent = new Or(eventName,leftEvent, rightEvent,filter);
      setFwdFlagAndContextIfAny(orEvent);

      // added by seyang, copied from other block, which was put by rajesh. 19 Sept 99
      if ((eventNamesEventNodes.put(eventName, orEvent)) != null)
        System.out.println("ERROR: Event with name " + eventName +"has already been registered");
      // added by seyang, copied from other block, which was put by rajesh. 19 Sept 99
      ceh = new CompositeEventHandle(orEvent, eventName);
    }
    else {
      System.err.println("ERROR:: Can't create an composite event ::"+ eventName);
      System.err.println("Check the parameters in createCompositeEvent( )");
      System.exit(0);
    }


    eventNamesEventHandles.put(eventName,ceh);
    return ceh;
  }



  /** This method creates a composite event for a ternary operator.
   *  (NOT, APERIODIC, APERIODIC-STAR)
   *
   * @param eventType	The ternary operator used for the composite	event.
   * @param eventName		The name of the composite event.
   * @param leftEvent		The event handle of the left event.
   * @param middleEvent	The event handle of the middle event.
   * @param rightEvent	The event handle of the right event.
   */
  public EventHandle createCompositeEvent(EventType eventType,
                                          String eventName,
                                          EventHandle leftEvent,
                                          EventHandle middleEvent,
                                          EventHandle rightEvent) {

    // <GED>
    if((eventType == EventType.NOT) && Constant.SCOPE.equals ("GLOBAL")){
      if(glbEvntCreatnDebug)
        System.out.println("\nCreate NOT composite global");

      // When both of constituent events are global event, create the composite
      // event node at the server site.

      if( (leftEvent instanceof RemoteEventHandle)
          &&(middleEvent instanceof RemoteEventHandle)
          &&(rightEvent instanceof RemoteEventHandle)){
        if(evntCreatnDebug)
          System.out.println("This event node will be detected in server");

        // create one remote node which represents the composite event on the local site

        RemoteEventHandle lEvnt = (RemoteEventHandle) leftEvent;

        StringBuffer lGlbNm = new StringBuffer(lEvnt.getProdEvntNm());
        lGlbNm.append (lEvnt.getAppNm ());
        lGlbNm.append (lEvnt.getMachNm ());

        RemoteEventHandle mEvnt = (RemoteEventHandle) middleEvent;

        StringBuffer mGlbNm = new StringBuffer(mEvnt.getProdEvntNm());
        mGlbNm.append (mEvnt.getAppNm ());
        mGlbNm.append (mEvnt.getMachNm ());

        RemoteEventHandle rEvnt = (RemoteEventHandle) rightEvent;
        StringBuffer rGlbNm = new StringBuffer(rEvnt.getProdEvntNm());
        rGlbNm.append (rEvnt.getAppNm ());
        rGlbNm.append (rEvnt.getMachNm ());

        // composite global event name = "evntType"+leftGlbEvnt+rightGlbEvnt

        String glbEvntName = eventType.getName()+lGlbNm+mGlbNm+rGlbNm;

        ledIntf.consumeCompGlbEvnt (eventType,glbEvntName,lGlbNm.toString(),mGlbNm.toString(), rGlbNm.toString(),this);

        // Compostie Event Name on the server
        // (eventType)(leftEventName)(rightEventName)
        // eg. AND event , G1 , G2
        // compositeGlobalEventName = ANDG1G2
        EventHandle eh = remEvntFactry.createRemEvnt (eventName,glbEvntName,"","",remNdMngr);
        setFwdFlagAndContextIfAny(eh.getEventNode());
        return eh;
      }
    }
    // <\GED>

    CompositeEventHandle ceh = null;
    Event compEvnt = null;
    if(eventType == EventType.NOT) {
      Not notEvent = new Not(eventName,leftEvent,middleEvent,rightEvent);
      compEvnt = notEvent;
      if ((eventNamesEventNodes.put(eventName, notEvent)) != null)
        System.out.println("ERROR: Event with name " + eventName +
	     "has already been registered");
      ceh = new CompositeEventHandle(notEvent, eventName);
    }
    else if (eventType == EventType.APERIODIC) {
      Aperiodic aperiodicEvent = new Aperiodic(eventName,leftEvent,middleEvent,rightEvent);
      compEvnt = aperiodicEvent;
      if ((eventNamesEventNodes.put(eventName, aperiodicEvent)) != null)
        System.out.println("ERROR: Event with name " + eventName +
            "has already been registered");
      ceh = new CompositeEventHandle(aperiodicEvent, eventName);
    }
    else if (eventType == EventType.APERIODICSTAR) {
      AperiodicStar aStarEvent = new AperiodicStar(eventName,leftEvent,middleEvent,rightEvent);
      compEvnt = aStarEvent;
      if ((eventNamesEventNodes.put(eventName, aStarEvent)) != null)
        System.out.println("ERROR: Event with name " + eventName +
            "has already been registered");
      ceh = new CompositeEventHandle(aStarEvent, eventName);
    }
    else {
      System.err.println("ERROR:: Can't create an composite event ::"+ eventName);
      System.err.println("Check the parameters in createCompositeEvent( )");
      System.exit(0);
    }


    // <GED>
    // Check whether this event is subcribed by the remote applicication
//    if(Constant.SCOPE.equals ("GLOBAL")){
      setFwdFlagAndContextIfAny(compEvnt);
/*      if(isConsumed(compEvnt)){
        prodEventName_ReqstContextsHt.put (eventName,compEvnt);
        if(evntCreatnDebug)
          System.out.println("Set the global flag at " + eventName);
        compEvnt.setForwardFlag();
      }*/
  //  }
    //</GED>

    eventNamesEventHandles.put(eventName,ceh);
    return ceh;
  }

  /** This method creates a composite event for a PLUS operator.
   *
   * @param eventType		The PLUS event operator
   * @param eventName		The name of the composite event.
   * @param leftEvent		The event handle of the left event.
   * @param timeString	The time expression for the right event.
   */
   public EventHandle createCompositeEvent(EventType eventType,
                                            String eventName,
                                            EventHandle leftEvent,
                                            String timeString) {

    CompositeEventHandle ceh = null;
    if (eventType == EventType.PLUS) {
      Plus plusEvent = new Plus(eventName,leftEvent, timeString);
      if ((eventNamesEventNodes.put(eventName, plusEvent)) != null)
        System.out.println("ERROR: Event with name " + eventName +
          "has already been registered");
      ceh = new CompositeEventHandle(plusEvent, eventName);
      setFwdFlagAndContextIfAny(plusEvent);
    }
    eventNamesEventHandles.put(eventName,ceh);
    return ceh;
  }

  /** This method creates a composite event for the ternary operators
   * PERIODIC and PERIODIC-STAR
   *
   * @param eventType	The ternary operator used for the composite	event.
   * @param eventName		The name of the composite event.
   * @param leftEvent		The event handle of the left event.
   * @param timeString	The time expression for the middle event.
   * @param rightEvent	The event handle of the right event.
   */
  public EventHandle createCompositeEvent(EventType eventType,
                                            String eventName,
                                            EventHandle leftEvent,
                                            String timeString,
                                            EventHandle rightEvent) {

    CompositeEventHandle ceh = null;
    if (eventType == EventType.PERIODIC) {
      Periodic periodicEvent = new Periodic(eventName,leftEvent, timeString, null, rightEvent);
      if ((eventNamesEventNodes.put(eventName, periodicEvent)) != null)
        System.out.println("ERROR: Event with name " + eventName +
  	  "has already been registered");
      ceh = new CompositeEventHandle(periodicEvent, eventName);
      setFwdFlagAndContextIfAny(periodicEvent);

    }
    else if (eventType == EventType.PERIODICSTAR) {
      PeriodicStar pStarEvent = new PeriodicStar(eventName,leftEvent, timeString, null, rightEvent);
      if ((eventNamesEventNodes.put(eventName, pStarEvent)) != null)
        System.out.println("ERROR: Event with name " + eventName +
	  "has already been registered");
      ceh = new CompositeEventHandle(pStarEvent, eventName);
      setFwdFlagAndContextIfAny(pStarEvent);
    }
    else {
      System.err.println("ERROR:: Can't create an composite event ::"+ eventName);
      System.err.println("Check the parameters in createCompositeEvent( )");
      System.exit(0);
    }

    eventNamesEventHandles.put(eventName,ceh);
    return ceh;
  }

  /** This method creates a composite event for the ternary operators
   * PERIODIC and PERIODIC-STAR
   *
   * @param eventType   The ternary operator used for the composite	event.
   * @param eventName	The name of the composite event.
   * @param leftEvent	The event handle of the left event.
   * @param timeString	The time expression for the middle event.
   * @param instance	The object whose value is being monitored in the
   *						interval specified by the left and right events.
   * @param rightEvent	The event handle of the right event.
   */
  public EventHandle createCompositeEvent(EventType eventType,
                                            String eventName,
                                            EventHandle leftEvent,
                                            String timeString,
                                            Object instance,
                                            EventHandle rightEvent) {

    CompositeEventHandle ceh = null;
    if (eventType == EventType.PERIODIC) {
      Periodic periodicEvent = new Periodic(eventName,leftEvent, timeString, instance, rightEvent);
      if ((eventNamesEventNodes.put(eventName, periodicEvent)) != null)
        System.out.println("ERROR: Event with name " + eventName +
	  "has already been registered");
      ceh = new CompositeEventHandle(periodicEvent,eventName);
      setFwdFlagAndContextIfAny(periodicEvent);
    }
    else if (eventType == EventType.PERIODICSTAR) {
      PeriodicStar pStarEvent = new PeriodicStar(eventName,leftEvent, timeString, instance, rightEvent);
      if ((eventNamesEventNodes.put(eventName, pStarEvent)) != null)
	 System.out.println("ERROR: Event with name " + eventName +
	    "has already been registered");
      ceh = new CompositeEventHandle(pStarEvent,eventName);
      setFwdFlagAndContextIfAny (pStarEvent);
    }
    else {
      System.err.println("ERROR:: Can't create an composite event ::"+ eventName);
      System.err.println("Check the parameters in createCompositeEvent( )");
      System.exit(0);
    }

    eventNamesEventHandles.put(eventName,ceh);
    return ceh;
  }

  private static Method getMethod(String method) {

    int i = method.lastIndexOf(".");
    String className = method.substring(0,i);
    String methodName = method.substring(++i);
    Method methodObj = Utilities.getMethodObject(className,methodName,
                                          "sentinel.led.ListOfParameterLists");
    return methodObj;
  }

  // added by seyang to get method with one object signature
  // on 24 July 99
  private static Method getUserMethod(String method) {
    int i = method.lastIndexOf(".");
    String className = method.substring(0,i);
    String methodName = method.substring(++i);
    Method methodObj = Utilities.getMethodObject(className,methodName,
        	                               "java.lang.Object");
    return methodObj;
  }

  protected NotifyBuffer getNotifyBuffer() {
     return notifyBuffer;
  }

  protected LEDThread getLEDThread() {
     return ledThread;
  }


  /**  This method creates a rule with the default context, coupling mode,
   *  triggering mode and priority.
   *
   *  Default Context : 		 RECENT
   *  Default Coupling mode :  IMMEDIATE
   *  Default Trigger mode :   NOW
   *  Default Priority :       1
   *
   *  @param ruleName	The name of the Rule.
   *  @param eventName	The name of the Event on which this Rule is	defined.
   *  @param condName	The fully qualified name of the condition method with
   *                      the class name and method name.
   *  @param actionName	The fully qualified name of the action method with the
   *  					class name and method name.
   */

  // create rule that has no event instance (instance level) and no rule instance.

  public Rule createRule(String ruleName, EventHandle eventHandle,
				 String condName, String actionName) {
    if (ruleNamesRuleNodes.containsKey(ruleName)) {
	  System.out.println("Fatal ERROR : Rule with name " + ruleName +
	    "already registered");
      return null;
    }
    if(ruleSchedulerDebug){
      System.out.println("Create this " +ruleName+" rule associcated with this "+eventHandle.getEventName()+" event.");
    }

    Method condMethod = getMethod(condName);
    Method actionMethod = getMethod(actionName);
    if(mainDebug)
      System.out.println("Create rule "+ruleName);

    Rule ruleNode = new Rule(ruleName, eventHandle, condMethod, actionMethod);
	  ruleNamesRuleNodes.put(ruleName, ruleNode);

    if(evntReqstDebug)
           System.out.println("Subscribe this rule "+ruleName+" to "+eventHandle.getEventName());


    addRule(eventHandle,ruleNode);
    ruleNode.setCondName(condName);
    ruleNode.setActionName(actionName);
    ruleNode.setRuleScheduler(ruleScheduler);
    return ruleNode;
  }

  /**  This method takes the user given priority for the Rule.
   *
   */

  // create rule that has no event instance (instance level) and no rule instance.

  public Rule createRule( String ruleName, EventHandle eventHandle,
                 String condName, String actionName,int priority) {

    if (ruleNamesRuleNodes.containsKey(ruleName)) {
      System.out.println("Fatal ERROR : Rule with name " + ruleName +
	       "already registered");
      return null;
    }

    if(ruleSchedulerDebug){
      System.out.println("Create this " +ruleName+" rule associcated with this "+eventHandle.getEventName()+" event.");
    }
    Method condMethod = getMethod(condName);
    Method actionMethod = getMethod(actionName);
    Rule ruleNode = new Rule(ruleName, eventHandle, condMethod, actionMethod, priority);
    ruleNamesRuleNodes.put(ruleName, ruleNode);
    addRule(eventHandle, ruleNode);
    ruleNode.setCondName(condName);
    ruleNode.setActionName(actionName);
    ruleNode.setRuleScheduler(ruleScheduler);
    return ruleNode;
  }

   /**  This method takes the user given Coupling mode for the Rule. The symbolic
     *  names for the different coupling modes are CouplingMode.IMMEDIATE,
     *  CouplingMode.DEFERRED and CouplingMode.DETACHED.
     *
     */

  // create rule that has no event instance (instance level) and no rule instance.

  public Rule createRule( String ruleName, EventHandle eventHandle,
        String condName, String actionName,int priority, CouplingMode coupling) {

    if (ruleNamesRuleNodes.containsKey(ruleName)) {
      System.out.println("Fatal ERROR : Rule with name " + ruleName +
	       "already registered");
      return null;
    }

    if(ruleSchedulerDebug){
      System.out.println("Create this " +ruleName+" rule associcated with this "+eventHandle.getEventName()+" event.");
    }
    Method condMethod = getMethod(condName);
    Method actionMethod = getMethod(actionName);
    Rule ruleNode = new Rule(ruleName, eventHandle, condMethod, actionMethod, priority, coupling);
    ruleNamesRuleNodes.put(ruleName, ruleNode);
    addRule(eventHandle, ruleNode);       // add the rule node to associate event
    ruleNode.setCondName(condName);
    ruleNode.setActionName(actionName);
    ruleNode.setRuleScheduler(ruleScheduler);
    return ruleNode;
  }

   /** This method takes the user given context for the rule.
     * The symbolic names for the different contexts are Context.RECENT, Context.CHRONICLE,
     * Context.CONTINUOUS and Context.CUMULATIVE.
     */

  // create rule that has no event instance (instance level) and no rule instance.

  public Rule createRule( String ruleName, EventHandle eventHandle,String condName,
      String actionName,    int priority, CouplingMode coupling, ParamContext context){

    if (ruleNamesRuleNodes.containsKey(ruleName)) {
      System.out.println("Fatal ERROR : Rule with name " + ruleName +
	       "already registered");
      return null;
    }
    if(ruleSchedulerDebug){
      System.out.println("Create this " +ruleName+" rule associcated with this "+eventHandle.getEventName()+" event.");
    }
    Method condMethod = getMethod(condName);
    Method actionMethod = getMethod(actionName);
    Rule ruleNode = new Rule(ruleName, eventHandle, condMethod, actionMethod, priority, coupling, context);
    ruleNamesRuleNodes.put(ruleName, ruleNode);
    addRule(eventHandle, ruleNode);
    ruleNode.setCondName(condName);
    ruleNode.setActionName(actionName);
    ruleNode.setRuleScheduler(ruleScheduler);
    return ruleNode;
  }

   /** This method takes the user given Triggering mode for the Rule. The symbolic
     *  names for the different trigger modes are TriggerMode.NOW and TriggerMode.PREVIOUS.
     *
     */

   // create rule that has no event instance (instance level) and no rule instance.

   public Rule createRule(String ruleName, EventHandle eventHandle,
				 String condName, String actionName,
				 int priority, CouplingMode coupling,
				 ParamContext context, TriggerMode triggerMode) {

    if (ruleNamesRuleNodes.containsKey(ruleName)) {
      System.out.println("Fatal ERROR : Rule with name " + ruleName +
	      "already registered");
      return null;
    }
    if(ruleSchedulerDebug){
      System.out.println("Create this " +ruleName+" rule associcated with this "+eventHandle.getEventName()+" event.");
    }
    Method condMethod = getMethod(condName);
    Method actionMethod = getMethod(actionName);
    Rule ruleNode = new Rule(ruleName, eventHandle, condMethod, actionMethod, priority, coupling, context, triggerMode);
    ruleNamesRuleNodes.put(ruleName, ruleNode);
    addRule(eventHandle, ruleNode);
    ruleNode.setCondName(condName);
    ruleNode.setActionName(actionName);
    ruleNode.setRuleScheduler(ruleScheduler);
    return ruleNode;
  }

   /** This method creates an instance level rule on the specified object instance.
     *
     */

  // create rule that has an event instance (instance level) but has no rule instance.

  public Rule createRule(String ruleName, EventHandle eventHandle,
                         String condName, String actionName,Object instance) {

    if (ruleNamesRuleNodes.containsKey(ruleName)) {
      System.out.println("Fatal ERROR : Rule with name " + ruleName +
			       "already registered");
      return null;
    }
    if(ruleSchedulerDebug){
      System.out.println("Create this " +ruleName+" rule associcated with this "+eventHandle.getEventName()+" event.");
    }
    Method condMethod = getMethod(condName);
    Method actionMethod = getMethod(actionName);
    if(mainDebug)
      System.out.println("Create rule "+ruleName);

    // Create rule node
    Rule ruleNode = new Rule(ruleName, eventHandle, condMethod, actionMethod);
    ruleNamesRuleNodes.put(ruleName, ruleNode);
    // Get the associated event node
    PrimitiveEventHandle primEventHandle = (PrimitiveEventHandle) eventHandle;

    String eventSig = primEventHandle.getBeginSignature();
    Vector eventNodeVector = (Vector) eventSignaturesEventNodes.get(eventSig);
    if (eventNodeVector == null) {
      eventSig = primEventHandle.getEndSignature();
      eventNodeVector = (Vector) eventSignaturesEventNodes.get(eventSig);
    }
    if (eventNodeVector == null) {
      System.out.println("ERROR! Sentinel: The event specified in the " +
			      "definition  of the instance level rule \"" +
				 ruleName + "\" does not exist");
    return null;
    }
    Primitive primEvent = null;
    for (int i=0; i<eventNodeVector.size(); i++) {
      primEvent = (Primitive) eventNodeVector.elementAt(i);
      if (primEvent.isClassLevelEvent())
        break;
    }

    // if(instance.getClass().getName() ==  eventHandle.className)
    //  System.out.println("The instance is the same as the event class");
    primEvent.addRule(ruleNode, instance);
    if(evntReqstDebug)
      System.out.println("Subscribe this rule "+ruleName+" to "+eventHandle.getEventName());

    ruleNode.setCondName(condName);
    ruleNode.setActionName(actionName);
    ruleNode.setRuleScheduler(ruleScheduler);
    return ruleNode;
  }

  /** This method creates an instance level rule on the specified object instance.
    * When the event occurs, this object target instance (an instance in any class)
    * can invoke a declared condition and action.
    *
    * This method creates an instance level rule on the specified object instance.
    *
    */

  // create rule that has event instance (instance level) and rule instance.

  public Rule createRule(Object targetInstance, String ruleName, EventHandle eventHandle,
                         String condName, String actionName,Object instance) {

    if (ruleNamesRuleNodes.containsKey(ruleName)) {
	    System.out.println("Fatal ERROR : Rule with name " + ruleName +
			       "already registered");
	    return null;
	  }
    if(ruleSchedulerDebug){
      System.out.println("Create this " +ruleName+" rule associcated with this "+eventHandle.getEventName()+" event.");
	  }
    Method condMethod = getMethod(condName);
    Method actionMethod = getMethod(actionName);
    if(mainDebug)
      System.out.println("Create rule "+ruleName);
    // Create rule node
    Rule ruleNode = new Rule(ruleName, eventHandle, condMethod, actionMethod,targetInstance);
    ruleNamesRuleNodes.put(ruleName, ruleNode);
    // Get the associated event node
    PrimitiveEventHandle primEventHandle = (PrimitiveEventHandle) eventHandle;

    String eventSig = primEventHandle.getBeginSignature();
    Vector eventNodeVector = (Vector) eventSignaturesEventNodes.get(eventSig);
    if (eventNodeVector == null) {
      eventSig = primEventHandle.getEndSignature();
      eventNodeVector = (Vector) eventSignaturesEventNodes.get(eventSig);
    }
    if (eventNodeVector == null) {
      System.out.println("ERROR! Sentinel: The event specified in the " +
        "definition  of the instance level rule \"" + ruleName + "\" does not exist");
    return null;
    }
    Primitive primEvent = null;
    for (int i=0; i<eventNodeVector.size(); i++) {
      primEvent = (Primitive) eventNodeVector.elementAt(i);
      if (primEvent.isClassLevelEvent())
        break;
    }
    primEvent.addRule(ruleNode, instance);
    if(evntReqstDebug)
      System.out.println("Subscribe this rule "+ruleName+" to "+eventHandle.getEventName());

    ruleNode.setCondName(condName);
    ruleNode.setActionName(actionName);
    ruleNode.setRuleScheduler(ruleScheduler);
    return ruleNode;
  }

  /** This method creates a rule of class level with dafault Context, CounplingMode
   *  , Trigger mode, and priority.
   * added by seyang on 24 July 99
   * @param reactive a value of type 'Reactive'
   * @param eventHandle a value of type 'EventHandle'
   * @param condName a value of type 'String'
   * @param actionName a value of type 'String'
   */

  // modified by wtanpisu
  // what : add ruleName parameter

  public Rule createRule( String ruleName,Reactive reactive,EventHandle eventHandle,
                          String condName,String actionName) {

    // modified on 03/31/2000

    Method condMethod = getMethod(condName);
    Method actionMethod = getMethod(actionName);
    Rule ruleNode = new Rule( reactive.toString()+ eventHandle.getEventName(),
                              eventHandle, condMethod, actionMethod);

    // added by wtanpisu
    ruleNamesRuleNodes.put(ruleName, ruleNode);
    // added on 03/31/2000

    addRule(eventHandle, ruleNode);
    ruleNode.setCondName(condName);
    ruleNode.setActionName(actionName);
    ruleNode.setRuleScheduler(ruleScheduler);
    return ruleNode;
  }

  // added by seyang
  /**
   * This method will create a rule on reactive oject with Condition, Action interface
   *
   * @param reactive a value of type 'Reactive'
   * @param eventHandle a value of type 'EventHandle'
   * @param condition a value of type 'Condition'
   * @param action a value of type 'Action'
   * @return a value of type 'Rule'
   */
  public Rule createRule( String ruleName,Reactive reactive,EventHandle eventHandle,
                          Condition condition,Action action) {
    if(mainDebug)
      System.out.println("Create rule "+ruleName);
    Rule ruleNode = new Rule(ruleName,reactive,eventHandle,condition, action);
    if(mainDebug)
      System.out.println("subscribe rule to "+eventHandle.getEventName());

    addRule(eventHandle, ruleNode);

    // added by wtanpisu
    ruleNamesRuleNodes.put(ruleName, ruleNode);
    ruleNode.setCondName("instant condName");
    ruleNode.setActionName("instant actionName");
    ruleNode.setRuleScheduler(ruleScheduler);
    // added on 03/31/2000

    return ruleNode;
  }



  /** This method creates an class level rule.
    * When the event occurs, this target rule instance will invoke
    * a declared condition and action.
    */

  // create rule that has no event instance (instance level) but has rule instance

  public Rule createRule(Object targetInstance, String ruleName, EventHandle eventHandle,
                         String condName, String actionName) {

    if (ruleNamesRuleNodes.containsKey(ruleName)) {
      System.out.println("Fatal ERROR : Rule with name " + ruleName +
	      "already registered");
      return null;
    }
    if(ruleSchedulerDebug){
      System.out.println("Create this " +ruleName+" rule associcated with this "+eventHandle.getEventName()+" event.");
    }
    Method condMethod = getMethod(condName);
    Method actionMethod = getMethod(actionName);
    if(mainDebug)
      System.out.println("Create rule "+ruleName);
    // Create rule node
    Rule ruleNode = new Rule(ruleName, eventHandle, condMethod, actionMethod ,targetInstance);

    ruleNamesRuleNodes.put(ruleName, ruleNode);
    if(evntReqstDebug)
      System.out.println("Subscribe this rule "+ruleName+" to "+eventHandle.getEventName());

    addRule(eventHandle,ruleNode);
    ruleNode.setCondName(condName);
    ruleNode.setActionName(actionName);
    ruleNode.setRuleScheduler(ruleScheduler);
    return ruleNode;
  }

    /** This method creates an class level rule.
    * When the event occurs, this target rule instance will invoke
    * a declared condition and action.
    *
    * This method takes the user given priority for the Rule.
    */

   // create rule that has no event instance (instance level) but has rule instance

  public Rule createRule( Object targetInstance, String ruleName, EventHandle eventHandle,
				                  String condName, String actionName,int priority) {

    if (ruleNamesRuleNodes.containsKey(ruleName)) {
      System.out.println("Fatal ERROR : Rule with name " + ruleName +
         "already registered");
      return null;
    }
    if(ruleSchedulerDebug){
      System.out.println("Create this " +ruleName+" rule associcated with this "+eventHandle.getEventName()+" event.");
    }
    Method condMethod = getMethod(condName);
    Method actionMethod = getMethod(actionName);
    Rule ruleNode = new Rule(ruleName, eventHandle, condMethod, actionMethod, priority,targetInstance);
    ruleNamesRuleNodes.put(ruleName, ruleNode);
    addRule(eventHandle, ruleNode);
    ruleNode.setCondName(condName);
    ruleNode.setActionName(actionName);
    ruleNode.setRuleScheduler(ruleScheduler);
    return ruleNode;
  }

  /** This method creates an class level rule.
    * When the event occurs, this target rule instance will invoke
    * a declared condition and action.
    *
    * This method takes the user given Coupling mode for the Rule. The symbolic
    * names for the different coupling modes are CouplingMode.IMMEDIATE,
    * CouplingMode.DEFERRED and CouplingMode.DETACHED.
    *
    */

   // create rule that has no event instance (instance level) but has rule instance

   public Rule createRule(Object targetInstance, String ruleName, EventHandle eventHandle,
				                  String condName, String actionName,
                          int priority, CouplingMode coupling) {

    if (ruleNamesRuleNodes.containsKey(ruleName)) {
      System.out.println("Fatal ERROR : Rule with name " + ruleName +
         "already registered");
      return null;
    }

    if(ruleSchedulerDebug){
      System.out.println("Create this " +ruleName+" rule associcated with this "+eventHandle.getEventName()+" event.");
    }
    Method condMethod = getMethod(condName);
    Method actionMethod = getMethod(actionName);
    Rule ruleNode = new Rule(ruleName, eventHandle, condMethod, actionMethod, priority, coupling,targetInstance);
    ruleNamesRuleNodes.put(ruleName, ruleNode);
    addRule(eventHandle, ruleNode);       // add the rule node to associate event
    ruleNode.setCondName(condName);
    ruleNode.setActionName(actionName);
    ruleNode.setRuleScheduler(ruleScheduler);
    return ruleNode;
  }


  /** This method creates an class level rule.
    * When the event occurs, this target rule instance will invoke
    * a declared condition and action.
    *
    * This method takes the user given context for the rule.
    * The symbolic names for the different contexts are ParamContext.RECENT, ParamContext.CHRONICLE,
    * ParamContext.CONTINUOUS and ParamContext.CUMULATIVE.
    */

  // create rule that has no event instance (instance level) but has rule instance

  public Rule createRule( Object targetInstance, String ruleName, EventHandle eventHandle,
				                  String condName, String actionName,
                          int priority, CouplingMode coupling, ParamContext context){

    if (ruleNamesRuleNodes.containsKey(ruleName)) {
      System.out.println("Fatal ERROR : Rule with name " + ruleName + "already registered");
      return null;
    }
    if(ruleSchedulerDebug){
      System.out.println("Create this " +ruleName+" rule associcated with this "+eventHandle.getEventName()+" event.");
    }
    Method condMethod = getMethod(condName);
    Method actionMethod = getMethod(actionName);
    Rule ruleNode = new Rule(ruleName, eventHandle, condMethod, actionMethod, priority, coupling, context,targetInstance );
    ruleNamesRuleNodes.put(ruleName, ruleNode);
    addRule(eventHandle, ruleNode);
    ruleNode.setCondName(condName);
    ruleNode.setActionName(actionName);
    ruleNode.setRuleScheduler(ruleScheduler);
    return ruleNode;
  }

   /** This method creates an class level rule.
    * When the event occurs, this target rule instance will invoke
    * a declared condition and action.
    *
    * This method takes the user given Triggering mode for the Rule. The symbolic
    *  names for the different trigger modes are TriggerMode.NOW and TriggerMode.PREVIOUS.
    *
    */

     // create rule that has no event instance (instance level) but has rule instance

   public Rule createRule(Object targetInstance,String ruleName, EventHandle eventHandle,
				 String condName, String actionName,
				 int priority, CouplingMode coupling,
				 ParamContext context, TriggerMode triggerMode) {

    if (ruleNamesRuleNodes.containsKey(ruleName)) {
      System.out.println("Fatal ERROR : Rule with name " + ruleName +
	       "already registered");
      return null;
    }
    if(ruleSchedulerDebug){
      System.out.println("Create this " +ruleName+" rule associcated with this "+eventHandle.getEventName()+" event.");
    }
    Method condMethod = getMethod(condName);
    Method actionMethod = getMethod(actionName);
    Rule ruleNode = new Rule(ruleName, eventHandle, condMethod, actionMethod, priority, coupling, context, triggerMode,targetInstance);
    ruleNamesRuleNodes.put(ruleName, ruleNode);
    addRule(eventHandle, ruleNode);
    ruleNode.setCondName(condName);
    ruleNode.setActionName(actionName);
    ruleNode.setRuleScheduler(ruleScheduler);
    return ruleNode;
  }




  // added by seyang
  /** This method replace the condition and action code of rule.
   *  @param ruleNode     Rule Object
   *  @param Condition    Condition Function Object
   *  @param Action       Action Function Object
   */
  public Rule replaceRule(Rule ruleNode,Condition condition,	Action action) {
    ruleNode.setCondition(condition);
    ruleNode.setAction(action);
    return ruleNode;
  }
  // added by seyang

  /** This method is used to disable  a rule given its name.
   *
   *  @param ruleName		The name of the rule to be disabled
   */
  public void disableRule(String ruleName) {
    Rule ruleNode = (Rule) ruleNamesRuleNodes.get(ruleName);
    Event event = ruleNode.getEventNode();
    if (event instanceof Primitive) {
      Primitive primEvent = (Primitive) event;
      primEvent.disableRule(ruleNode);
    }
    else if (event instanceof Composite) {
      Composite compEvent = (Composite) event;
      compEvent.disableRule(ruleNode);
    }
  }

   /** This method is used to enable a disabled rule.
   *
   *  @param ruleName		The name of the rule to be enabled
   */
  public void enableRule(String ruleName) {
    Rule ruleNode = (Rule) ruleNamesRuleNodes.get(ruleName);
    Event event = ruleNode.getEventNode();
    if (event instanceof Primitive) {
      Primitive primEvent = (Primitive) event;
      primEvent.enableRule(ruleNode);
    }
    else if (event instanceof Composite) {
      Composite compEvent = (Composite) event;
      compEvent.enableRule(ruleNode);
    }
  }

   /** This method is used to delete  a rule given its name.
   *
   *  @param ruleName		The name of the rule to be deleted
   */
  public void deleteRule(String ruleName) {
    Rule ruleNode = (Rule) ruleNamesRuleNodes.get(ruleName);
    Event event = ruleNode.getEventNode();
    if (event instanceof Primitive) {
      Primitive primEvent = (Primitive) event;
      primEvent.deleteRule(ruleNode);
    }
    else if (event instanceof Composite) {
      Composite compEvent = (Composite) event;
      compEvent.deleteRule(ruleNode);
    }
  }

  private void addRule(EventHandle eventHandle, Rule ruleNode) {
    Event event = eventHandle.getEventNode();
      // added on 24 Dec 99 by seyang
    if (event instanceof FilterEvent) {
      FilterEvent fe = (FilterEvent) event;
      event = fe.getEventNode();
    }
    // added on 24 Dec 99 by seyang
    if (event instanceof Primitive) {
      Primitive primEvent = (Primitive) event;
      primEvent.addRule(ruleNode);
    }
    //<GED>
    else     if (event instanceof RemoteEvent) {
      RemoteEvent remEvt = (RemoteEvent) event;
      remEvt.addRule(ruleNode);
    }
    //</GED>
    else if (event instanceof Composite) {
      Composite compEvent = (Composite) event;
      compEvent.addRule(ruleNode);
    }
  }




   /**  This method raises an event at the beginning of a method.
     *
     *  @param eventHandle	The reference to the event handle associated
     *						with the primitive event.
     *
     *  @param instance		The reference of the instance raising
     *						the event.
     */

  public static void raiseBeginEvent(EventHandle[] eventHandleArray,Object instance) {

    if(eventDetectionDebug){
      System.out.println("raiseBeginEvent");
      System.out.println("There are "+eventHandleArray.length+" event handle elements");
    }
    ECAAgent ecaAgent;
    Thread currThread = Thread.currentThread();
    if (currThread instanceof RuleThread) {
      Rule rule = (Rule) ((RuleThread)currThread).getRule();
      //if (rule.getDetectFlag() == false)
	//return;
    }
    for (int i=0; i<eventHandleArray.length; i++) {
      ecaAgent = (ECAAgent) eventHandlesEcaAgentInstances.get(eventHandleArray[i]);
      raiseBeginEvent(ecaAgent,eventHandleArray[i],instance);
    }
  }

  public void raiseBeginEvent(EventHandle eventHandle,Object instance) {
    if(eventDetectionDebug){
      System.out.println("raiseBeginEvent");
    }

    PrimitiveEventHandle primEventHandle = (PrimitiveEventHandle) eventHandle;
    // if(primEventHandle.isFilterEvent() &&
    //!primEventHandle.check()) return;
    if((instance instanceof Reactive) &
  	  primEventHandle.isFilterEvent() &&
	    (!primEventHandle.check((Reactive)instance)))
    	return;
    String eventSig = primEventHandle.getBeginSignature();
    ParameterList eventHandleParamList = primEventHandle.getParamList();
    ParameterList paramList = new ParameterList(eventHandleParamList.getHashtable(),
		                        			eventHandleParamList.getMethodSignature());
    notifyPrimitiveNode(this, eventSig, paramList, instance);
  }

  public void raiseEndEvent(EventHandle eventHandle,Object instance) {
    PrimitiveEventHandle primEventHandle = (PrimitiveEventHandle) eventHandle;
    //if(primEventHandle.isFilterEvent() &&
	  //!primEventHandle.check()) return;
    if((instance instanceof Reactive) &
    	 primEventHandle.isFilterEvent() &&
       (!primEventHandle.check((Reactive)instance)))
       return;
    String eventSig = primEventHandle.getEndSignature();
    ParameterList eventHandleParamList = primEventHandle.getParamList();

    ParameterList paramList = new ParameterList(eventHandleParamList.getHashtable()
      ,eventHandleParamList.getMethodSignature());
    notifyPrimitiveNode(this, eventSig, paramList, instance);
  }

  private static void raiseBeginEvent(ECAAgent ecaAgent, EventHandle eventHandle,
			              Object instance) {
    PrimitiveEventHandle primEventHandle = (PrimitiveEventHandle) eventHandle;
    // added by seyang on 9 Aug 99
    if((instance instanceof Reactive) &
        primEventHandle.isFilterEvent() &&
    	(!primEventHandle.check((Reactive)instance)))
      return;

    // added by seyang on 9 Aug 99

    // added by seyang on 11 Aug 99
    //if(primEventHandle.isFilterEvent() &&
    //!primEventHandle.check()) return;
    // added by seyang on 11 Aug 99

    String eventSig = primEventHandle.getBeginSignature();
    ParameterList eventHandleParamList = primEventHandle.getParamList();
    ParameterList paramList = new ParameterList(eventHandleParamList.getHashtable(),
		eventHandleParamList.getMethodSignature());
    notifyPrimitiveNode(ecaAgent, eventSig, paramList, instance);
  }

  /**  This method raises an event at the end of a method.
    *
    *  @param eventHandle      The reference to the event handle associated
    *                          with the primitive event.
    *
    *  @param instance         The reference of the instance raising
    *                          the event.
    */
  public static void raiseEndEvent(EventHandle[] eventHandleArray,Object instance) {
    if(eventDetectionDebug)
      System.out.println("Raise End Event");

    ECAAgent ecaAgent;
    Thread currThread = Thread.currentThread();
    if (currThread instanceof RuleThread) {

      Rule rule = (Rule) ((RuleThread)currThread).getRule();

//      if (rule.getDetectFlag() == false)
  //      return;

    }


    for (int i=0; i<eventHandleArray.length; i++) {

      //System.out.println("Show the event name of raised event "+    eventHandleArray[i].getEventName());
      ecaAgent = (ECAAgent) eventHandlesEcaAgentInstances.get(eventHandleArray[i]);
      if(eventDetectionDebug)
        System.out.println(    eventHandleArray[i].getEventName());
      raiseEndEvent(ecaAgent,eventHandleArray[i],instance);
    }
  }

  private static void raiseEndEvent(ECAAgent ecaAgent, EventHandle eventHandle,
			            Object instance) {
    if(eventDetectionDebug)
      System.out.println("Raise End Event ...");

    PrimitiveEventHandle primEventHandle = (PrimitiveEventHandle) eventHandle;

    // added by seyang on 9 Aug 99
    if((instance instanceof Reactive) & primEventHandle.isFilterEvent() &&
	  (!primEventHandle.check((Reactive)instance)))
      return;
      // added by seyang on 9 Aug 99

	    // added by seyang on 11 Aug 99
      //if(primEventHandle.isFilterEvent() &&
	    //!primEventHandle.check()) return;
      // added by seyang on 11 Aug 99

    String eventSig = primEventHandle.getEndSignature();
    ParameterList eventHandleParamList = primEventHandle.getParamList();
    ParameterList paramList = new ParameterList(eventHandleParamList.getHashtable(),
						                             eventHandleParamList.getMethodSignature());
    notifyPrimitiveNode(ecaAgent, eventSig, paramList, instance);
  }


  static void notifyPrimitiveNode(ECAAgent ecaAgent, String eventSig,
					  ParameterList paramList,Object instance) {
    if(eventDetectionDebug)
      System.out.println("ECAAgent::notifyPrimitiveNode");

    paramList.setTS();
    // set the event instance in the parameter list
    if(eventDetectionDebug && instance!= null)
      System.out.println("Put this instance "+ instance.getClass().getName () + " in parameter list");
    paramList.setEventInstance(instance);
    if(eventDetectionDebug)
      System.out.println("done w/ setting instance in paramList");

    Hashtable eventSignaturesEventNodes = ecaAgent.getEventSignaturesEventNodes();
    RuleScheduler ruleScheduler = ecaAgent.getRuleScheduler();


    // added by wtanpisu
    ProcessRuleList processRuleList = ruleScheduler.getProcessRuleList();
    // added on 03/31/2000

    Vector eventNodeVector = (Vector) eventSignaturesEventNodes.get(eventSig);
    if (eventNodeVector == null) {
      System.out.println("notifyPrimitiveNode: Event with method " +
				"signature " + eventSig + " not defined");
      return;
    }

    NotifyBuffer notifyBuffer = ecaAgent.getNotifyBuffer();
    Primitive primitiveEvent = null;

    TimeStamp tsObj = paramList.getTS();
    if (eventDetectionDebug)
      //	  System.out.println("RAISING EVENT " + eventSig + " " + tsObj.getSequence());
      System.out.println("RAISING EVENT " + eventSig + " " + tsObj.getGlobalTick());
      if (eventDetectionDebug) {
	System.out.println("eventNodeVector.size() = " + eventNodeVector.size());
      }

      NotifyObject notifyObj = null;
      Thread currThread = Thread.currentThread();

      // Search through the evntNodeVector for the current event signature
      // to notify both class level event nodes and instance level event nodes
      // that have the same event signature.
      // It is possible that two primitive event nodes (one class level and
      // another instance level) may contain the same event signature.

      for (int i=0; i<eventNodeVector.size(); i++) {
        if (eventDetectionDebug)
          System.out.println(i);
	  // modified on 23 Dec 99
          // commented by seyang -> primitiveEvent = (Primitive) eventNodeVector.elementAt(i);
	Object aEvent = eventNodeVector.elementAt(i);
	if(aEvent instanceof FilterEvent) {
	  if (eventDetectionDebug)
            System.out.println("it's filter event");
          FilterEvent fevent = (FilterEvent)aEvent;
	  if(!fevent.check((Reactive) instance)) continue;
	  else primitiveEvent = (Primitive)fevent.getEventNode();
        }
        else
          primitiveEvent = (Primitive)aEvent;

        // modified on 23 Dec 99
        // this event is not defined
        if (primitiveEvent == null) {
          System.out.println("Error : Event with name " + eventSig +
                                  "has not been registered yet");
        return;
      }

      // this is a class level event if objectInstance is null
      // otherwise, this is an instance level event.

      if (primitiveEvent.isClassLevelEvent()) {
	if (eventDetectionDebug)
          System.out.println(i+"Notifying CLASS level event " +
        primitiveEvent.eventName + "...");

        notifyObj = new NotifyObject(primitiveEvent,paramList,currThread,ruleScheduler);

        // modified by wtanpisu


        if(!Constant.RSCHEDULERFLAG){
          notifyBuffer.put(notifyObj);
        }
        else{

        if (notifyBuffer != null) {
          putIntoNotifyBuffer(notifyObj,ecaAgent,currThread,processRuleList);
        }else
        	System.out.println("SERIOUS Error! ECAAgent not initialized in the beginning of application");
        }
      }
      // modified on 03/31/2000

      // if this event is an instance level event,
      // check if the raising instance and the instance
      // defined in the event are the same.

      else if (primitiveEvent.isInstanceLevelEvent(instance)) {

        // notify will report the occurence of this event
        // to the corresponding primitive event node in the
        // event graph.
	if (eventDetectionDebug)
          System.out.println("Notifying INSTANCE level event " +
                       primitiveEvent.eventName + "...");
        notifyObj = new NotifyObject(primitiveEvent,paramList,currThread,ruleScheduler);


        if(!Constant.RSCHEDULERFLAG){
          notifyBuffer.put(notifyObj);
        }
        else{
	    // modified by wtanpisu
        if (notifyBuffer != null) {
    	    putIntoNotifyBuffer(notifyObj,ecaAgent,currThread,processRuleList);
        }else
          System.out.println("SERIOUS Error! ECAAgent not initialized in the beginning of application");
        }
      }
      // modified on 03/31/2000
    }

    // <GED>
    // added by weera on 12/14/2000
    // notify to the GED

    // If this primitive event is subscribed by GED, notify GED the occurence of this event
    if(primitiveEvent.getForwardFlag()){

      String evntNm = primitiveEvent.getEventName();
      if(glbNotifDebug){
        System.out.println("Forward Flag is true" );
        System.out.println("Notify the GED about the occurence of this primitive event : "+evntNm);
      }

      /* packing up the paramter list into pc table before sending to server */
      if(glbNotifDebug){
        System.out.println("Packing the parameter list into a PCTable");
        System.out.println("This table will be sent out to Server");
      }

      EventSet evntSet = new EventSet();
      evntSet.addElement(paramList);
      if(paramList == null){
        System.out.println("parmalist is null");
      }
      if(evntSet.getSize() == 1){
        System.out.println("parmalists list size is 1");
      }
      TimeStamp ts = paramList.getTS();
      evntSet.setTS(ts) ;
      PCEntry entry = new PCEntry(evntSet);
      byte context = (byte) ContextBit.RECENT + ContextBit.CHRONICLE +
      ContextBit.CONTINUOUS + ContextBit.CUMULATIVE;
      entry.setContext(context);

      PCTable pcTable = new PCTable();
      pcTable.setForPrimitive(true);
      Vector pcEntries = pcTable.getPCEntries();
      pcEntries.addElement(entry);

      NotificationMessage notifMesg = new NotificationMessage(evntNm,Constant.APP_NAME,Constant.APP_URL,pcTable);

      String des = Constant.GED_URL + Constant.GED_NAME;
      ledIntf.send(des,notifMesg);
    }
    // end of added
    //</GED>
  }



  // added by wtanpisu
  /** This method is used to take care of waiting immediated child mechanism. It'll put the notify Object
   *  into the buffer and wait for the completion of its child
   */

  protected static void putIntoNotifyBuffer(NotifyObject notifyObj,ECAAgent ecaAgent,
                      Thread currThread,ProcessRuleList processRuleList
                      ){
    NotifyBuffer notifyBuffer = ecaAgent.getNotifyBuffer();
    LEDThread ledThread = ecaAgent.getLEDThread();
    // If the it 's top level transaction
    if (currThread == applThread  ) {

      // If dectection mode is Sync.,
      // waits until associated rule is activated.
      // Then, start waiting for its immediate child
      if (notifyObj.waitFlag == true){

      // wait for inserting all the associated rules in the queue
      // before start waiting for immediate child

      synchronized (notifyObj){
	try{
          notifyBuffer.put(notifyObj);
	  notifyObj.wait();
	}
	catch(InterruptedException e){}
      }

      if (eventDetectionDebug)
	System.out.println(Thread.currentThread().getName()+" waits for its imm child");

	// waiting for the childs
	processRuleList.waitForImmRules();
        if (eventDetectionDebug)
          System.out.println(Thread.currentThread().getName()+"'s imm child is finished");
      }

      // If it's parallel mode, put the notify object into the buffer and continue working
      // This is for the future work.
      else{
	notifyBuffer.put(notifyObj);
      }
    }
    else { // sub transaction
      if( currThread instanceof RuleThread)  {
	RuleThread ruleThread = (RuleThread) currThread;
        if (eventDetectionDebug)
	  System.out.println("Yielding rule thread " + ruleThread.getName() + " from notifyPrimitiveNode");
        }
        if (notifyObj.waitFlag == true){
	  synchronized (notifyObj){
          try{
            notifyBuffer.put(notifyObj);
            notifyObj.wait();
          }
          catch(InterruptedException e){}
        }
        if (eventDetectionDebug)
          System.out.println(Thread.currentThread().getName()+" waits for its imm child");
        processRuleList.waitForImmRules();
      }
    }
  }
	// added on 03/31/2000



   /** This method inserts an integer parameter into the parameter list of
    *  the event handle.
    *
    *  @param varName		Name of the integer variable
    *
    *  @param intValue		Value of the integer variable
    */
   public static void insert(EventHandle[] eventHandleArray, String varName, int intValue) {
    PrimitiveEventHandle primEventHandle;
    for (int i=0; i< eventHandleArray.length; i++) {
      ((PrimitiveEventHandle)eventHandleArray[i]).insert(varName, intValue);
    }
   }

   /** This method inserts a float parameter into the parameter list of
    *  the event handle.
    *
    *  @param varName			Name of the float variable
    *
    *  @param floatValue		Value of the float variable
    */
   public static void insert(EventHandle[] eventHandleArray, String varName, float floatValue) {
     for (int i=0; i< eventHandleArray.length; i++)
      ((PrimitiveEventHandle)eventHandleArray[i]).insert(varName,floatValue);
   }

   /** This method inserts a byte parameter into the parameter list of
    *  the event handle.
    *
    *  @param varName		Name of the byte variable
    *
    *  @param byteValue		Value of the byte variable
    */
   public static void insert(EventHandle[] eventHandleArray, String varName, byte byteValue) {
     for (int i=0; i< eventHandleArray.length; i++)
      ((PrimitiveEventHandle)eventHandleArray[i]).insert(varName,byteValue);
   }

   /** This method inserts a short parameter into the parameter list of
    *  the event handle.
    *
    *  @param varName			Name of the short variable
    *
    *  @param shortValue		Value of the short variable
    */
   public static void insert(EventHandle[] eventHandleArray, String varName, short shortValue) {
     for (int i=0; i< eventHandleArray.length; i++)
      ((PrimitiveEventHandle)eventHandleArray[i]).insert(varName,shortValue);
   }

   /** This method inserts a long parameter into the parameter list of
    *  the event handle.
    *
    *  @param varName		Name of the long variable
    *
    *  @param longValue		Value of the long variable
    */
   public static void insert(EventHandle[] eventHandleArray, String varName, long longValue) {
    for (int i=0; i< eventHandleArray.length; i++)
      ((PrimitiveEventHandle)eventHandleArray[i]).insert(varName,longValue);
   }

  /** This method inserts a double parameter into the parameter list of
    *  the event handle.
    *
    *  @param varName			Name of the double variable
    *
    *  @param doubleValue		Value of the double variable
    */
   public static void insert(EventHandle[] eventHandleArray, String varName, double doubleValue) {
    for (int i=0; i< eventHandleArray.length; i++)
      ((PrimitiveEventHandle)eventHandleArray[i]).insert(varName,doubleValue);
   }

   /** This method inserts a char parameter into the parameter list of
    *  the event handle.
    *
    *  @param varName		Name of the char variable
    *
    *  @param charValue		Value of the char variable
    */
   public static void insert(EventHandle[] eventHandleArray, String varName, char charValue) {
    for (int i=0; i< eventHandleArray.length; i++)
      ((PrimitiveEventHandle)eventHandleArray[i]).insert(varName,charValue);
   }

   /** This method inserts a boolean parameter into the parameter list of
    *  the event handle.
    *
    *  @param varName			Name of the boolean variable
    *
    *  @param boolValue			Value of the boolean variable
    */
   public static void insert(EventHandle[] eventHandleArray, String varName, boolean boolValue) {
    for (int i=0; i< eventHandleArray.length; i++)
      ((PrimitiveEventHandle)eventHandleArray[i]).insert(varName,boolValue);
   }

   /** This method inserts an Object parameter into the parameter list of
    *  the event handle.
    *
    *  @param varName		Name of the Object variable
    *
    *  @param object		Value of the Object variable
    */
   public static void insert(EventHandle[] eventHandleArray, String varName, Object object) {

    for (int i=0; i< eventHandleArray.length; i++)
      ((PrimitiveEventHandle)eventHandleArray[i]).insert(varName,object);

   }


  /**
   *  This method is used to execute the deferred rules.
   */

  // added by wtanpisu
  public void processDefRules(){
    EventHandle[] commitTransaction = getEventHandles("commitTransaction");
    this.raiseBeginEvent(commitTransaction,this);
  }

  /**
   * This mothod is a action method when commitEvent is triggered
   */

  public void executeProcessDeffRules(ListOfParameterLists parameterLists){
    ruleScheduler.process_deff_rules();
    if(ruleSchedulerDebug)
      System.out.println("execution condition the commit rule");
  }
  // added on 01/24/2000

}


