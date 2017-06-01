// Track.java

package MAKEFITS;

import java.util.Hashtable;
import java.util.Vector;
import java.awt.*;
import java.io.*;
import sentinel.led.*;
import sentinel.ged.*;
import sentinel.comm.*;
//import Sentinel.*;

public class Track {

  static boolean debug = DebugHelper.isDebugFlagTrue("debug");

  public static TrackLogger eventLogger = new TrackLogger();
  public static TrackLogger analystLogger = new TrackLogger();


  private String start =  "yes";  // for enter and leave rule
  private boolean isInZone = false;  // for enter and leave rule

  /**
   * Area of zoneA is between logitude -50 to logitude +50 and latitude -50 to latitude +50
   */

//  static RecAOI zoneA = new RecAOI("AOI", new Latitude(30,0,"N"),new Longitude(120,0,"W"),10,10);

  static RecAOI zoneA = new RecAOI("AOI", new Latitude(30,0,"N"),new Longitude(120,0,"W"),10,10);


//  3015N2/12049W6
  private String trackNumber;   //eg. 0001 for Track 0001
  private Longitude trackLon; // longitude value of a track
  private Latitude trackLat ;  // latitude value of a track
  private Time reportTimeStmp; // report time stamp

  static Graph graph = new Graph();


  static EventHandle AnyEnterZone;
  /** Class Level Event
   *  When any track get into the AOI zone
   */
  static{
     AnyEnterZone = ( EventHandle) ECAAgent.getDefaultECAAgent().createPrimitiveEvent(
                    "AnyEnterZone",
                    "MAKEFITS.Track",
                    EventModifier.END,
                    "void AnyEnter()");
     ECAAgent.getDefaultECAAgent().createRule("AnyEnterZoneRule",AnyEnterZone,"MAKEFITS.Track.TRUE","MAKEFITS.Track.TEST");
  }

  public static void AnyEnter(){
      EventHandle[] e = ECAAgent.getEventHandles("AnyEnterZone");
      ECAAgent.getDefaultECAAgent().raiseEndEvent(e,null);
  }


  // POS set attributes

  String dateTime;            // 1
  String month;               // 2
  String latitude;            // 3
  String longitude;           // 4
  String senser;              // 5
  String bearingOfMajAxis;    // 6
  String lenOfSemiMajAxis;    // 7
  String lenOfSemiMinAxis;    // 8
  String course;              // 9
  //String speed;               // 10
  String altiDepth;           // 11
  String RDF_RF;              // 12
  String spare;               // 13
  String sourceCode;          // 14
  String sequential;          // 15
  String photos;              // 16
  String noOfContacts;        // 17

  // CTC set attributes

  String trackNum;          // 1
  String classNum;          // 2
  String trademark;         //3
  String type;              //4
  String category;          //5
  String pannantNum;        //6
  // String flag;              //7
  String SCONUM;            //8
  String slectiveID;        //9
  String alertCode;         //10
  String forceCode;         //11
  String sysTrackNum;       //12
  String trackType;         //13
  String avgSpeed;          //14
  String avgTime;           //15
  String discreteID;        //16
  String UID;               //17
  String interRadioCallSgn; //18
  String suspicionCode;     //19
  String emitterVceCallSgn; //20

  // other potential attributes in track object

  private String className; // class name
  private float speed;
  private String TrackType;
  private String PennantNumberO;
  private String flag;
  private String SCONUMB;
  private String selectiveID ;
  private String name=trackNumber;


  public Track (){}


  public Track (String trackNumber){
    this.trackNumber = trackNumber;
    this.name = trackNumber+"";


    createBeginCountDownEvent(trackNumber) ;
  }

  // Rule 4

  private String    endDetectEventName ;
  private EventHandle    endDetectEvent ;

  private String    startDetectEventName ;
  private EventHandle    startDetectEvent ;

  private String     showTrackStatusEveryMinEventName;
  private EventHandle showTrackStatusEveryMinEvent ;

