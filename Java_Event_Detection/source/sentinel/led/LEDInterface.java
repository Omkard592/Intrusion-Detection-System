/**
 * Title:        Local Event Detection
 * Description:
 * Copyright:    Copyright (c) 1999
 * Company:      ITLAB, Unversity of Texas, Arlington
 * @author       Weera Tanpisuth
 * @version
 */

package sentinel.led;
import sentinel.comm.*;

import java.rmi.*;
import java.rmi.server.*;
import java.net.*;
import java.util.*;

/* This two classes are the interface */
import sentinel.comm.*;

import java.io.*;

/**
 * The LEDInterface class is a communication module in the Local Event Detection (LED).
 *
 */

public class LEDInterface implements SentinelComm,Serializable{

  static boolean evntCreatnDebug = DebuggingHelper.isDebugFlagTrue("evntCreatnDebug");
  static boolean evntNotifDebug = DebuggingHelper.isDebugFlagTrue("evntNotifDebug");
  static boolean evntReqstDebug = DebuggingHelper.isDebugFlagTrue("evntReqstDebug");


  static boolean GEDDebug = DebuggingHelper.isDebugFlagTrue("GEDDebug");
  static boolean globalRuleCreationDebug = DebuggingHelper.isDebugFlagTrue("globalRuleCreationDebug");
  static boolean showMethodDebug = DebuggingHelper.isDebugFlagTrue("showMethodDebug");


  public void send(SentinelMessage mesg){};

  /* A list of the remote event names */
  //private Vector listOfGlbEvntNm = new Vector();


   /**
   * This Hashtable stores the mapping from the global event names to the ECAAgents
   * When the LEDInterface receives the notification from the GED, LEDInterface
   * will notify to all the ECAAgents that subscribed to this event.
   */
  private Hashtable glbEvntNm_ECAAgentList = new Hashtable();


  /**
   * Remote object on the server is used to make RMI call to create the
   * global event on the server.
   */
  private GlobalEventFactory glbEvntFact;

  /**
   * LED Message Receiver is used to receive all the incoming messages, including
   * detection request message and notification message from the global event detector.
   */
  private LEDMesgRecvImp senMesgRec =null;

  /**
   * Constructor
   */
  public LEDInterface() {

    // Binding the led message receiver into the registry
    try {
      senMesgRec     = new  LEDMesgRecvImp(this);
      System.out.println("Binding the client reference to registry.");
      Naming.rebind(Constant.APP_ID ,senMesgRec);
      System.out.println("Bind the message receiver of "+Constant.APP_ID+" into registry");
    }catch (Exception e){
      System.err.println("Server exception: " +  e.getMessage());
      e.printStackTrace();
    }

    System.setSecurityManager(new RMISecurityManager());

    // Register to the GED server
    String url = Constant.GED_URL;
    ServerConnector con = null;
    try{
      glbEvntFact = (GlobalEventFactory)Naming.lookup(url+Constant.GED_NAME+"_GLOBAL_FACTORY" );

      if(GEDDebug){
        System.out.println("Registering to GED");
        System.out.println("Application ID :: "+Constant.APP_ID);
      }

      // looking up for the connector in the registry
      con = (ServerConnector)Naming.lookup(url+Constant.GED_NAME+"_CONNECTOR");


      // register to the server
      con.register(Constant.APP_ID);

    }
    catch(StubNotFoundException se){
      se.getMessage ();
      se.printStackTrace();
    }catch(RemoteException re){
      re.getMessage ();
      re.printStackTrace();
    }catch(NotBoundException nbe){
      nbe.getMessage ();
      nbe.printStackTrace();
    }catch(MalformedURLException  me){
      me.getMessage ();
      me.printStackTrace ();
    }catch (Exception e)  {
      System.err.println("ERROR:: in LEDInterface " + e.getMessage());
      e.printStackTrace();
      System.exit(-1);
    }
    if(GEDDebug){
      System.out.println("Registeration Complete");
    }

  }


  /**
   * This method is to send the message from the client to server
   * There are two types of messages (DetectionRequest and Notification)
   */
  public void send(String destination, SentinelMessage mesg){
    try{
      SentinelMesgRecv rec = (SentinelMesgRecv)Naming.lookup(destination);
      rec.onMessage(mesg);
    }catch(Exception e){
      e.printStackTrace();
    }
  }

