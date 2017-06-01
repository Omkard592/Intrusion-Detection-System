/**
 * Title:        Global Event Detection
 * Description:
 * Copyright:    Copyright (c) 1999
 * Company:      ITLAB University of Texas, Arlington
 * @author       Weera Tanpisuth
 */

package sentinel.comm;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * This interface defines a connector which used in registration process.
 * It is implemented by the class ServerConnectorImp, providing the "register"
 * method for a remote invocation.
 */

public interface ServerConnector extends Remote{

  /**
   * Register an application to the global event detector
   */

  public void register(String appID) throws RemoteException;


}






