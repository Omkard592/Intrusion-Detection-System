
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
import sentinel.led.*;

import java.rmi.*;
import java.rmi.registry.*;
import java.util.*;

/**
 * This GEDInterface class is provided to support a communication between
 * a local event detector and a global event detector. This module is responsible
 * for recieving notification messages and detection request messages from clients
 * and sending (propagating) those messages to the corresponding clients
 */

public class GEDInterface implements SentinelComm {

  static boolean glbEvntCreatnDebug = DebuggingHelper.isDebugFlagTrue("glbEvntCreatnDebug");
  static boolean glbEvntNotifDebug = DebuggingHelper.isDebugFlagTrue("glbEvntNotifDebug");
  static boolean glbEvntReqstDebug = DebuggingHelper.isDebugFlagTrue("glbEvntReqstDebug");
  static boolean gedDebug = DebuggingHelper.isDebugFlagTrue("gedDebug");
  static boolean registerDebug = DebuggingHelper.isDebugFlagTrue("registerDebug");

  /**
   *  A server connector for registration. Clients use this connector to register
   *  themself to GED by calling register method
   */
  private ServerConnectorImp servConnector;

  /**
   * The hashtable maps the producer with the vector of detection request messages.
   * This table contains the list of events consumed by remote sites or distant consumers.
   *
   */
  private Hashtable prod_DectectnReqstHt = new Hashtable();

  /**
   * Ths hastable maps the global event name with the list of clients' identification.
   * This table can be used to look up for the clients when the global events
   * have occured so that the GED can notify to the associate clients.
   */

   //Note
   // global primitive event name :: prodEvntName + appEvntNm + machNm
   // global composite event name (binary operation)  :: evntType + leftGlbEvntNm + rightGlbEvntNm
   // global composite event name (ternary operation) :: evntType + leftGlbEvntNm + midGlbEvntNm + rightGlbEvntNm>

  private Hashtable glbEvntName_consumerList = new Hashtable();

  /**
   * Buffer for buffering the notification messages that will be sent to the
   * corresponding consumers.
   *
   */

   // Note !!! This is a simple version of buffer manager

  private NotifMesgBuffer notifMesgBuffer = new NotifMesgBuffer();

  /**
   * The auxilary thread helps GED to notify the occurences of events to the
   * corresponding consumers.
   */
  private NotificationSenderThread notifThread;

  /**
   * The receiver contains the "onMessage"method that can be called from
   * remote site to send the messages.
   */
  private GEDMesgRecvImp gedMesgRec;

  /**
   * This hashtable maps the client with the host address.
   * e.g. consumer_bangkok , 129.107.12.242
   */
  private Hashtable clntAddrsHt = new Hashtable();

  /**
   * Constructor
   */
  GEDInterface()throws RemoteException {

    notifThread = new NotificationSenderThread(notifMesgBuffer);
    new Thread(notifThread).start();

    try {
      /* Binding connector, client can lookup and register */
      servConnector = new ServerConnectorImp(this);

      if(gedDebug)
        System.out.println("Bind the connetor "+GEDConstant.GED_NAME+"_CONNECTOR"+ " into registry");
      Naming.rebind(GEDConstant.GED_NAME+"_CONNECTOR" , servConnector);

      /* Binding ged message receiver, client can lookup and send messages */
      gedMesgRec     = new  GEDMesgRecvImp(this);
      Naming.rebind(GEDConstant.GED_NAME  ,gedMesgRec);
    }catch (Exception e){
      System.err.println("Server exception: " +  e.getMessage());
      e.printStackTrace();
    }
  }

  /**
   * Keep the client id and its address into the hashtable
   */
  synchronized void addClient(String appID, String clientHost){

    clntAddrsHt.put(appID,clientHost);

    /* add new producer into producer list */
    if(!prod_DectectnReqstHt.containsKey(appID)){
      Vector dectReqstList = new Vector();

      // These reserved slots will be used later
      dectReqstList.addElement (new Integer(0)); // initialize the number of notification request to 0
      dectReqstList.addElement (new Integer(0)); // initialize the number of notification request to 0
      prod_DectectnReqstHt.put (appID,dectReqstList);
    }
  }

  /**
   * Check whether the client is already registered or not
   */
  boolean isRegistered (String clientID){
    return  clntAddrsHt.containsKey(clientID);
  }