  /**
   * This method is called by LEDMesgRecvImp, when it receives detection
   * request from the GED. It unpacks the message to obtain the event name
   * and context in order to set the forward flag.
   */
  public void receiveEvntDetctnReqst(SentinelMessage mesg){

    DetectionRequestMessage dectctnRequstMesg = (DetectionRequestMessage) mesg;

    String eventNm = dectctnRequstMesg.getEvntNm ();
    int context = dectctnRequstMesg.getContext();

    if(evntReqstDebug){
      System.out.println("LEDInterface::receiveEvntDetctnReqst");
      System.out.println("Message Details\n"+dectctnRequstMesg.toString());
      System.out.println("Producer receives the detecion request of this event "+ eventNm);
    }

    ECAAgent.makeGlobal(eventNm,context);
  }

  /**
   * This method is called by LEDMesgRecvImp, when it receives notification
   * from the GED. It unpacks the message to obtain the event name and
   * PCTable and create the notifyOject and put it into the notifyBuffer
   */

  public void receiveNotification(SentinelMessage mesg){

    ECAAgent ecaAgent;
    Hashtable remEvntNm_RemEvntHndle;
    String agentName;
    EventHandle eventHandle;

    if(evntNotifDebug)
      System.out.println("\nLEDInterface::receiveNofication");

    NotificationMessage notifMesg = null;


    if( mesg instanceof NotificationMessage){
      notifMesg = (NotificationMessage)  mesg;
    }else{
      System.err.println("Internal ERROR :: Wrong type of message");
    }

    // Get event name on the producer site
    String prodEvntNm = notifMesg.getProdEvntNm ();

    // Get all the ECA agents
    Enumeration en = ECAAgent.ecaAgentNamesEcaAgentInstances.elements();

    // Searching for the event node from each agent.
    while(en.hasMoreElements()){

      ecaAgent = (ECAAgent) en.nextElement();
      remEvntNm_RemEvntHndle = ecaAgent.getRemNodeManager ().getConsEvntNm_ConsEvntHandlHT();
      Hashtable glbNmConsNm  = ecaAgent.getRemNodeManager ().getGlbEvntNm_ConsEvntNm ();

      // Mapping back the consumer event name (eventName)
      // with the global event name (eventName_appName_machName)
      if(evntNotifDebug)
        System.out.println("This event on the server is notified ::\n"+notifMesg.getProdEvntNm ()+notifMesg.getAppNm ()+notifMesg.getMachNm ());
      String consEvntNm = (String) glbNmConsNm.get(notifMesg.getProdEvntNm ()+notifMesg.getAppNm ()+notifMesg.getMachNm ());

      // Get the event handle
      eventHandle = (EventHandle) remEvntNm_RemEvntHndle.get(consEvntNm);

      if(evntNotifDebug)
        System.out.println("Notify this remote event node :: "+eventHandle.getEventName());

      PCTable pcTable =  notifMesg.getPCTable();

      RuleScheduler ruleScheduler = ecaAgent.getRuleScheduler();
      ProcessRuleList processRuleList = ruleScheduler.getProcessRuleList();
      NotifyBuffer notifyBuffer = ecaAgent.getNotifyBuffer();


      RemoteEvent remEvnt = (RemoteEvent)	eventHandle.getEventNode ();

      Thread currThread = Thread.currentThread();

      // Put the notify object into the notify buffer

      Thread.currentThread().setName(Constant.LEDReceiverThreadName);


      NotifyObject notifyObj = new NotifyObject(remEvnt,pcTable,Thread.currentThread(),ruleScheduler);


      // Unlike the application thread, the thread that notifies the remote node
      // doesn't need to wait for immediate child. It processes the event int
      // PARALLEL detection mode.

      if (notifyBuffer != null) {

        notifyBuffer.put(notifyObj);

      }else{
        System.err.println("Internal ERROR :: Notify Buffer is not created");
      }
    }
  }


  /**
   * The method is called by ECAAgent to make rmi call to create the primitive
   * global event node on the server.
   */

