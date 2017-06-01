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
import java.rmi.server.*;

/**
 *  The class is provided to support the client registration. The client can
 *  make a remote invocation by using "register" method to register to the GED.
 */

public class ServerConnectorImp extends UnicastRemoteObject implements ServerConnector{

  static boolean registerDebug = DebuggingHelper.isDebugFlagTrue("registerDebug");


  private GEDInterface gedIntf;

  public ServerConnectorImp() throws RemoteException{}

  /**
   * Constructor
   */

  public ServerConnectorImp(GEDInterface gedIntf) throws RemoteException{
    this.gedIntf = gedIntf;
  }

  /**
   * Register to the global event detector.
   */

  synchronized public void register(String appID) throws RemoteException{

    // Map the new application id to the old application id

    appID  =  GEDUtilities.getOldAppID(appID);

    if(appID == null){
      System.err.println("ERROR :: MAPPING in Gobal.config may not set properly.");
      System.exit(0);
    }

    try{

      if(registerDebug){
        System.out.println("\n\nServerConnectorImp::register");
        System.out.println(appID+" is registering to the GED");
        System.out.println(appID+" is on "+RemoteServer.getClientHost());
      }

      gedIntf.addClient (appID, RemoteServer.getClientHost());
      gedIntf.getDectReqst(appID);
    }catch(Exception e){
      e.printStackTrace();
    }

    if(registerDebug)
      System.out.println("Registration completed");
  }
}