  /**
   * This method is called when the new client registers to the GED.
   * It checks whether there is any detection request wating for this client.
   * If any, forward the detection request to the producer.
   */
  synchronized void getDectReqst(String appID){

    if(glbEvntReqstDebug)
      System.out.println("GEDInterface::getDectReqst");

    if(prod_DectectnReqstHt.containsKey(appID)){
      if(glbEvntReqstDebug)
        System.out.println("Receiving the detection request");

      Vector reqstList = (Vector)  prod_DectectnReqstHt.get(appID);


      /* To implement log file, we need to increase the
      * counter at the first element located in the dectReqstMesg vector
      * then write the mesg in the log file
      */

      if(glbEvntReqstDebug)
        System.out.println("Number of message in the list "+reqstList.size ());

      int reqstListSize = reqstList.size ();

      for(int i=2 ; i< reqstListSize ; ++i){

        DetectionRequestMessage dectReqstMesg  =  (DetectionRequestMessage) reqstList.elementAt (i);

        if(glbEvntReqstDebug || registerDebug)
          System.out.println("\nSend the detection request message to producer");
        try{
          send(appID,dectReqstMesg);
          if(glbEvntReqstDebug ||registerDebug)
            System.out.println(dectReqstMesg.toString());
        }catch(Exception e){
          e.printStackTrace();
        }
      }
    }else{
      if(glbEvntReqstDebug)
        System.out.println("There is no the detection request waiting");
    }
  }

  /**
   * This method is used to send the detection request message to the producer.
   */
  public synchronized void send(String clientID,SentinelMessage mesg){


    if(mesg instanceof DetectionRequestMessage){
      try{

        String url = "rmi://"+clntAddrsHt.get(clientID)+"/";

        // Map the old application id to the new application it this application
        // runs on the different machine.
        clientID = GEDUtilities.getNewAppID(clientID);

        if(glbEvntReqstDebug || registerDebug)
          System.out.println("send the message to "+url+clientID);

        SentinelMesgRecv rec = (SentinelMesgRecv)Naming.lookup(url+clientID);
        rec.onMessage(mesg);
      }catch(Exception e){
        e.printStackTrace();
      }
    }else if (mesg instanceof NotificationMessage){
      System.out.println("ERROR !!!! in send()");
      System.exit(0);
    }
  }

  /**
   * This method is used to send the notification messages to the consumers.
   */
  public synchronized void send(SentinelMessage mesg){
    if(glbEvntNotifDebug){
      System.out.println("\nGEDInterface::send");
    }

    if(mesg instanceof NotificationMessage){

      NotificationMessage notifMesg  = (NotificationMessage) mesg;

      if(glbEvntNotifDebug){
        notifMesg.toString();
      }

      String appID = null;
      String oldAppID = null;

      // for composite event
      if(notifMesg.getAppNm().equals("")){
        oldAppID ="";
      }
      // for global primitive event
      else{
        // Mapping the new application id to the old application id
        appID = notifMesg.getAppNm()+"_"+notifMesg.getMachNm();
        oldAppID = GEDUtilities.remUnderScore(GEDUtilities.getOldAppID(appID));

        // Need to set the machine name in notification message to be the old one
        // so that when the consumer receives the message, it can realize it.
        String oldMachineName =  GEDUtilities.getMachNm(GEDUtilities.getOldAppID(appID));
        notifMesg.setMachNm(oldMachineName);
      }

      Vector consumerList = (Vector) glbEvntName_consumerList.get(notifMesg.getProdEvntNm()+oldAppID);

      if(glbEvntNotifDebug)
        System.out.println("Size of consumer list "+consumerList.size());

      for(int i =0 ; i<consumerList.size() ; i++){
        String consID = (String) consumerList.elementAt(i);

        // Map the old application id to the new application it this application
        // runs on the different machine.
        String  consIP = (String) clntAddrsHt.get(consID);

        consID = GEDUtilities.getNewAppID(consID);

        String url = "rmi://"+consIP+"/"+consID;

        if(glbEvntNotifDebug)
          System.out.println("\nsend the message to "+url);
        notifMesg.setDest(url);

        // Instead of directly sending out the notification to each client,
        // we need to put them into the buffer, and let the designated thread
        // notifies the occurence of event to each client.
        notifMesgBuffer.put(notifMesg);
      }
    }else {
      System.out.println("ERROR");
    }
  }

