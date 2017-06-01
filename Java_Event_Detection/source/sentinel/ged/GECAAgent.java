
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

import java.rmi.*;
import java.rmi.server.*;
import java.rmi.Naming  ;
import java.rmi.registry.*;
import java.io.*;
import java.util.*;

/**
 *  This GECAAgent class is provided to support the global event detection.
 *  This class contains main method used to start the global event detector.
 *
 */



public class GECAAgent {

  static boolean glbEvntNotifDebug = DebuggingHelper.isDebugFlagTrue("glbEvntNotifDebug");

  /* Global Event Detection Agent Name */
  private static String agentName;

  /* Global Event Node Manager */
  private static GlobalNodeManager glbNdMngr;

  /* Communication interface for GED */
  private static GEDInterface gedIntf;

  /* Global event factory is to create global event node from remote. */
  private static GlobalEventFactoryImp glbEvntFactryImp ;

  /* Constructor */
  GECAAgent(String agentName) {
    this.agentName = agentName;
  }


  /**
   * This method initializes some of the data structures used by the Global
   * Event Detector (GED), instantiating global node manager, global
   * interface. It also read the configuration from Global.config.
   */

  public static GECAAgent initializeGECAAgent() throws RemoteException{

    System.out.println("Initialize GECAAgent ....");

    /* Read the global event detector configuration file (Global.config) */
    GEDUtilities.readGEDConfig ();

    agentName = GEDConstant.GED_NAME;

    GECAAgent gecaAAgent = new GECAAgent(agentName);

    glbNdMngr = new GlobalNodeManager(gecaAAgent);

    /**
     *  Add this global event detector agent and its global node manager
     *  into the table containing all the GECAAgents and global node managers
     */
    GlobalNodeManager.putAgentName_glbManager(agentName,glbNdMngr);

    /** Add the global node manager into the list of global node manager */
    GlobalNodeManager.addGlbNdMngr(glbNdMngr);

    gedIntf = new  GEDInterface();

    try {

      /**
      * Binding the global event factory into the registry so the client
      * can look up and make RMI call to create the global event.
      */

      glbEvntFactryImp = new GlobalEventFactoryImp(gedIntf,glbNdMngr);

      /**
       * Name of global factory depends on GED name.
       */
      Naming.rebind(GEDConstant.GED_NAME+"_GLOBAL_FACTORY" ,glbEvntFactryImp);

      System.out.println("Server bound and started !!!!!!!");

      /**
       * End server ( Hit any key to end the server )
       */
      DataInputStream reader = new DataInputStream(System.in);
      int ch = '0';
      try {
        ch = reader.read();
      }catch(IOException ie) {
	ie.printStackTrace();
      }
    }
    catch (Exception e){
      System.err.println("Server exception: " +  e.getMessage());
      e.printStackTrace();
    }
    return gecaAAgent;
  }

  /**
   * This method is used to notify the global event node. This is called by
   * GEDInterface object when it receive the notification from a producer.
   */

  void notifyGlobalNode(GlobalEvent glbEvnt,NotificationMessage notifMesg){

    if(glbEvntNotifDebug)
      System.out.println("\n GECAAgent::notifyGlobalNode");

    /* Send the notification message to consumers when the send back flag is set */

    if(glbEvnt.getSendBackFlag ()){
      if(glbEvntNotifDebug)
        System.out.println("Send back flag is set to true");

      gedIntf.send(notifMesg);
    }else{
      if(glbEvntNotifDebug)
       System.out.println("Send back flag of "+ glbEvnt.getEventName() +" is false");
    }

    if(glbEvntNotifDebug)
      System.out.println("Notify the global node");

    /**
     * Notify to the corresponding global event node and propagate
     * the occurence of this event.
     */

    glbEvnt.notify(notifMesg);
  }


  /**
   * returns the global event communication interface (GEDInterface) object
   */

  static GEDInterface getGEDInterface(){
    return gedIntf;
  }

  /**
   * This main method is used to start the GED.
   */

  public static void main(String[] args)throws RemoteException {
    initializeGECAAgent();
  }

}