
/**
 * Title:        Global Event Detection
 * Description:
 * Copyright:    Copyright (c) 1999
 * Company:      ITLAB University of Texas, Arlington
 * @author       Weera Tanpisuth
 */

package sentinel.comm;

import java.rmi.*;

/**
 * This interface defines a message receiver for the communication interfaces
 * (LEDInterface and GEDInterface). It is implemented by the classes GEDMesgRecvImp
 * and LEDMesgRecvImp, providing the "onMessage" method for a remote invocation.
 */


public interface SentinelMesgRecv extends Remote{

  /**
   * Transmit the sentinel message to the remote site
   */

  void onMessage(SentinelMessage mesg) throws RemoteException;

}