  /**
   * Add the detection request message into the list which corresponds to the
   * procducer. It's used when the consumer subcribes to the event at the remote site.
   */
  synchronized void addDetcReqstToProdList(String producer,SentinelMessage dectReqstMesg){

    if(glbEvntReqstDebug)
      System.out.println("\nGEDInterface::sendDetctnReqst");

    if(clntAddrsHt.containsKey(producer)){

      Vector reqstList = (Vector)  prod_DectectnReqstHt.get(producer);
      reqstList.add(dectReqstMesg);

      if(glbEvntReqstDebug)
        System.out.println("Send the detection request to producer");
    }
    else{

      if(glbEvntReqstDebug){
        System.out.println("Add the event of interest in the prod_DectectnReqst table");
        System.out.println(producer+" hasn't registered in to the system");
      }

      // Some cosumers already asked for event detection from this producer
      if(prod_DectectnReqstHt.containsKey(producer)){
        Vector reqstList = (Vector)  prod_DectectnReqstHt.get(producer);
        reqstList.add(dectReqstMesg);
      }
      // ask from the new producer
      else{
        Vector dectReqstList = new Vector();

        // initialize the number of notification request to 0
        // these reserved slots will be used later
        dectReqstList.addElement (new Integer(0));
        dectReqstList.addElement (new Integer(0));

        dectReqstList.add(dectReqstMesg);
        if(glbEvntReqstDebug)
          System.out.println("Add this request to the producer list :: \n"+dectReqstMesg.toString());
        prod_DectectnReqstHt.put (producer,dectReqstList);
      }
    }
  }

 /**
  * This method is called from GEDMesgRecvImp when the GED receive the detection
  * request message from the client.
  */

 public void receive(DetectionRequestMessage detReqstMesg ){
    if(glbEvntReqstDebug){
      System.out.println("\n\nGEDInterface::receive");
    }

    if(glbEvntReqstDebug){
      System.out.print("Request for this event ::\n");
      System.out.println(detReqstMesg.getEvntNm()+detReqstMesg.getAppNm ()+detReqstMesg.getMachNm ());

      if(detReqstMesg.getContext() == ParamContext.RECENT.getId())
        System.out.println("In the RECENT context");
      else if(detReqstMesg.getContext() == ParamContext.CONTINUOUS.getId())
        System.out.println("In the CONTINUOUS context");
      else if(detReqstMesg.getContext() == ParamContext.CHRONICLE.getId())
        System.out.println("In the CHRONICLE context");
      else if(detReqstMesg.getContext() == ParamContext.CUMULATIVE.getId())
        System.out.println("In the CUMULATIVE context");
    }

    int context = detReqstMesg.getContext();

    Vector glbNodeManagerList = GlobalNodeManager.getGlbNdMngrList();

    /* Searching for the global event node or the composite event on server.
     * Then set the context recursively, and propagate the requst to the producer
     */

    for(int i = 0 ; i < glbNodeManagerList.size () ; ++i){
      GlobalNodeManager gNdMngr = (GlobalNodeManager) glbNodeManagerList.elementAt(i);


      if(glbEvntReqstDebug)
        System.out.println("Searching for the node ::"+detReqstMesg.getEvntNm()+detReqstMesg.getAppNm ()+detReqstMesg.getMachNm ());

      Event eV = (Event) gNdMngr.getGlbEvntNd(detReqstMesg.getEvntNm()+detReqstMesg.getAppNm ()+detReqstMesg.getMachNm ());

      eV.setSendBackFlag(true);
      eV.setContextRecursive(context);
      if(glbEvntReqstDebug)
        System.out.println("Set the send back flag in global event :: \n"+eV.getEventName());
    }
  }

 /**
  * This method is called from GEDMesgRecvImp when the GED receive the notification
  * message from the client.
  */