  // Rule 5
  private EventHandle notReportInXMin;
  private String notReportInXMinEventName;

  // Rule 6
  private EventHandle notUpdtBetwT1T2;
  private String notUpdtBetwT1T2EventName;

  // Rule 7

  private EventHandle  inZone;    // inZone event is raised when the track is in the defined area
  private String  inZoneEventName ;

  private EventHandle outOfZone;    // inZone event is raised when the track is out of the defined area
  private String outOfZoneEventName  ;

  private EventHandle  enterZone ;    // inZone event is raised when the track enters in the defined area
  private String  enterEventName ;

  private EventHandle leaveZone;    // inZone event is raised when the track leaves in the defined area
  private String   leaveEventName ;

  // Rule 11

  private EventHandle  afterTrackEnterXMin;
  private String   afterTrackEnterXMinEventName;


  // explicit event handler

  private EventHandle beginCountDn;
  private String beginCountDnEventName;


  private EventHandle reportLocation ;
  private String reportLocationEventName;

  /**
  *   event specification
  */

  /**
  * Event handler for Rule 4
  */

  public EventHandle createStartDetect(){

    startDetectEventName = name+"_startDetectEventName";

    startDetectEvent =  ECAAgent.getDefaultECAAgent().createPrimitiveEvent(
    startDetectEventName,              // Event name
    "MAKEFITS.Track",                     // class Name
    EventModifier.END,                    // Event Modifier
    "void startDetect()",      // Method signature
    this);                                // Instance (track1, track2,...,or trackN)
    return (PrimitiveEventHandle)  startDetectEvent;
  }

  public EventHandle createEndDetect(){

    endDetectEventName = name+"_endDetectEventName";

    endDetectEvent =  ECAAgent.getDefaultECAAgent().createPrimitiveEvent(
   endDetectEventName,              // Event name
    "MAKEFITS.Track",                     // class Name
    EventModifier.END,                    // Event Modifier
    "void endDetect()",      // Method signature
    this);                                // Instance (track1, track2,...,or trackN)
    return (PrimitiveEventHandle)  endDetectEvent;
  }


  public void createshowStatusRule(){
    ECAAgent.getDefaultECAAgent().createRule("showStatusRule_"+name, showTrackStatusEveryMinEvent,"MAKEFITS.Track.C_trueCond","MAKEFITS.Track.A_showTrackStatus");
  }

  public void startDetect(){
    // Start showing tracks' status every Y minutes

    EventHandle[] start = ECAAgent.getEventHandles(startDetectEventName);
    ECAAgent.raiseEndEvent(start,this);
  }



  public EventHandle createShowTrackStatusEveryMinEvent(){

    showTrackStatusEveryMinEventName = name+"_showTrackStatusEveryYMinutes";
    showTrackStatusEveryMinEvent = ECAAgent.getDefaultECAAgent().createCompositeEvent(EventType.PERIODIC,
    showTrackStatusEveryMinEventName,
    startDetectEvent,Constant.SHOW_TRACK_EVERY_X_MINS,
    endDetectEvent);
    return     showTrackStatusEveryMinEvent;
  }

  // Event handler for Rule 5

  // change location
  public EventHandle createReportLocationEvent(){
    reportLocationEventName = name+"_reportLocactionEvnt";
    reportLocation =    ECAAgent.getDefaultECAAgent().createPrimitiveEvent(
    reportLocationEventName,              // Event name
    "MAKEFITS.Track",                     // class Name
    EventModifier.END,                    // Event Modifier
    "void setLatitudeLongitude(Latitude,Longitude)",      // Method signature
    this);                                // Instance (track1, track2,...,or trackN)
    return (PrimitiveEventHandle) reportLocation;
  }



