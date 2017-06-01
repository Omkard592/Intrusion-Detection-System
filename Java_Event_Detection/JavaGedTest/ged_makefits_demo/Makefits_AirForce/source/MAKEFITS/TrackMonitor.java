package MAKEFITS;

//import Sentinel.*;
import sentinel.led.*;
import sentinel.ged.*;
import sentinel.comm.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.Vector;
import javax.swing.*;


public class TrackMonitor {


  /**
   * setupData contains the setup data to define the event specification.
   *  The time associated with the event can vary from one interval to another interval.
   *  e.g. you can set SHOW_TRACK_EVERY_X_MINS = 0 hrs 0 min 30 sec
   */

  static Vector configData = TrackUtilities.readInputData("MAKEFITS/setup.txt");

  static boolean x = TrackUtilities.readConfig(configData);

  /** year of the data */
  static String year = Constant.YEAR;

  static String formatTime;

  // Track Display GUI
  static JFrame WCoastFrm;
  static Graph graph;

  /** debug purpose */
  static boolean debug = DebugHelper.isDebugFlagTrue("debug");


  /** This vector contains a list of registered track in the system*/
  static public Vector listOfTracks = new Vector();

  // write a small test routine that counts the number of
  // tokens per line.  If the number counted equals
  // the number that is stored in elementsPerLine
  // than report it otherwise report an error.

  /** The hashtable map the track number with track object */
  static  public Hashtable  trackNumberTrackObject = new Hashtable();

  /** Default Constructor */
  public TrackMonitor(){}

  public Object switchObj = new Object();



  /**Main method*/
  public static void main(String[] args){



    if(args.length != 2){
      System.out.println("USAGE MAKEFITS/TrackMonitor -getLines inputFileName");
    }


    TrackMonitor m = new TrackMonitor();     // dummy instance
    ECAAgent myAgent = ECAAgent.initializeECAAgent();



    /*   Track Graph Panel Instantiation */
    int xLong = 500;
    int yLat = 334;

    WCoastFrm = new MonitoringAreaFrm();



    WCoastFrm.setSize(700,334);


    graph = Track.graph;
    graph.setSize(xLong,yLat);

    // Add JTabbedPanes
    ((MonitoringAreaFrm)WCoastFrm).addMonitoringMap((JPanel)graph);
    ((MonitoringAreaFrm)WCoastFrm).addLogger(Track.analystLogger,"Alert");
    ((MonitoringAreaFrm)WCoastFrm).addLogger(Track.eventLogger,"Track Info");

    WCoastFrm.setLocation(425,0);
    graph.init();
    graph.validate();
    graph.start();

     WCoastFrm.show();

    /* Comment  a.panel.setTrackHistory() out, if you don't want to show history
     * Comment  a.panel.setTrackHistory() out, if you want to show history
     */
    // a.panel.setTrackHistory();
    graph.panel.unsetTrackHistory();


    Vector vEvents = new Vector();

    /** Read input data from file  */
    vEvents = TrackUtilities.readInputData(args[1]);

    /** Show the input file */
    TrackUtilities.showLines(vEvents);

    /** Manipulate the data input system */
    getTokens(vEvents);



    /* Ending the application */
    DataInputStream reader = new DataInputStream(System.in);
    int ch = '0';

    try {
      ch = reader.read();
    }catch(IOException ie){
      ie.printStackTrace();
    }

    System.exit(0);
    return;
}



  /** The getTokens method is used to read the input track data from the input file
   */

