/**
 * Title:        Sentinel Event Dectection
 * Version:
 * Copyright:    Copyright (c) 1999
 * Author:       Weera Tanpisuth
 * Company:      ITLAB at University of Texas, Arlington
 * Description:  Tthe common files shared between LED and GED
 */
package sentinel.led;

/**
 * This class is used to keep all the constant values. Most of these values
 * are read from the configuration file.
 */

public class Constant {

  /**
   * Name of a receiver thread which will run when "onMessage" method is called (RMI).
   */
  public static final String LEDReceiverThreadName = "LEDReceiverThread";


  // The values are read from App.Config file

  /** Detection scope (GLOBAL or LOCAL) */
  public static String SCOPE = "LOCAL";

  /** The application name */
  public static String APP_NAME ;

  /** The machine name on which the application are running. */
  public static String APP_URL ;


  /** The application id is the concatenation of APP_NAME nad APP_URL. */
  public static String APP_ID ;

  /** The location of the Global Event Detector */
  public static String GED_URL;

  /** The name of the global event detector */
  public static String GED_NAME;

  // Remark !! May not need
  static String GED_PORT = null;

  public static boolean RSCHEDULERFLAG;
}