  public EventHandle createNotReportLocationInXMinEvent(){
    this.notReportInXMinEventName   = name+"_notUpdateInXMinEventName";
    notReportInXMin = ( EventHandle) ECAAgent.getDefaultECAAgent().createCompositeEvent(
                                EventType.NOT,           // Event Type
                                notReportInXMinEventName, // Event Name
                                beginCountDn,           // Left Event
                                reportLocation,          // Middle Event
    (EventHandle) ECAAgent.getDefaultECAAgent().createCompositeEvent(EventType.PLUS,
                                      "XminAfterBeginCountDn"+notReportInXMinEventName,
                                      // PLUS event name (adding "+ eventName" to avoid duplicate event name
                                       beginCountDn,
                                      Constant.NO_CHANGE_IN_LOCATION_FOR_X_MINS));
    return notReportInXMin;
  }

  public void TEST(ListOfParameterLists parameterLists){
    System.out.println("TEST");
      System.out.println("TEST");
        System.out.println("TEST");
          System.out.println("TEST");
            System.out.println("TEST");
              System.out.println("TEST");
  }

  public boolean TRUE(ListOfParameterLists parameterLists){
    return true;

  }

  // not update between T1 T2
  // Event handler for Rule 6

 private EventHandle referencePointEvent;

  public EventHandle  createNotUpdtBetwT1T2Event(){


    String   referencePointEventName = name+"_referencePoint";
    referencePointEvent =   (EventHandle) ECAAgent.getDefaultECAAgent().createPrimitiveEvent(
    referencePointEventName,              // Event name
    "MAKEFITS.Track",
    EventModifier.END,
    "void referencePoint()",    // virtual event T1
    this
    );


    String   notUpdtBetwT1T2BeginEventName = name+"_notUpdtBetwT1T2BeginEvent";
    EventHandle notUpdtBetwT1T2Begin =   (EventHandle) ECAAgent.getDefaultECAAgent().createCompositeEvent(
           EventType.PLUS,
    notUpdtBetwT1T2BeginEventName,              // Event name
     referencePointEvent,
          "0 hrs 0 min 30 sec"
    );


    String notUpdtBetwT1T2EndEventName = name+"_notUpdtBetwT1T2EndEvent";
    EventHandle notUpdtBetwT1T2End =    ECAAgent.getDefaultECAAgent().createCompositeEvent(
           EventType.PLUS,
    notUpdtBetwT1T2EndEventName,              // Event name
     referencePointEvent,
          "0 hrs 0 min 40 sec"

    );



    notUpdtBetwT1T2EventName  = name+"_notUpdtBetwT1T2Event";
    notUpdtBetwT1T2 = ( EventHandle) ECAAgent.getDefaultECAAgent().createCompositeEvent(
    EventType.NOT,           // Event Type
    notUpdtBetwT1T2EventName, // Event Name
    notUpdtBetwT1T2Begin,           // Left Event
    reportLocation,          // Middle Event
    notUpdtBetwT1T2End);     // Right Event

    createNotUpdtBetwT1T2Rule();

    EventHandle[] t1 = ECAAgent.getEventHandles(referencePointEventName);

   ECAAgent.getDefaultECAAgent().createRule("T1 of "+name,notUpdtBetwT1T2Begin,"MAKEFITS.Track.C_trueCond","MAKEFITS.Track.A_T1");
   ECAAgent.getDefaultECAAgent().createRule("T2 of "+name,notUpdtBetwT1T2End,"MAKEFITS.Track.C_trueCond","MAKEFITS.Track.A_T2");

   ECAAgent.getDefaultECAAgent().raiseEndEvent(t1,this);


    return notUpdtBetwT1T2;
  }



  // Event handler for Rule 7

  /**
   *  First, the track that is in a specified zone (area) causes inZoneEvent.
   *  Then,if that track is out of zone causes outOfZoneEvent.
   *  This implies the leave event (Rule7).
   *
   *  In the other way, outOfZoneEvent + inZoneEvent ==> the enter event (Rule7)
   */


  // inZone event is raised when the track is in the defined area
  public EventHandle createInZoneEvent (){

    this.inZoneEventName = name+"_inZoneEvent";
    inZone =(EventHandle) ECAAgent.getDefaultECAAgent().createPrimitiveEvent(
    inZoneEventName ,    // Event name
    "MAKEFITS.Track",                   // Class name
    EventModifier.END,         // Event Modifier
    "void inZoneSignal()",    // Method Signature
    this);                     // Intance (track1, track2,...,or trackN)

    return inZone;
  }

