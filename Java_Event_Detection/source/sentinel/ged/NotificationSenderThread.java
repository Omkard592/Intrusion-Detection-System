
/**
 * Title:        Global Event Detection
 * Description:  Your description
 * Copyright:    Copyright (c) 1999
 * Company:      ITLAB, Unversity of Texas, Arlington
 * @author       Weera Tanpisuth
 * @version
 */

package sentinel.ged;
import sentinel.led.*;
import sentinel.comm.*;


import java.rmi.*;
import java.rmi.registry.*;
import java.util.*;

/**
 * This runnable thread helps the global even detection to send the notification
 * to the consumer. This thread unloaded the burden tasks of the detection thread.
 */

public class NotificationSenderThread implements Runnable{

  /**
   * Buffer containing the notification messages waiting for the thread
   * to send to consumer
   */
  private NotifMesgBuffer buffer;

  /**
   * Constructor
   */
  public NotificationSenderThread(NotifMesgBuffer buffer) {
    this.buffer = buffer;
  }

  /**
   * This thread keeps pulling the message from the buffer, it any.
   * It will send the message to the corresponding consumer.
   */

  public void run(){
    while(true){
      NotificationMessage notifMesg = (NotificationMessage) buffer.get();

      // Get the notificaion message from the buffer
      String dest = notifMesg.getDest();
      try{
        SentinelMesgRecv rec = (SentinelMesgRecv)Naming.lookup(dest);
        rec.onMessage(notifMesg);
      }catch(Exception e){
        e.printStackTrace();
      }
    }
  }
}