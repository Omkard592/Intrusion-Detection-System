/**
 * Title:        Local Event Detection
 * Description:
 * Copyright:    Copyright (c) 1999
 * Company:      ITLAB, Unversity of Texas, Arlington
 * @author       Weera Tanpisuth
 * @version
 */

package sentinel.led;

import sentinel.comm.*;
import java.io.*;

/**
 * The notification message is implemented the sentinel message, containing
 * an event of interest name, an application name in which an event has raised,
 * a location where the application runs, and the PCTable of which the event is
 * detected.
 *
 * This detection request message is packed by a produder and is sent to the
 * server GED who will propagate the request to the consumer.
 */

public class NotificationMessage implements Serializable,SentinelMessage  {


  /** Event name */
  private String prodEvntNm;

  /** Application name */
  private String appNm;

  /** Machine name */
  private String machNm;

  /** Destination */
  private String consumer;

  /** Parameter Lists and their contexts */
  private PCTable pcTable;

  public NotificationMessage(){}

  public NotificationMessage(String prodEvntNm,String appNm, String machNm, PCTable pcTable) {
    this.prodEvntNm = prodEvntNm;
    this.appNm = appNm;
    this.machNm = machNm;
    this.pcTable = pcTable;
  }


  /** Return the event name */
  public String getProdEvntNm(){
    return prodEvntNm;
  }

  /** Return the PCTable */
  public PCTable getPCTable(){
    return pcTable;
  }

  /** Return the application name */
  public String getAppNm(){
    return appNm;
  }

  /** Return the machine name */
  public String getMachNm(){
    return machNm;
  }

  /** Set the machine name */
  public void setMachNm(String machNm){
    this.machNm =machNm;
  }

  /**
   * Set destination of this message
   */
  public void setDest(String consumer){
    this.consumer = consumer;
  }

  /**
   * Return the destination of this message
   */
  public String getDest(){
    return consumer;
  }

   /** Return the message information */
  public String toString(){

    StringBuffer bs = new StringBuffer();
    bs.append ("Notification Message\n");
    bs.append ("- Event name       : "+prodEvntNm+"\n");
    bs.append ("- Application name : "+appNm+"\n");
    bs.append ("- Machine name     : "+machNm);
    return bs.toString ();
  }
}