  // TEST
//  public void createInZoneRule() {

//    ECAAgent.getDefaultECAAgent().createRule("InZoneRule_"+name,inZone,"MAKEFITS.Track.TRUE","MAKEFITS.Track.TEST");
//  }

  // TEST

  // inZone event is raised when the track is out of the defined area
  public EventHandle createOutOfZoneEvent (){

    this.outOfZoneEventName = name+"_outOfZoneEvent&^&";
    outOfZone =(EventHandle) ECAAgent.getDefaultECAAgent().createPrimitiveEvent(
    outOfZoneEventName,                 // Event name
    "MAKEFITS.Track",          // Class name
    EventModifier.END,         // Event Modifier
    "void outZoneSignal()",    // Method Signature
    this);                     // Intance (track1, track2,...,or trackN)

    return outOfZone;
  }





  public EventHandle createTrackEnterEvent(){

    this.enterEventName   = name+"_enterZoneEvent";
    enterZone = ( EventHandle) ECAAgent.getDefaultECAAgent().createCompositeEvent(
    EventType.SEQ,           // Event Type
    enterEventName,          // Event Name
    outOfZone,           // Left Event
    inZone          // Right Event
    );
    return enterZone;
  }

  public EventHandle createTrackLeaveEvent(){

    this.leaveEventName   = name+"_leaveZoneEvent";
    leaveZone = ( EventHandle) ECAAgent.getDefaultECAAgent().createCompositeEvent(
    EventType.SEQ,           // Event Type
    leaveEventName ,       // Event Name
    inZone ,        // Left Event
    outOfZone );    // Right Event
    return leaveZone;

  }





   // Event handler for Rule 11

  public EventHandle createAfterTrackEnterXMinEvent(){
    this.afterTrackEnterXMinEventName = name+"_afterTrackEnterXMinEvent";
    afterTrackEnterXMin =(EventHandle) ECAAgent.getDefaultECAAgent().createCompositeEvent(
    EventType.AND,
    afterTrackEnterXMinEventName ,    // Event name
    enterZone,
    (EventHandle) ECAAgent.getDefaultECAAgent().createCompositeEvent(EventType.PLUS,
    "XminAfterEntrance"+afterTrackEnterXMinEventName, // PLUS event name (adding "+ eventName" to avoid duplicate event name

    enterZone,
    Constant.AFTER_TRACK_ENTER_X_MINS));
    return afterTrackEnterXMin;
  }


  // Explicit eventhandler (interrogation)
  public EventHandle createBeginCountDownEvent (String name){

    this.beginCountDnEventName = name+"_beginCountDnEvent";
    beginCountDn =(EventHandle) ECAAgent.getDefaultECAAgent().createPrimitiveEvent(
    beginCountDnEventName,    // Event name
    "MAKEFITS.Track",                   // Class name
    EventModifier.END,         // Event Modifier
    "void beginCountDown()",    // Method Signature
    this);                     // Intance (track1, track2,...,or trackN)

    return beginCountDn;
  }


  /**
   * Rule5
   */
  public void createNotReportInXMinRule (){
    ECAAgent.getDefaultECAAgent().createRule("NotReportInXMinRule_"+name,notReportInXMin,"MAKEFITS.Track.C_trueCond","MAKEFITS.Track.A_noChangeInLocation");
  }

  /**
   * Rule6
   */
  public void createNotUpdtBetwT1T2Rule(){
    ECAAgent.getDefaultECAAgent().createRule("NotUpdtBetwT1T2Rule"+name,notUpdtBetwT1T2,"MAKEFITS.Track.C_trueCond","MAKEFITS.Track.A_NotUpdtBetwT1T2");

  }


