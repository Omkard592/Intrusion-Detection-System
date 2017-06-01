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
 * This remote event handle class is used in composite event definitions.
 */

public class RemoteEventHandle extends EventHandle{
  /** an event name at the producer site. */
  private String prodEvntNm;

  /** an application name in which the event occurs*/
  private String appNm;

  /** a machine name where the application runs */
  private String machNm;



  /**
   * Constructor
   */

  RemoteEventHandle(Event event, String evntNm, String  prodEvntNm, String appNm, String machNm) {
      super(event, evntNm);
      this. prodEvntNm =  prodEvntNm;
      this.appNm = appNm;
      this.machNm = machNm;
  }

  /** Return an event name */
  String getProdEvntNm(){
    return prodEvntNm;
  }

  /** Return an application name */
  String getAppNm(){
    return appNm;
  }

  /** Return a machine name */
  String getMachNm(){
    return machNm;
  }
}