  public static void getTokens(Vector vEvents){

    /** Type of data 1) POS 2) CTC */
    String dataType = null;

    /** A line of input data */
    String textLine = null;

    /** Next line of input data */
    String nextTextLine = null;

    /** Tokens of next line input data */
    String[] nextTextLineAttri;

    StringTokenizer st = null;

    String trackNumber = null ;
    String amityType = null;
    String trackType = null;
    Track newTrack = null;

    Time reportT = null;

    // Since there is track number in POS line, after reading CTC we need
    // to keep track of track number so that when we read POS line we can
    // refer to the right track

    Track currentTrack = null ;

    /** Number of the records in input file */
    int recordNumber = 1;

    Time virtualCurrentTime =null;

    /** Reading the input data line by line */
    for(int jj = 0; jj < vEvents.size(); jj++){

      textLine = (String) vEvents.elementAt(jj);

      /** Skip the blank line*/
      if(textLine.length()!= 0){
        String[] attri = TrackUtilities.parseInput(textLine);

        if(debug)
          System.out.println("----------- RECORD "+ (recordNumber++) +" -------------------------");


        /** if the line contains CTC data set*/
        if(attri[0].equals ("CTC")){
          if(debug)
            System.out.println("CTC");

          nextTextLine = (String) vEvents.elementAt(jj+1);

          // If there is a empty line between the records
          int k = jj+2;
          while(nextTextLine.length () == 0)  {
            nextTextLine = (String) vEvents.elementAt(k++);
          }

          nextTextLineAttri = TrackUtilities.parseInput(nextTextLine);

         // get time
          reportT = new Time(getTimeStamp(nextTextLineAttri));
          if(jj == 0){
            virtualCurrentTime = reportT;
          }

          long pause = reportT.getMillis() - virtualCurrentTime.getMillis();
          if(debug)
            System.out.println("The event will occur after " + pause/1000 + " seconds.");
          TrackUtilities.sleep(pause);
          virtualCurrentTime = reportT;
        }

        /** if the line contains POS data set*/
        else if(attri[0].equals ("POS")){

          reportT = new Time(getTimeStamp(attri));
          long pause = reportT.getMillis() - virtualCurrentTime.getMillis();
          if(debug)
            System.out.println("The event will occur after " + pause/1000 + " seconds.");

          TrackUtilities.sleep(pause);
          virtualCurrentTime = reportT;
        }


        dataType = attri[0];
        if(dataType.equals("CTC")){

          trackNumber = attri[1]; // track number
          amityType = attri[3];    // amity type
          trackType  = attri[4];  // track type
          System.out.println("New Track number "+trackNumber);
//          System.out.println("Track type "+trackType);

          // In case there are more attribute
          // Check whether the nextToken is "" or not
          // If yes, ignore it
          // If no, set track attribute
          // If the track data already existed

          // RJY
          currentTrack = (Track)trackNumberTrackObject.get(trackNumber);
        }


        /* When currentTrack is null, this mean no record about this current track
         * in the system.
         */

        if(currentTrack == null){

          // instantiate the new track object
          if(debug)
            System.out.println("Create new track");

          /** Add new track into the map */
          newTrack = new Track(trackNumber);
          newTrack.setAmityType(amityType);
          graph.addNode(trackNumber,new Latitude(0, 0,"N"),new Longitude(0, 0,"W"), trackType);

          // RJY
          currentTrack = newTrack;

          createRules(newTrack,trackNumber);

          /** Keep the new track our record table*/
          listOfTracks.addElement(newTrack);
          trackNumberTrackObject.put(trackNumber,newTrack);
        }

        /** Update the location of the existing track */
        if(dataType.equals("POS")){
          updateLoc(attri,currentTrack,reportT);
        }
      }
    }

    System.out.println("\nTYPE and key to END");
    DataInputStream reader = new DataInputStream(System.in);
    int ch = '0';
    try {
      ch = reader.read();
    }
    catch(IOException ie) {
      ie.printStackTrace();
    }
    System.out.println("\n*** END of LED application ***\n");System.exit(0);
  }


