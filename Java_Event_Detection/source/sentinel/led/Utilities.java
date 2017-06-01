package sentinel.led;

import java.lang.reflect.Method;
import java.io.*;
import java.util.Properties;

import java.net.*;
import java.util.StringTokenizer;



/** This class contains some general purpose methods that are used by the
 *  ECAAgent. Some of these methods are public and may be used by user applications.
 */
public class Utilities {
  static boolean Debug = DebuggingHelper.isDebugFlagTrue("Debug");

  static Properties props = null;

  static boolean isDebugFlagTrue(String debugFlag) {
    if (props == null) {
      props = new Properties();
      try {
        FileInputStream in = new FileInputStream("properties.txt");
        props.load(in);
      }
      catch (FileNotFoundException fnfe) {
    //    System.out.println("Utilities:Properties file not found");
    //    fnfe.printStackTrace();
      }
      catch (IOException ioe) {
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

  static String remWhiteSpaces(String str) {
    StringBuffer newStr = new StringBuffer();
    char ch;
    int strLen = str.length();

    for (int i=0; i<strLen; i++) {
      ch = str.charAt(i);
      if (ch != ' ' && ch != '\t' && ch != '\b' && ch != '\r' && ch != '\n')
         newStr.append(ch);
      }
      return newStr.toString();
   }

   public static void sleepFor(int millis) {
      System.out.println("Sleeping for " + millis/1000 + " seconds ...");
      try {
         Thread.sleep(millis);
      }
      catch (InterruptedException e) {
         e.printStackTrace();
         System.out.println("\nInterruptedExeception caught in sleep");
      }
   }

   /** This method returns a reference to a Java Method Object given the class name,
    *  method name and the name of the method parameter.
    */
   public static Method getMethodObject(String declaringClassName,
                                        String methodName,String param) {
      Class reflectedClass;
      Method method;
      Class[] formalParams = new Class[1];

      try {
         reflectedClass = Class.forName(declaringClassName);
         formalParams[0] = Class.forName(param);
      }
      catch (ClassNotFoundException e) {
         e.printStackTrace();
         return null;
      }
      try {
         method = reflectedClass.getDeclaredMethod(methodName,formalParams);
      }
      catch (NoSuchMethodException e) {
         e.printStackTrace();
         return null;
      }
      return method;
   }

   /** This method returns a reference to a Java Method Object given the class name,
    *  method name and the parameters of the method.
    */
   public static Method getMethodObject(String declaringClassName,
                                        String methodName,Class[] formalParams) {
      Class reflectedClass;
      Method method;

	  try {
         reflectedClass = Class.forName(declaringClassName);
      }
      catch (ClassNotFoundException e) {
         e.printStackTrace();
         return null;
      }
      try {
         method = reflectedClass.getDeclaredMethod(methodName,formalParams);
      }
      catch (NoSuchMethodException e) {
         e.printStackTrace();
         return null;
      }
      return method;
   }

   public static void main(String[] args) {

   System.out.println("Host name "+ getHostName());


      String str = " void sell_stock(int, int)";
      System.out.println("str = " + str);
      String newStr = Utilities.remWhiteSpaces(str);
      System.out.println("newStr = " + newStr);

      str = "         void  \nsell_stock(\b\b\nint,\r\t int)";
      System.out.println("str = " + str);
      newStr = Utilities.remWhiteSpaces(str);
      System.out.println("newStr = " + newStr);
   }

  // added by wtanpisu
  // rational : to utilize the rule scheduler, this method is a condition method
  // for commit rule.

  public static boolean True(ListOfParameterLists parameterLists){
    System.out.println("condition of commit rule is always true");
	  return true;
  }
  // added by wtanpisu on 24 Jan 2000

   /** This method is used to obtain host name */

  //<GED>
  public static String getHostName(){
    String hostName ="ERROR in Utilities.getHostName()";
    try{

      InetAddress inetAdd = InetAddress.getLocalHost ();
      hostName = inetAdd.getHostName ();
      return hostName;
    }
    catch(UnknownHostException unknownHostExcptn){
      unknownHostExcptn.printStackTrace ();
    }
    return hostName;
  }
  //<\GED>


  /**
   * This method is used to the read the configuration file ("App.config")
   */

  static void readAppConfig() {

    System.out.println("Start reading the config file 1");

    Properties props = null;
    if (props == null) {
      props = new Properties();
      try{
        FileInputStream in = new FileInputStream("App.config");
  	props.load(in);
      }catch (FileNotFoundException fnfe) {
        fnfe.printStackTrace();
        System.out.println("Can't find the App.config");
        System.out.println("Please create App.config, before running application");
        System.exit(-1);
      }
      catch (IOException ioe) {
		ioe.printStackTrace();
      }
    }


    String scope = props.getProperty ("SCOPE");

    if(scope == null)  {
      System.out.println("ERROR !!! Check the SCOPE property in App.config");
      System.out.println("Add \"SCOPE LOCAL\" or \"SCOPE GLOBAL\" in to the file");
      System.exit(-1);
    }


  /* If scope is global, keep the properties into variables in CONSTANT class */

    if(scope.equals ("GLOBAL")){
      if(Debug)
        System.out.println("SCOPE :: GLOBAL");

      Constant.SCOPE = props.getProperty("SCOPE");
      String  gedURL = props.getProperty("GED_URL");
      String appNm = props.getProperty("APP_NAME");
      String vmcURL = props.getProperty("APP_URL");
      String gedName = props.getProperty("GED_NAME");
      String ruleSchFlag = props.getProperty("RULE_SCHEDULER");
   //   String gedPort = props.getProperty("PORT");

      if(ruleSchFlag != null &&
            (ruleSchFlag.equalsIgnoreCase("ON") ||ruleSchFlag.equalsIgnoreCase("OFF"))){

        if(ruleSchFlag.equalsIgnoreCase("ON"))
          Constant.RSCHEDULERFLAG = true;
        else if(ruleSchFlag.equalsIgnoreCase("OFF")){
          Constant.RSCHEDULERFLAG = false;
          if(Debug)
            System.out.println("RULE_SCHEDULER :: "+ Constant.RSCHEDULERFLAG);
        }else{
          System.err.println("RULE_SCHEDULER in App.config is not set properly");
          System.err.println("To turn on rule scheduler set  RULE_SCHEDULER ON");
          System.err.println("To turn off rule scheduler set  RULE_SCHEDULER OFF");
          System.exit(-1);
        }

        if(gedURL == null){
          System.err.println("GED_URL in App.config is not set properly");
          System.err.println("If the GED runs on 129.107.12.241, plese set as follows");
          System.err.println("GED_URL 129.107.12.241");
          System.exit(-1);
        }else{
          Constant.GED_URL = "rmi://"+gedURL +"/";;
          if(Debug)
            System.out.println("GED_URL :: "+ Constant.GED_URL);
        }

        if(gedName == null){
          System.err.println("GED_NAME in App.config is not set properly");
          System.err.println("Please set the GED_NAME ");
          System.exit(-1);
        }else{
          Constant.GED_NAME = gedName;
          if(Debug)
            System.out.println("GED_NAME :: "+ Constant.GED_NAME);
        }


        if(appNm == null){
          System.err.println("APP_NAME in App.config is not set properly");
          System.err.println("If your application name is consumer, plese set as follows");
          System.err.println("APP_NAME  consumer");
          System.exit(-1);
        }else{
          Constant.APP_NAME =  appNm;
          if(Debug)
            System.out.println("APP_NAME :: "+ Constant.APP_NAME);
        }

        Constant.APP_URL = getHostName();
/*      if ( vmcURL == null){
        System.err.println("APP_URL in App.config is not set properly");
        System.err.println("If your virtual machine name is newdelhi, plese set as follows");
        System.err.println("APP_URL newdelhi");
        System.exit(-1);
      }else{
        Constant.APP_URL = vmcURL ;
        if(Debug)
          System.out.println("APP_URL :: "+ Constant.APP_URL);
      }*/

        Constant.APP_ID =  appNm+"_"+ Constant.APP_URL;
        /* Set the applicatio name */
        //  Constant.APP_ID =  appNm+"_"+vmcURL;

   /*   if ( gedPort == null){
        System.err.println("PORT in App.config is not set properly");
        System.exit(-1);
      }else{
        Constant.GED_PORT = gedPort ;
        if(Debug)
          System.out.println("PORT :: "+ Constant.GED_PORT);
      }*/
      }
    }else if(props.getProperty ("SCOPE").equals ("LOCAL")){
      Constant.SCOPE = props.getProperty("SCOPE");
      String ruleSchFlag = props.getProperty("RULE_SCHEDULER");


      if(ruleSchFlag != null &&
            (ruleSchFlag.equalsIgnoreCase("ON") ||ruleSchFlag.equalsIgnoreCase("OFF"))){

        if(ruleSchFlag.equalsIgnoreCase("ON"))
          Constant.RSCHEDULERFLAG = true;
        else if(ruleSchFlag.equalsIgnoreCase("OFF"))
          Constant.RSCHEDULERFLAG = false;
        if(Debug)
          System.out.println("RULE_SCHEDULER :: "+ Constant.RSCHEDULERFLAG);
      }else{
        System.err.println("RULE_SCHEDULER in App.config is not set properly");
        System.err.println("To turn on rule scheduler set  RULE_SCHEDULER ON");
        System.err.println("To turn off rule scheduler set  RULE_SCHEDULER OFF");
        System.exit(-1);
      }



      if(Debug)
        System.out.println("SCOPE :: LOCAL");
   }else{
        System.out.println("Check the SCOPE property in App.config");
      System.out.println("OPTION :: LOCAL and GLOBAL ");
      System.exit(-1);
    }


    if (Debug)
      System.out.println("\n---------------- CLOSE Configuration File-------------------\n");
  }


}





