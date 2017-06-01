package sentinel.led;

/**
 * Title:        Local Event Detection
 * Description:
 * Copyright:    Copyright (c) 1999
 * Company:      ITLAB, Unversity of Texas, Arlington
 * @author       Weera Tanpisuth
 * @version
 */

import sentinel.comm.*;
import java.rmi.*;
import java.io.*;
import java.rmi.server.*;

/**
 * This class implements SentinelMesgRecv. This class works as a messge listener.
 * The global event detector sends the detection request of the notification
 * message to the local event detector by invoking the onMessage method.
 */
public class LEDMesgRecvImp extends UnicastRemoteObject implements SentinelMesgRecv{

  static boolean evntNotifDebug = DebuggingHelper.isDebugFlagTrue("evntNotifDebug");
  static boolean evntReqstDebug = DebuggingHelper.isDebugFlagTrue("evntReqstDebug");



  private transient LEDInterface ledIntf;

  /**
   * constructor
   */
  public LEDMesgRecvImp(LEDInterface ledIntf)throws RemoteException {
    this.ledIntf = ledIntf;
  }

  /**
   * This method supports RMI call from the remote machine. It receive the
   * incoming message and determine the message type before processing this
   * message.
   */
  public void onMessage(SentinelMessage mesg){

    if(evntNotifDebug || evntReqstDebug)
      System.out.println("\nLEDMesgRecvImp::onMessage");

    try {
      DetectionRequestMessage detReqstMesg ;
      NotificationMessage notifMesg ;

      if(mesg instanceof NotificationMessage){
        if(evntNotifDebug)
          System.out.println("Receive the notificaiton message");
        notifMesg = (NotificationMessage) mesg;
        ledIntf.receiveNotification (notifMesg);
      }
      else if (mesg instanceof DetectionRequestMessage){
        if(evntReqstDebug)
          System.out.println("Receive the detection request message");
        detReqstMesg = (DetectionRequestMessage) mesg;
        ledIntf.receiveEvntDetctnReqst(detReqstMesg);
      }else
        System.out.println("ERROR in casting back the mesg");

    }catch(Exception e){
      System.out.println("Exception: " + e.getMessage());
      e.printStackTrace();
    }
  }
}