 void consumePrimGlbEvnt(String prodEvntNm, String appNm, String machNm, ECAAgent ecaAgent){

    if(evntCreatnDebug)
      System.out.println("\nLEDInterface::consumePrimGlbEvnt");

    try{

      // event already consumed by another eca agent located on the same site.

      StringBuffer glbEvntNm = new StringBuffer(prodEvntNm+appNm+machNm);

      // Check whether this glbEvnt is already subscribed by this site.
      // If remoteEventNm contains this event name, this event is subcribed
      // by this site. Since many Local Event Detectors may subcribe to the same event.

        if(glbEvntNm_ECAAgentList.containsKey(glbEvntNm)){
//      if(listOfGlbEvntNm.contains (glbEvntNm)){
        // Put the local eca agent into the hash table.
        Vector agntList = (Vector) glbEvntNm_ECAAgentList.get(glbEvntNm);
        agntList.add (ecaAgent);
      }else{
        glbEvntFact.createPrimGlobalEvent(prodEvntNm,appNm,machNm,ECAAgent.getAppID());

        if(evntCreatnDebug)
          System.out.println("Creation of the global event on the server is complete");

        Vector agntList = new Vector();
        agntList.add (ecaAgent);
        glbEvntNm_ECAAgentList.put(glbEvntNm,agntList);
      }
    } catch (Exception e){
      System.err.println("ERROR!!!");
      System.err.println("Can't send the request to consume global event");
      e.printStackTrace();
    }
  }

  /**
   * The method is called by ECAAgent to make rmi call to create the composite
   * global event node on the server. This is for AND,OR,SEQ operator.
   */

  void consumeCompGlbEvnt(EventType eventType, String glbEvntName, String lGlbEvntNm,String rGlbEvntNm,ECAAgent ecaAgent){
    if(evntCreatnDebug){
      System.out.println("\n\nLEDInterface::consumeCompGlbEvnt");
      System.out.println("Create the composite global event :: "+eventType.getName());
    }
    try{

      // event already consumed by another ecca agent which is located on the same site.

      /**
       * If the other agent already made the request to the same event, don't need
       * to create another event node in the server.
       */
//      if(listOfGlbEvntNm.contains (glbEvntName)){
      if(glbEvntNm_ECAAgentList.containsKey(glbEvntName)){
        Vector agntList = (Vector) glbEvntNm_ECAAgentList.get(glbEvntName);
        agntList.add (ecaAgent);
      }
      else{
        if(evntCreatnDebug){
          System.out.println("Send the request to ged to create "+eventType.getName()+" event");
        }
        glbEvntFact.createCompGlobalEvent(eventType.getName(),lGlbEvntNm,rGlbEvntNm,ECAAgent.getAppID());
        Vector agntList = new Vector();
        agntList.add (ecaAgent);
        glbEvntNm_ECAAgentList.put(glbEvntName,agntList);
      }
    }
    catch (Exception e){
      System.err.println("ERROR LEDInterface::consumePrimGlbEvnt");
      System.err.println("Can't send the request to consume global event" + e.getMessage());
      e.printStackTrace();
    }
  }

  /**
   * The method is called by ECAAgent to make rmi call to create the composite
   * global event node on the server. This is for NOT operator.
   */
  void consumeCompGlbEvnt(EventType eventType, String glbEvntName, String lGlbEvntNm, String mGlbEvntNm, String rGlbEvntNm, ECAAgent ecaAgent){

    System.out.println("\n\nLEDInterface::consumeCompGlbEvnt");
    System.out.println("Create the comp global event on the server is completed");
    try{

      // event already consumed by another ecca agent which is located on the same site.

      /**
       * If the other agent already made the request to the same event, don't need
       * to create another event node in the server.
       */
//      if(listOfGlbEvntNm.contains (glbEvntName)){
     if(glbEvntNm_ECAAgentList.containsKey(glbEvntName)){
        Vector agntList = (Vector) glbEvntNm_ECAAgentList.get(glbEvntName);
        agntList.add (ecaAgent);
      }
      else{
        System.out.println("Send the request to ged to create "+eventType.getName()+" event");
        glbEvntFact.createCompGlobalEvent(eventType.getName(),lGlbEvntNm, mGlbEvntNm, rGlbEvntNm,ECAAgent.getAppID());
        Vector agntList = new Vector();
        agntList.add (ecaAgent);
        glbEvntNm_ECAAgentList.put(glbEvntName,agntList);
      }
    }
    catch (Exception e){
      System.err.println("ERROR LEDInterface::consumePrimGlbEvnt");
      System.err.println("Can't send the request to consume global event" + e.getMessage());
      e.printStackTrace();
    }
  }
}