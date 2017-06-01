/**
 * Title:        Local Event Detection
 * Description:
 * Copyright:    Copyright (c) 1999
 * Company:      ITLAB, Unversity of Texas, Arlington
 * @author       Weera Tanpisuth
 * @version
 */

package sentinel.led;

/**
 * The remote event factory class is provided to create the remote event node.
 */

public class RemoteEventFactory {

  static boolean glbEvntCreatnDebug = DebuggingHelper.isDebugFlagTrue("glbEvntCreatnDebug");

  /** Constructor */
  public RemoteEventFactory() {
  }

  /**
   * This method is used to create remote event node
   */
  RemoteEventHandle createRemEvnt(String consEvntNm,String prodEvntNm,  String appNm, String machNm,RemoteNodeManager remNdMngr){

    if(glbEvntCreatnDebug)
      System.out.println("\nRemoteEventFactory::createRemEvnt");

    /** If the event is detected at server appNm and machNm a left blank */
    RemoteEvent remEvnt = new RemoteEvent(consEvntNm,prodEvntNm,appNm, machNm);

    remNdMngr.putConsEvntNm_ConsEvntNdHT(consEvntNm,remEvnt);

    /* Mapp the global event name to local event name (consumer event name). */

    remNdMngr.putGlbEvntNm_ConsEvntNm (prodEvntNm+appNm+machNm,consEvntNm);
    RemoteEventHandle remEvntHandl = new RemoteEventHandle(remEvnt,consEvntNm,prodEvntNm,appNm,machNm);
    remNdMngr.putConsEvntNm_ConsEvntHandlHT(consEvntNm,remEvntHandl);
    return remEvntHandl;
  }
}