  /**
   * Rule 7
   * The rule is triggered every time when there is a report of change of location
   * We use this information to find whether the track enter or leave
   */
  public void createInZoneOrOutOfZoneRule(){
    ECAAgent.getDefaultECAAgent().createRule("InZoneOrOutOfZoneRule_"+name,reportLocation,"MAKEFITS.Track.C_checkZone","MAKEFITS.Track.A_trackInZone");
  }




  /*
   * Rule 7
   * The rule is triggered when the track enters the zone.
   */
  public void createEnterZoneRule(){
//      ECAAgent.getDefaultECAAgent().createRule("enterZoneRule_"+name,enterZone,"MAKEFITS.Track.C_enterTheZone","MAKEFITS.Track.A_enterTheZone",1,CouplingMode.IMMEDIATE ,Context.RECENT);
    ECAAgent.getDefaultECAAgent().createRule("enterZoneRule_"+name,enterZone,"MAKEFITS.Track.C_enterTheZone","MAKEFITS.Track.A_enterTheZone",1,CouplingMode.IMMEDIATE ,ParamContext.RECENT);
    //    ECAAgent.getDefaultECAAgent().createRule("enterZoneRule_"+name,enterZone,"MAKEFITS.Track.C_enterTheZone","MAKEFITS.Track.A_enterTheZone",1,CouplingMode.IMMEDIATE ,ParamContext.CHRONICLE);
  }

  /*
   * Rule 7
   * The rule is triggered when the track leaves the zone.
   */
  public void createLeaveZoneRule(){
    ECAAgent.getDefaultECAAgent().createRule("leaveZoneRule_"+name,leaveZone,"MAKEFITS.Track.C_leaveTheZone","MAKEFITS.Track.A_leaveTheZone",1,CouplingMode.IMMEDIATE ,ParamContext.CHRONICLE);
//    ECAAgent.getDefaultECAAgent().createRule("leaveZoneRule_"+name,leaveZone,"MAKEFITS.Track.C_leaveTheZone","MAKEFITS.Track.A_leaveTheZone",1,CouplingMode.IMMEDIATE ,Context.CHRONICLE);
  }


  /*
   * Rule 11
   * The rule is triggered when the track leaves the zone.
   */
  public void createAfterTrackEnterXMinRule(){
 ECAAgent.getDefaultECAAgent().createRule("XMinAfterEntrace_"+name,afterTrackEnterXMin,"MAKEFITS.Track.C_trueCond","MAKEFITS.Track.A_XMinAfterTrackEnter",1,CouplingMode.IMMEDIATE ,ParamContext.RECENT);
//    ECAAgent.getDefaultECAAgent().createRule("XMinAfterEntrace_"+name,afterTrackEnterXMin,"MAKEFITS.Track.C_trueCond","MAKEFITS.Track.A_XMinAfterTrackEnter",1,CouplingMode.IMMEDIATE ,Context.RECENT);
  }


  //**************************************************************************
  //    Conditions and Actions Method
  //**************************************************************************


  public boolean C_trueCond(ListOfParameterLists parameterLists) {
    if(debug)
    System.out.println("Condition: TRUE");
    return true;
  }

  /**
   *  Condition and Action for Rule 4
   */


   public void A_showTrackStatus(ListOfParameterLists parameterLists) {
      // enumerate all tracks in the system


      if(debug)
      System.out.println("\n\nAction_Rule 4: Show all the tracks' status");

      Track.analystLogger.log(" ");
      Track.analystLogger.log(TrackUtilities.printTimeValue(Constant.SHOW_TRACK_EVERY_X_MINS)+" since last check on "+trackNumber);

      Constant.alertOutput.println(" ");
      Constant.alertOutput.println (TrackUtilities.printTimeValue(Constant.SHOW_TRACK_EVERY_X_MINS)+" since last check on "+trackNumber);
      Constant.alertOutput.flush ();
  }


  /**
   *  Condition and Action for Rule 5
   */

