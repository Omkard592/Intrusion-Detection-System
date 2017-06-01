/**
 * Title:        Local Event Detection
 * Description:
 * Copyright:    Copyright (c) 1999
 * Company:      ITLAB, Unversity of Texas, Arlington
 * @author       Weera Tanpisuth
 * @version
 */

package sentinel.led;

import java.util.Hashtable ;

/**
 * This class in used to manage the remote event node. The remote event
 * detection can refer the event node by using this class
 */

public class RemoteNodeManager {

  /** This hashtable contains all the agent names and their correspoding
   *  remote node managers.
   */
  private static Hashtable agentName_remNdManager = new Hashtable();

  /**
   * This hashtable contains consumer event name (given by the user) and its
   * consumer event handle.
   */
  private Hashtable consEvntNm_ConsEvntHandl= new Hashtable();

  /**
   * This hashtable contains consumer event name (given by the user) and its
   * consumer event node.
   */
  private Hashtable consEvntNm_ConsEvntNd= new Hashtable();

  /**
   * This hashtable map the global event name ( producer event name + producer
   * application name + machine name ) and consumer event name
   */
  private Hashtable glbEvntNm_ConsEvntNm = new Hashtable();


  public RemoteNodeManager() {}

 /** Put the agent name and correspoding remote node manager into hashtable */
  static void putAgentName_remManager(String agentNm, RemoteNodeManager remNdMngr){
    agentName_remNdManager.put (agentNm,remNdMngr);
  }

  /** Put the global event name and consumer event node into hashtable */
  void putConsEvntNm_ConsEvntNdHT(String remEvntNm, RemoteEvent remEvntNd){
    consEvntNm_ConsEvntNd.put(remEvntNm,remEvntNd);
  }

  /** Put the global event name and consumer event handle into hashtable */
  void putConsEvntNm_ConsEvntHandlHT(String remEvntNm , RemoteEventHandle remEvntHandl){
    consEvntNm_ConsEvntHandl.put ( remEvntNm ,remEvntHandl);
  }

  /** Returns global event name consumer event handle hashtable */
  Hashtable getConsEvntNm_ConsEvntHandlHT(){
    return consEvntNm_ConsEvntHandl;
  }

  /** Put the global event name and consumer event name into hashtable */
  void putGlbEvntNm_ConsEvntNm(String glbEvntNm ,String consEvntNm){
    glbEvntNm_ConsEvntNm.put ( glbEvntNm , consEvntNm);
  }

  /** Returns global event name consumer event name hashtable */
  Hashtable getGlbEvntNm_ConsEvntNm(){
    return glbEvntNm_ConsEvntNm;
  }
}