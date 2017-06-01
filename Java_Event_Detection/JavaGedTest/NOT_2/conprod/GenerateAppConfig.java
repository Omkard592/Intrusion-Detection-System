import java.net.*;
import java.io.*;

public class GenerateAppConfig {


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

  public static String getIPAddress(){
    String ip ="ERROR in Utilities.getHostName()";
    try{

      InetAddress inetAdd = InetAddress.getLocalHost ();
      ip = inetAdd.getHostAddress();
      return ip;
    }
    catch(UnknownHostException unknownHostExcptn){
      unknownHostExcptn.printStackTrace ();
    }
    return ip;
  }

  public static void main(String[] args){
    getIPAddress();
    try{
      File configFile = new File("App.config");
      if(configFile.createNewFile())
        System.out.println("Created the App.Config file");

      FileWriter fw = new FileWriter(configFile);
      BufferedWriter bw = new BufferedWriter(fw);

      String inputText = "SCOPE GLOBAL";
      bw.write(inputText,0,inputText.length());
      bw.newLine();
      inputText = "RULE_SCHEDULER ON";
      bw.write(inputText,0,inputText.length());
      bw.newLine();
      inputText = "GED_URL "+getIPAddress();
      bw.write(inputText,0,inputText.length());
      bw.newLine();
      inputText = "GED_NAME ged1";
      bw.write(inputText,0,inputText.length());
      bw.newLine();
      inputText = "APP_NAME conprod";
      bw.write(inputText,0,inputText.length());
      bw.newLine();
      bw.flush();
      bw.close();
      fw.close();
    }catch(IOException ie){ie.printStackTrace();}
  }

/*
  public static void main(String[] args){
    getIPAddress();
    try{
      File configFile = new File("Global.config");
      if(configFile.createNewFile())
        System.out.println("Created the Global.Config file");

      FileWriter fw = new FileWriter(configFile);
      BufferedWriter bw = new BufferedWriter(fw);

      String inputText = "BEGIN";
      bw.write(inputText,0,inputText.length());
      bw.newLine();
      inputText = "MAPPING consumer_newdelhi consumer_"+getHostName();
      bw.write(inputText,0,inputText.length());
      bw.newLine();
      inputText = "MAPPING producer_newdelhi producer_"+getHostName();
      bw.write(inputText,0,inputText.length());
      bw.newLine();
      inputText = "MAPPING conprod_newdelhi conprod_"+getHostName();
      bw.write(inputText,0,inputText.length());
      bw.newLine();
      inputText = "MAPPING airTrackApp_myhome airTrackApp_"+getHostName();
      bw.write(inputText,0,inputText.length());
      bw.newLine();
      inputText = "MAPPING shipTrackApp_myhome shipTrackApp_"+getHostName();
      bw.write(inputText,0,inputText.length());
      bw.newLine();
      inputText = "MAPPING control_myhome control_"+getHostName();
      bw.write(inputText,0,inputText.length());
      bw.newLine();
      inputText = "GED_NAME ged1";
      bw.write(inputText,0,inputText.length());
      bw.newLine();
      inputText = "END";
      bw.write(inputText,0,inputText.length());
      bw.newLine();
      bw.flush();

      bw.close();
      fw.close();
    }catch(IOException ie){ie.printStackTrace();}
  }*/
}