  public static String getTimeStamp(String[] nextTextLineAttri){

    /** Read time data from the file*/
    String dd = nextTextLineAttri[1].substring (0,2);
    String hh = nextTextLineAttri[1].substring (2,4);
    String mm = nextTextLineAttri[1].substring (4,6); // minute

    char checkSum;
    String sec;

    if(nextTextLineAttri[1].charAt(6) =='z' || nextTextLineAttri[1].charAt(6) =='Z'){
      sec = "00";
      checkSum = nextTextLineAttri[1].charAt (7);
    } else{
      sec = nextTextLineAttri[1].substring (6,8);
      checkSum = nextTextLineAttri[1].charAt (9);
    }

    String mon = nextTextLineAttri[2]; // month
    String mo = TrackUtilities.monValue(mon);  // return integer value of month


    //  String formatTime = hh+":"+mm+":"+"00"+"/"+"mo"+"/"+dd+"/"+year;
    formatTime = hh+":"+mm+":"+sec+"/"+mo+"/"+dd+"/"+year;

    if(debug)
      System.out.println("Time stamp is "+formatTime);
    return formatTime;
  }

  public static void createRules(Track newTrack, String trackNumber){
    //**************** Start creating rules for this new track *********/

    // binding the events and rules for each instance

    // ECA Rule 4
    // Now apply to only track number T7005
//    if(trackNumber.equals ("T7005")){
      // System.out.println("Apply rule 4 to T7005");
      createRule4(newTrack);
//    }

//    if(trackNumber.equals ("T7001") ||trackNumber.equals ("T7002")||trackNumber.equals ("T7010")){
      // ECA Rule 5
      createRule5(newTrack);

      // ECA Rule 6
      newTrack.createNotUpdtBetwT1T2Event();

      // ECA Rule 7
      createRule7(newTrack);

      // ECA Rule 11
      newTrack.createAfterTrackEnterXMinEvent()  ;
      newTrack.createAfterTrackEnterXMinRule() ;
      newTrack.beginCountDown(); // start monitoring the update

      // add the track into the list and hashtable
      // so that you can refer it later on
  //  }
    //************** Finish creating rules for this new track *********/
  }


  public static void updateLoc(String[] attri,Track currentTrack, Time reportT){
    String latRaw = attri[3]; //latitude raw data
    String lonRaw = attri[4]; //longitude raw data
    Latitude tLatitude = new Latitude(Integer.parseInt(latRaw.substring(0,2)),Integer.parseInt(latRaw.substring(2,4)),latRaw.substring(4,5));
    Longitude tLongitude = new Longitude(Integer.parseInt(lonRaw.substring(0,3)),Integer.parseInt(lonRaw.substring(3,5)),lonRaw.substring(5,6));

    if(debug)
      System.out.println("UPDATE location"+latRaw+lonRaw);
    currentTrack.setReportTime (reportT);
    currentTrack.setLatitudeLongitude (tLatitude,tLongitude);
    if(debug)
      System.out.println("new lat long"+currentTrack.getLatitudeValue()+"    " +currentTrack.getLongitudeValue());

    graph.changeLocation(currentTrack.getTrackNumber(), tLatitude, tLongitude);
  }

  public static void createRule4(Track newTrack){
    newTrack.createStartDetect()  ;
    newTrack.createEndDetect() ;
    newTrack.createShowTrackStatusEveryMinEvent();
    newTrack.createshowStatusRule();
    newTrack.startDetect ();
  }

  public static void createRule5(Track newTrack){
    newTrack.createReportLocationEvent();
    newTrack.createNotReportLocationInXMinEvent ();
    newTrack.createNotReportInXMinRule () ;
  }


  public static void createRule7(Track newTrack){
    // ECA Rule 7
    newTrack.createInZoneEvent() ;
    newTrack.createOutOfZoneEvent();
    newTrack.createInZoneOrOutOfZoneRule();
    newTrack.createTrackEnterEvent()   ;
    newTrack.createTrackLeaveEvent() ;
 //   newTrack.createInZoneRule();
    newTrack.createEnterZoneRule() ;
    newTrack.createLeaveZoneRule();
  }

  public static void startSimulation(ListOfParameterLists parameterLists){


  //  EventHandle evntHndl =   myAgent.createPrimitiveEvent("StartTrackEvent","MAKEFITS.TrackMonitor","startEvent","control","myhome");
  //  myAgent.createRule("StartTrackEvent",evntHndl,"MAKEFITS.TrackMonitor.TRUE","MAKEFITS.TrackMonitor.startSimulation");

  ///    notifyAll();
  }

}