  public void receive(NotificationMessage notifMesg){

    if(glbEvntReqstDebug){
      System.out.println("\nReceive the notificaiton message\n");
      System.out.println("Notifying this event : "+notifMesg.getProdEvntNm()+notifMesg.getAppNm ()+notifMesg.getMachNm ());
    }

    /**
     * Map the new Producer ID with Old ID
     */
    String prodID = notifMesg.getAppNm ()+"_"+notifMesg.getMachNm ();
    String oldProdID = GEDUtilities.getOldAppID(prodID) ;
    if(glbEvntNotifDebug){
      System.out.println("Print out the old prod id of this event name :: "+notifMesg.getProdEvntNm());
      System.out.println("Old id : "+oldProdID);
      System.out.println("New id : "+prodID);
    }

    /* Seaching for the global event node */
    Vector glbNodeManagerList = GlobalNodeManager.getGlbNdMngrList();
    for(int i = 0 ; i < glbNodeManagerList.size () ; ++i){

      GlobalNodeManager  gNdMngr = (GlobalNodeManager) glbNodeManagerList.elementAt(i);

      // Looking up the event by using global event name
      // global event name => "producer event name + app name + machine name"

//      Event eV =   (Event) gNdMngr.getGlbEvntNd(notifMesg.getProdEvntNm()+notifMesg.getAppNm ()+notifMesg.getMachNm ());
      Event eV =   (Event) gNdMngr.getGlbEvntNd(notifMesg.getProdEvntNm()+ GEDUtilities.remUnderScore(oldProdID));

      GlobalEvent gV = null;
      if(eV instanceof GlobalEvent){
        gV = (GlobalEvent) eV;
      }else{
        System.err.println("ERROR in GEDInterface::receive");
      }

      GECAAgent gAgent =  gNdMngr.getAssocAgent();


      PCEntry entry = (notifMesg.getPCTable()).getRecentSet();

      /**
       * if the entry is null, there is no occurence in this context.
       */
      if(entry != null){
        if(glbEvntNotifDebug)
          System.out.println("RECENT set");
      }
      else{
        if(glbEvntNotifDebug)
          System.out.println("NO RECENT set");
      }



      gAgent.notifyGlobalNode (gV,notifMesg);


    }
  }

  /**
   * This method is used to put the global event name and consumer's identification
   * into hashtable.
   */
 void putGlbEvntNmConsList(String glbEvntNm,String consID){
    if(glbEvntCreatnDebug){
      System.out.println("Add this event name "+glbEvntNm);
      System.out.println("and consumer "+consID +" into the system.");
    }
    Vector consVector = (Vector) glbEvntName_consumerList.get(glbEvntNm);
    if(consVector != null){
      System.out.println("NOT FOUND");
      consVector.add(consID);
    }else{
      consVector = new Vector();
      consVector.add(consID);
      glbEvntName_consumerList.put(glbEvntNm,consVector);
    }
  }
}


   /**
   * This hashtable maps the consumer with the list of notification messages.
   */
  //private Hashtable cons_NotifMesgList = new Hashtable();












//   gedIntf.putConsNotifMesg(cons,notifMesg);
//    glbNdMngr.putGlbEvntNm_GlbEvntNd(glbEvntNm,andEvent);



/*  private synchronized void sendDetctnReqst(String producer,SentinelMessage dectReqstMesg){

      try{
        String url = "rmi://"+clntAddrsHt.get(producer)+"/";
        System.out.println("send the detection request to "+url+producer);
        SentinelMesgRecv rec = (SentinelMesgRecv)Naming.lookup(url+producer);
        rec.onMessage(dectReqstMesg);
      }catch(Exception e){
        e.printStackTrace();
      }

  }*/

  /*This method is for the future work when we have multiple Global Event Detectors*/
/*  public void send(String destination, SentinelMessage mesg) {
    if(mesg instanceof DetectionRequestMessage){
      sendDetctnReqst(destination,mesg);
    }
    else if(mesg instanceof NotificationMessage){
      try{
        sendNotification(destination,mesg);
      }catch(Exception e){
        e.printStackTrace();
      }
    }else{
      System.out.println("Internal Error !!! LEDInterface:receive");
    }
  }*/




  // call by  GECAAgent
/*  private void sendNotification(String consumer, SentinelMessage notifMesg) throws RemoteException{
    try{
      String url = "rmi://"+clntAddrsHt.get(consumer)+"/";
      System.out.println("Send the notification message to "+url+consumer);
      SentinelMesgRecv rec = (SentinelMesgRecv)Naming.lookup(url+consumer);
      rec.onMessage(notifMesg);
    }catch(Exception e){
      e.printStackTrace();
    }

  }*/






