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

/**
 * This global event handle class is used in composite event definitions.
 */

public class GlobalEventHandle extends EventHandle{


  private String prodEvntNm;
  private String appNm;
  private String machNm;

  public GlobalEventHandle(Event event, String prodEvntNm) {
      super(event, prodEvntNm);
  }

  /**
   * Returns the event name on the producer site
   */
  String getProdEvntNm(){
    return prodEvntNm;
  }

  /**
   * Returns the application name in which the event has been raised
   */
  String getAppNm(){
    return appNm;
  }

  /**
   * Returns the machine name where the event has been raised
   */
  String getMachNm(){
    return machNm;
  }
}


