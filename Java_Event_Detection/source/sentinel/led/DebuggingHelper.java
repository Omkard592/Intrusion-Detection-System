
/**
 * Title:        Local Event Detection
 * Description:  Your description
 * Copyright:    Copyright (c) 1999
 * Company:      ITLAB, Unversity of Texas, Arlington
 * @author       Weera Tanpisuth
 * @version
 */
package sentinel.led;

import java.io.*;
import java.util.Properties;

/**
 * This class contains some general purpose methods that are used by the
 * ECAAgent. Some of these methods are public and may be used by user applications.
 */

public class DebuggingHelper{
  static Properties props = null;
  static public boolean isDebugFlagTrue(String debugFlag) {
    if (props == null) {
      props = new Properties();
      try {
	FileInputStream in = new FileInputStream("properties.txt");
	props.load(in);
      }catch (FileNotFoundException fnfe) {
	//System.out.println("Utilities:Properties file not found");
//	fnfe.printStackTrace();
      }catch (IOException ioe) {
	ioe.printStackTrace();
      }
    }
    if (props.getProperty(debugFlag) == null)
      return false;
    else if (props.getProperty(debugFlag).equals("true"))
      return true;
    else
      return false;
  }
}