  public void A_noChangeInLocation(ListOfParameterLists parameterLists){
     ParameterList paramList = parameterLists.getFirst();
         String ts ="";
    try {
     Track eventInstance = (Track) paramList.getEventInstance();
	    ts = (String)   paramList.getObject("TimeStamp");
      if(debug)
    System.out.println("TEST show the timestamp"+ts);
     }
	  catch (Exception e) {
	  }



        if(debug)
    System.out.println("Action_Rule5 :: No update on track-"+trackNumber+" for 1 minutes.");
    analystLogger.log (" ");
    analystLogger.log ("Alert for "+trackNumber);
    analystLogger.log ("No change in location of "+trackNumber+" for "+TrackUtilities.printTimeValue (Constant.NO_CHANGE_IN_LOCATION_FOR_X_MINS) +" form "+ts);


    Constant.alertOutput.println (" ");
    Constant.alertOutput.println ("Alert for "+trackNumber);
    Constant.alertOutput.println ("No change in location of "+trackNumber+" for "+TrackUtilities.printTimeValue (Constant.NO_CHANGE_IN_LOCATION_FOR_X_MINS) +" form "+ts);
    Constant.alertOutput.flush ();



    // Future Plan for Rule5
    // Add code to alert analyst here
  }

  /**
   *  Action for Rule 6
   */




   public void A_T1(ListOfParameterLists parameterLists){
      PrimitiveEventHandle        t1peh  =   (PrimitiveEventHandle)          referencePointEvent;

      t1peh.insert("T1",(Object)TrackUtilities.getTimeStamp());
  }
   public void A_T2(ListOfParameterLists parameterLists){
   PrimitiveEventHandle        t2peh  =   (PrimitiveEventHandle)          referencePointEvent;

     t2peh.insert("T2",(Object)TrackUtilities.getTimeStamp());

  }


  public void A_NotUpdtBetwT1T2(ListOfParameterLists parameterLists) {

 ParameterList paramList = parameterLists.getFirst();
         String t1 ="";
                  String t2 ="";
    try {
     Track eventInstance = (Track) paramList.getEventInstance();
	    t1 = (String)   paramList.getObject("T1");
      	    t2 = (String)   paramList.getObject("T2");

     }
	  catch (Exception e) {
	  }


           if(debug){
    System.out.println("Action Rule 6");
    System.out.println("No update of between T1 and T2");
    }
    analystLogger.log (" ");
    analystLogger.log ("Alert for "+trackNumber);
    analystLogger.log ("No update on "+trackNumber+" position received between "+t1+" and "+t2+", check "+trackNumber+" status");

    Constant.alertOutput.println (" ");
    Constant.alertOutput.println ("Alert for "+trackNumber);
    Constant.alertOutput.println ("No update on "+trackNumber+" position received between "+t1+" and "+t2+", check "+trackNumber+" status");
    Constant.alertOutput.flush ();



  }



  /**
   *  Condition and Action for Rule7
   */

  public boolean C_enterTheZone(ListOfParameterLists parameterLists){
    //if(isEnter == true){
    if(debug)
    System.out.println("Condition from Rule7 :: is true");


    return true;

    // }
    // else
    //return false;

    // Future work
    // Put the condition code here, eg. if the track is friendly, we
    // don't need to alert the analyst.

  }

  public void A_enterTheZone(ListOfParameterLists parameterLists){
    if(amityType.equalsIgnoreCase("amity")){
      return ;
    }
    AnyEnter();

  if(debug)
    System.out.println("Action from Rule7 :: Alert !!! This track enters"+ trackNumber+" the defined zone");
    analystLogger.log (" ");
    analystLogger.log ("Alert for "+trackNumber);
    analystLogger.log (trackNumber+" is entering the "+"AOI");

    Track.graph.setWarning(trackNumber);

    Constant.alertOutput.println (" ");
    Constant.alertOutput.println ("Alert for "+trackNumber);
    Constant.alertOutput.println (trackNumber+" is entering the "+"AOI");
    Constant.alertOutput.flush ();



    // Future work
    // Put the action code here (for GUI), eg.  show the alert signal on the screen
    //isInZone = true; //
  }

