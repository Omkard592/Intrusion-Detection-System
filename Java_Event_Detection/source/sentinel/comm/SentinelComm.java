/**
 * Title:        Global Event Detection
 * Description:
 * Copyright:    Copyright (c) 1999
 * Company:      ITLAB University of Texas, Arlington
 * @author       Weera Tanpisuth
 */

package sentinel.comm;

/**
 * This interface defines a communication module for the event detector.
 * It is implemented by the classes LEDInterface and GEDInterface to support
 * a communication between local event detector and global event detector.
 */

public interface SentinelComm {

  /**
   * Send the sentinel message (Detection request message)
   */

  public void send(String destination, SentinelMessage mesg);

  /**
   * Send the sentinel message (Notification message)
   */

  public void send(SentinelMessage mesg);

}