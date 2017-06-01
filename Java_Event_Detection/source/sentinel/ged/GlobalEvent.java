
/**
 * Title:        Global Event Detection
 * Description:  Your description
 * Copyright:    Copyright (c) 1999
 * Company:      ITLAB, Unversity of Texas, Arlington
 * @author       Weera Tanpisuth
 * @version
 */

package sentinel.ged;

import sentinel.comm.*;
import java.util.Vector;
import sentinel.led.*;

/**
 * The global class is used to construct global event nodes on the server.
 */

public class GlobalEvent extends Event{

  static boolean glbEvntReqstDebug = DebuggingHelper.isDebugFlagTrue("glbEvntReqstDebug");
  static boolean glbEvntNotifDebug = DebuggingHelper.isDebugFlagTrue("glbEvntNotifDebug");



  /**
   * Indicate whether the detector needs to propagate the occurence of event
   * to the consumer or not.
   */
  private boolean sendBackFlag = false;

  /**
   * The parameter and contexts table of the global event
   */
  private PCTable gedPCTable = null;

  /**
   * The event name on the producer site.
   */
  private String prodEvntNm;

  /**
   * The producer's application name of this event
   */
  private String prodAppNm;

  /**
   * The location from where this event originated
   */
  private String prodMachNm;

  /** Default constructor */
  public GlobalEvent() {}

  /**
   * Constructor
   */
  GlobalEvent(String eventName,String prodEvntNm,String prodAppNm,String prodMachNm){
    super(eventName);
    this.prodAppNm = prodAppNm;
    this.prodEvntNm = prodEvntNm;
    this.prodMachNm = prodMachNm;
  }


  /**
   * returns the PCTable of this event
   */

  // This method is not used in global event detection.
  // It is used for debugging purpose
  protected Table getTable(Notifiable event) {
	return gedPCTable;
  }


  /**
   * The method is used to notify the global event. This is called by GECAAgent
   * when it receives the notification from the producer.
   */

  void notify(NotificationMessage notifMesg){

    // Unpacking the pcTable passed from a producer
    gedPCTable = notifMesg.getPCTable();
    Vector pcEntries = gedPCTable.getPCEntries();

    // Propagate the parameter lists to the parents
    Vector parentTables = getParentTables();

    Table table =null;

    if (glbEvntNotifDebug)
      System.out.println("\nEvent.java:parentTables.size = " + parentTables.size());

      for(int i = 0; i < parentTables.size(); i++) {
	table = (Table) parentTables.elementAt(i);

      if (glbEvntNotifDebug)
        System.out.println("Put the param and context into the parent table");

      ((PCTable) table).getPropagation(pcEntries); // in pcTable
      table.print();
    }

    // Propagate the occurence of this event to the parent node, if any
    propagateEvent();
  }


  /**
   * Prapagate the occrence of the event to the parent node.
   */
  void propagateEvent(){
    Notifiable event = null;

    // Check at the subscriber list
    for (int i = 0; i < subscribedEvents.size(); i++) {
      event = (Notifiable) subscribedEvents.elementAt(i);
      Composite a = (Composite) event;
      a.notify(this,null,null);
    }
  }


  /**
   * reset the context recursively
   */

  // Note this method is not used in GED at this time
  public void resetContextRecursive(int context) {
    this.resetContextCurrent(context);
  }


  /**
   * Set the context bit of this global event only, since the global node
   * is the leaf node.
   */
  public void setContextRecursive(int context) {

    if(glbEvntReqstDebug){
      System.out.println("Increase "+ParamContext.getContext(context)+" context counter of this event ::\n"+eventName);
    }

    this.setContextCurrent(context);


    propagateContextToProducer(context);
  }


  /**
   * This method is to increment the context counter for the given context at
   * the event node on the producer site. After the context counter is
   * increased or set for the first time, the Global Event Detector will
   * send the request to the producer to set the the forward flag of corresponding
   * event on the producer site. This method is called when there is a detection
   * request from a consumer.
   */

  void propagateContextToProducer(int context){

    boolean propagate = false;
    DetectionRequestMessage detectnReqstMesg = null;
    GEDInterface gedIntf = GECAAgent.getGEDInterface();

//    if(glbEvntReqstDebug)
//      System.out.println("This new rule has the "+ ParamContext.getContext(context)+"  context");

    if(context == ParamContext.RECENT.getId()){
//      if(glbEvntReqstDebug)
//        System.out.println("Recent counter = "+ recentCounter);

      // When recent counter is 1, detector starts detecting this event
      // in the recent context mode.
      if(recentCounter == 1){
        propagate = true;
      }
    }else if(context == ParamContext.CONTINUOUS.getId()){
      if(contiCounter == 1){
        propagate = true;
      }
    }else if(context == ParamContext.CHRONICLE.getId()){
      if(chronCounter == 1){
        propagate = true;
      }
    }else if(context == ParamContext.CUMULATIVE.getId()){
      if(cumulCounter == 1){
        propagate = true;
      }
    }

    if(propagate == true){

      // Pack the pack message telling the producer to forward the occurence
      // of this event, if this event has occured
      DetectionRequestMessage dectReqstMesg = new DetectionRequestMessage(prodEvntNm, prodAppNm, prodMachNm,context);

      if(glbEvntReqstDebug){
        System.out.println("Propagate the context of the rule to producer");
        System.out.println("This signal will set recent context counter on this event node "+dectReqstMesg.getEvntNm() +" on the producer site");
      }

      String producerID = prodAppNm+"_"+prodMachNm ; // appNm_machNm

      if(glbEvntReqstDebug)
        System.out.println("Send detection request to this site "+producerID);

      // Update the producer list.
      gedIntf.addDetcReqstToProdList(producerID,dectReqstMesg);

      // check whether the producer is registered or not. If the producer already
      // registered to the GED, then send the detection request to the producer
      // Otherwise, keep the record into the log
      if(gedIntf.isRegistered(producerID)){
      //  detectnReqstMesg = new DetectionRequestMessage(prodEvntNm,prodAppNm,prodMachNm,context);
        gedIntf.send(producerID,dectReqstMesg);
      }
    }
  }

  public void notify(Notifiable event, Thread parent,Scheduler ruleScheduler) {
    // Primitive event does not get notified from another event
    return;
  }
}