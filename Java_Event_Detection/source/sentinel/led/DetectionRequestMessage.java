/**
 * Title:        Local Event Dectection
 * Version:
 * Copyright:    Copyright (c) 1999
 * Author:       Weera Tanpisuth
 * Company:      ITLAB at University of Texas, Arlington
 */

package sentinel.led;
import sentinel.comm.*;

import java.io.*;

/**
 * The detection request message is implemented the sentinel message, containing
 * an event of interest name, an application name in which an event has raised,
 * a location where the application runs, and the context in which the event is
 * detected.
 *
 * This detection request message is packed by a consumer and is sent to the
 * server GED who will propagate the request to the producer.
 */

public class DetectionRequestMessage implements Serializable, SentinelMessage {

  /** Event name */
  private String evntNm;

  /** Application name */
  private String appNm;

  /** Machine name */
  private String machNm;

  /** Context */
  private int context;

  public DetectionRequestMessage(){}

  /** Constructor */
  public DetectionRequestMessage(String evntNm,String appNm, String machNm,int context) {
    this.evntNm = evntNm;
    this.appNm = appNm;
    this.machNm = machNm;
    this.context = context;
  }

  /** Return an event name */
  public String getEvntNm(){
    return evntNm;
  }

  /** Return an application name */
  public String getAppNm(){
    return appNm;
  }

  /** Return a machine name */
  public String getMachNm(){
    return machNm;
  }

  /** Return a context */
  public int getContext(){
    return context;
  }

  /** Return the message information */
  public String toString(){

    StringBuffer bs = new StringBuffer();
    bs.append ("Detection Request Message\n");
    bs.append ("- Event name       : "+evntNm+"\n");
    bs.append ("- Application name : "+appNm+"\n");
    bs.append ("- Machine name     : "+machNm+"\n");
    bs.append ("- Context          : "+ParamContext.getContext(context));
    return bs.toString ();
  }
}