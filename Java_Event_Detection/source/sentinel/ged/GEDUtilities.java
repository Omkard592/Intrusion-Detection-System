
/**
 * Title:        Global Event Detection
 * Description:  Your description
 * Copyright:    Copyright (c) 1999
 * Company:      ITLAB, Unversity of Texas, Arlington
 * @author       Weera Tanpisuth
 * @version
 */

package sentinel.ged;
import java.io.*;
import java.util.*;

/** This class contains some general purpose methods that are used by the
 *  global event detection.
 */

class GEDUtilities {

  /**
   * The hashtable maps the old application id and new application id.
   * It can be used when the application runs on the different machine.
   */
  private static Hashtable OLDAPPID_NEWAPPIDMAP = null;

  /**
   * The hashtable maps the new application id and new application id.
   * It can be used when the application runs on the different machine.
   */
  private static Hashtable NEWAPPID_OLDAPPIDMAP = null;

  public GEDUtilities() {}

  /**
   * This method is used to read the ged configuration file (ged.config)
   */
  static void readGEDConfig() {

    OLDAPPID_NEWAPPIDMAP = new Hashtable();
    NEWAPPID_OLDAPPIDMAP = new Hashtable();
    BufferedReader br = null;

    System.out.println("Start reading the config file");

    try {
      br = new BufferedReader(new FileReader("Global.config"));
    }catch (FileNotFoundException fnfe) {
      System.out.println("Can't find the Global.config");
      System.out.println("Please create Global.config, before running application");
      System.exit(-1);
      fnfe.printStackTrace();
    }catch (IOException ioe) {
      ioe.printStackTrace();
    }

    String sNextLine = null;
    try{
      sNextLine = br.readLine();
      while(sNextLine != null){
        if(sNextLine.equalsIgnoreCase("BEGIN")){
          sNextLine = br.readLine();
          while(sNextLine != null){
            StringTokenizer st = new StringTokenizer(sNextLine);
            if(st.hasMoreTokens()) {
              String prop = st.nextToken();
              if(prop.equalsIgnoreCase("LOG_DIR")){
                // REMARK !!!!
                // For future work
              }
              else if(prop.equalsIgnoreCase("MAPPING")){

                String oldId = null;
                String newId = null;
                try{
                  oldId = st.nextToken();
                  newId = st.nextToken();
                  System.out.println("MAPPING "+oldId+" :: " +newId);
                }catch(NullPointerException npe){
                  System.err.print("ERROR in Global.config. MAPPING is not set properly ") ;
                  npe.printStackTrace();
                  System.exit(-1);
                }
                OLDAPPID_NEWAPPIDMAP.put(oldId,newId);
                NEWAPPID_OLDAPPIDMAP.put(newId,oldId);
              }
              else if(prop.equalsIgnoreCase("GED_NAME")){
                GEDConstant.GED_NAME = st.nextToken();
                System.out.println("GED Name :: "+ GEDConstant.GED_NAME);
              }
              else if(prop.equalsIgnoreCase("END")){
                System.out.println("Finish reading the global.config");
                br.close();
                return;
              }
              sNextLine = br.readLine();
            }
          }
        }
        sNextLine = br.readLine();
      }
    }catch(Exception exc){
      exc.printStackTrace() ;
    }
  }

  /**
   * Return the old application identificaiton
   */
  static String getOldAppID(String newAppID){
    return (String) NEWAPPID_OLDAPPIDMAP.get(newAppID);
  }

  /**
   * Return the new application identificaiton
   */
  static String getNewAppID(String oldAppID){
    return (String) OLDAPPID_NEWAPPIDMAP.get(oldAppID);
  }

  /**
   * Remove the underscore in the application id
   * application id  = application name + "_" + machine name
   */
  static String remUnderScore(String appID) {
    StringBuffer newStr = new StringBuffer();
    char ch;
    int strLen = appID.length();

    for (int i=0; i<strLen; i++) {
      ch = appID.charAt(i);
      if (ch != '_')
         newStr.append(ch);
      }
      return newStr.toString();
   }

  /**
   * Returns the machine name
   * application id  = application name + "_" + machine name
   */
  static String getMachNm(String appID) {
    StringBuffer newStr = new StringBuffer();
    char ch;
    int strLen = appID.length();

    for (int i=0; i<strLen; i++) {
      ch = appID.charAt(i);
      if (ch == '_'){
        int j = i+1;
        for(; j<strLen; j++){
          ch = appID.charAt(j);
          newStr.append(ch);
        }
        return newStr.toString();
      }
    }
    return null;
   }


}