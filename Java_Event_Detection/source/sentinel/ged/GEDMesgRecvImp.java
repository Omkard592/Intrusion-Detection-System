/**
 * Title:        Local Event Detection
 * Description:  Your description
 * Copyright:    Copyright (c) 1999
 * Company:      ITLAB, Unversity of Texas, Arlington
 * @author weera tanpisuth
 * @version
 */

package sentinel.ged;

import sentinel.comm.*;
import sentinel.led.*;
import java.rmi.*;
import java.io.*;
import java.rmi.server.*;

/**
 * This class provids the "onMessage" method for a remote invocation.
 * The client can send the notification message or the detection request
 * message by invoking "onMessage" method.
 */

public class GEDMesgRecvImp extends UnicastRemoteObject implements SentinelMesgRecv{

  static boolean glbEvntNotifDebug = DebuggingHelper.isDebugFlagTrue("glbEvntNotifDebug");
  static boolean glbEvntReqstDebug = DebuggingHelper.isDebugFlagTrue("glbEvntReqstDebug");

  /**
   * A communicaiton module for global event detector.
   */
  private transient GEDInterface gedIntf;

  public GEDMesgRecvImp (GEDInterface gedIntf)throws RemoteException {
    this.gedIntf = gedIntf;
  }

  /**
   * The method is used to received the incoming message from the client
   * who makes a RMI call.
   */

  public void onMessage(SentinelMessage mesg){
    if(glbEvntNotifDebug || glbEvntReqstDebug)
      System.out.println("\nGEDMesgRecvImp::onMessage");

    try {

      DetectionRequestMessage detReqstMesg ;
      NotificationMessage notifMesg ;

      if(mesg instanceof NotificationMessage){
        if(glbEvntNotifDebug)
          System.out.println("Receive the notificaiton message");
        gedIntf.receive((NotificationMessage) mesg);
      }else if (mesg instanceof DetectionRequestMessage){
        if(glbEvntReqstDebug)
          System.out.println("Receive the detection request message");
        gedIntf.receive((DetectionRequestMessage) mesg);
      }else
        System.out.println("ERROR in casting back the mesg");
    }catch(Exception e){
      System.out.println("Exception: " + e.getMessage());
      e.printStackTrace();
    }
  }
}