  public String  amityType;
  public void setAmityType(String amityType){
    this.amityType = amityType;
  }
  public boolean C_leaveTheZone(ListOfParameterLists parameterLists){
    // if(isEnter == false){    // track leaves


    if(debug)
    System.out.println("Condition from Rule7 :: is true");
    return true;
    // }
    // else
    // return false;

    // Future work
    // Put the condition code here, eg. if the track is friendly, we
    // don't need to alert the analyst.
  }



  public void A_leaveTheZone(ListOfParameterLists parameterLists){
  if(debug)
    System.out.println("Action from Rule7 :: Alert !!! This track"+ trackNumber +" leaves the defined zone");
    analystLogger.log (" ");
    analystLogger.log ("Alert for "+trackNumber);
    analystLogger.log (trackNumber+" is leaving the AOI ");

    Constant.alertOutput.println (" ");
    Constant.alertOutput.println ("Alert for "+trackNumber);
    Constant.alertOutput.println (trackNumber+" is leaving the AOI ");
    Constant.alertOutput.flush ();

    Track.graph.unsetWarning(trackNumber);

    // isInZone = false;

    // after track leaves, set isEnter to true since it can enter the zone, not leave the zone
    // Future work
    // Put the action code here (for GUI), eg.  show the alert signal on the screen
  }

  public boolean C_checkZone(ListOfParameterLists parameterLists) {

    Latitude  targetLatitude =   trackLat;
    Longitude   targetLongitude =    trackLon;

    if(debug){
    System.out.println("Condition_C_checkZone");
    System.out.println("tLat = " + targetLatitude.getValue() + "tLong = " + targetLongitude.getValue());
    }

    // There might be more than one interesting zone.
    //if ( zoneA.inZone(targetLatitude,targetLongitude) && zoneB.inZone(targetLatitude,targetLongitude)){

    if ( zoneA.inZone(targetLatitude,targetLongitude)){
      if(debug)
      System.out.println("TRUE: It is in the defined boundary");

      if(start == "yes"){
        inZoneSignal();
        isInZone = true;
        start = "no";
      }
      else if(isInZone == true)  // if the track is already in zone, no need to signal inZoneSignal
    {}
      else{
        inZoneSignal();
        isInZone = true;
      }
      return true;
    }
    else {
      if(debug)
      System.out.println("FALSE: It is out of the defined boundary");

      if(start == "yes"){
        if(debug)
        System.out.println("send out of zone signal");
        outZoneSignal();
        isInZone = false;
        start = "no";
      }
      else if(isInZone == false) // This means this track is already out of zone, no need to signal outZoneSignal()
    {}
      else{
        outZoneSignal();
        isInZone = false;
      }
      return false;
    }
  }


  public void A_trackInZone(ListOfParameterLists parameterLists) {}

  /**
   *  Action for Rule11
   */

  public void A_XMinAfterTrackEnter(ListOfParameterLists parameterLists) {

    if(debug){
    System.out.println("Action Rule 11");
    System.out.println("Update the info ....... after this track "+trackNumber+" has entered in zone for 2 min");
    }
    analystLogger.log (" ");
    analystLogger.log ("Alert for "+trackNumber);
    analystLogger.log (TrackUtilities.printTimeValue (Constant.AFTER_TRACK_ENTER_X_MINS )+" after "+trackNumber+" entered the AOI");


    Constant.alertOutput.println (" ");
    Constant.alertOutput.println ("Alert for "+trackNumber);
    Constant.alertOutput.println (TrackUtilities.printTimeValue (Constant.AFTER_TRACK_ENTER_X_MINS )+" after "+trackNumber+" entered the AOI");
    Constant.alertOutput.flush ();

    //Future work
    //Add code here to perform more acition on rule7

  }

  //****************************************************************************
  //                  The events of interest
  //****************************************************************************

  public void setReportTime(Time reportTime){
    this.reportTimeStmp = reportTime;
    return ;
  }


