/**
 * Title:        Global Event Detection
 * Description:  Your description
 * Copyright:    Copyright (c) 1999
 * Company:      ITLAB, Unversity of Texas, Arlington
 * @author       Weera Tanpisuth
 * @version
 */

package sentinel.ged;

import sentinel.led.NotificationMessage;
import java.util.*;

/**
 *  The buffer is used to stores notification messages. The detection thread
 *  put notification messages in the buffer.
 */

public class NotifMesgBuffer {
  private Vector buffer = new Vector();
  public  NotifMesgBuffer( ) { }

  /**
   * This method gets a notification message object from the buffer.
   */

  synchronized public NotificationMessage get() {

    //  make sure that there is a message in the buffer
    while(buffer.size() == 0){

      //wait if there is no message in the buffer
      try{
        wait();
      }catch(InterruptedException ie) {
        System.out.println("InterruptedException caught");
        ie.printStackTrace();
      }
    }

    NotificationMessage notifMesg = (NotificationMessage) buffer.remove(0);
    return notifMesg;
  }

  /**
   * This method puts a notification message into the buffer.
   */
  synchronized public void put(NotificationMessage notifMesg) {

    buffer.add(notifMesg);

    // when the buffer has a message, wake up the consumer thread.
    if (buffer.size() == 1)
      notifyAll();
  }
}