  /**
   * set the track's location
   *
   * @param Time report timeStamp
   * @param Latitude new track's latitude
   * @param Longitude new track's logitude
   */

  public void setLatitudeLongitude(Latitude updateLat,Longitude updateLon){
    trackLat = updateLat;
    trackLon = updateLon;
  //    if(reportLocation != null){
    String out =  trackNumber+" update received";
    if(debug){
      System.out.println("\n\n"+trackNumber+" set (update) new the location");
      System.out.println("Set lat lon"+updateLat.getDegrees () +" and "+updateLon.getDegrees ());
    }

    eventLogger.log(" ");
    eventLogger.log(out);

    Constant.othGoldOutput.println ("");
    Constant.othGoldOutput.println (out);
    Constant.othGoldOutput .flush ();

    if(reportLocation != null){

      EventHandle[] reportLoc = ECAAgent.getEventHandles(reportLocationEventName);
      PrimitiveEventHandle        peh  =   (PrimitiveEventHandle)beginCountDn;

      peh.insert("TimeStamp",(Object)TrackUtilities.getTimeStamp());
      ECAAgent.getDefaultECAAgent().raiseEndEvent(reportLoc,this);
      beginCountDown();
    }
    return  ;
  }


  public float[] getAOIZoneCoordinates()
  {
    float[] coords = new float[4];
    coords[0] = zoneA.getEBound();
    coords[1] = zoneA.getWBound();
    coords[2] = zoneA.getNBound();
    coords[3] = zoneA.getSBound();

    return coords;
  }

  /**
   * External event
   */
  public void beginCountDown(){
    EventHandle[] beginCountDn = ECAAgent.getEventHandles(beginCountDnEventName);
    ECAAgent.getDefaultECAAgent().raiseEndEvent(beginCountDn,this);
  }

  private void inZoneSignal(){
//    if(debug)
//    System.out.println("raise the inzone event");
    EventHandle[] trackInZone = ECAAgent.getEventHandles(inZoneEventName);
  ECAAgent.insert(trackInZone,"TrackNumber",trackNumber);
    ECAAgent.getDefaultECAAgent().raiseEndEvent(trackInZone,this);
  }

  private void outZoneSignal(){
      if(debug)
    System.out.println("raise the outzone event");
    EventHandle[] trackOutOfZone = ECAAgent.getEventHandles(outOfZoneEventName);
    ECAAgent.insert(trackOutOfZone,"TrackNumber",trackNumber);
    ECAAgent.getDefaultECAAgent().raiseEndEvent(trackOutOfZone,this);
  }





  /**
   * Sets the track number.
   *
   * @param number integer track number
   */

  public void setTrackNumber (String number){
    trackNumber = number;
  }




  /**
   * Returns the track number.
   *
   * @return integer track number
   */

  public String getTrackNumber (){
    return trackNumber;
  }


  /**
   * Returns the track speed.
   *
   * @return float speed in knots
   */

  public float getSpeed (){
    return speed;
  }


  /**
   * Returns the track latitude as a float.
   *
   * @return float latitude in degrees in the range -90.0 to +90.0
   */

  public float getLatitudeValue (){
    return trackLat.getValue ();
  }

  /**
   * Returns the track longitude as a float.
   *
   * @return float longitude in degrees in the range -180.0 to +180.0
   */

  public float getLongitudeValue (){
    return trackLon.getValue ();
  }

  /**
   * Returns the track latitude as a Latitude object.
   *
   * @return Latitude object
   */

  public Latitude getLatitude (){
    return trackLat;
  }

  /**
   * Returns the track longitude as a Longitude object.
   *
   * @return Longitude object
   */

  public Longitude getLongitude (){
    return trackLon;
  }

  /**
   * Returns a <code>String</code> representation of the track.
   *
   * @return String describing track
   */


  public String toString(){
    if(trackLat != null && trackLon != null)
    return (trackNumber+"; lat=" + trackLat.getValue() + ";lon=" + trackLon.getValue());
    else
    return (trackNumber+" No location detail");
  }
}