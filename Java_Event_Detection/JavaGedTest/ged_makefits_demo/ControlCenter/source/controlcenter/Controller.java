package controlcenter;


import java.awt.*;
import javax.swing.*;
import com.borland.jbcl.layout.*;
import java.awt.event.*;
import javax.swing.border.*;
import sentinel.led.*;
import java.io.*;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:
 * @author
 * @version 1.0
 */

public class Controller {


  static  ControlFrame frame = new ControlFrame();
  static{
    frame.setSize(320,473);
    frame.show();
  }
  static   JTextArea alertTextArea = frame.getAlertTextArea();
  static   JTextArea infoTextArea = frame.getInfoTextArea();

  public Controller() {
  }


  public static void main (String[] args){
    ECAAgent contrlAgent = ECAAgent.initializeECAAgent();
    Controller c = new Controller();



    /**
     * Rule 1 When track X, excluding US track, enters the area of interest (AOI).
     * Notify the control center
     *
     * Global primitive event from MAKEFITS (NAVY)
     */

    // TrackNumber T7001
    EventHandle T7001InAOIEvntHndl =   contrlAgent.createPrimitiveEvent("T7001_Enter_AOI","controlcenter.Controller","T7001_enterZoneEvent","shipTrackApp","myhome");
    contrlAgent.createRule("T7001_Enter_AOI_Rule",T7001InAOIEvntHndl,"controlcenter.Controller.TRUE","controlcenter.Controller.A_enterAOI");


    // TrackNumber US001
    EventHandle US001InAOIEvntHndl =   contrlAgent.createPrimitiveEvent("US001_Enter_AOI","controlcenter.Controller","US001_enterZoneEvent","shipTrackApp","myhome");
    contrlAgent.createRule("US001_Enter_AOI_Rule",US001InAOIEvntHndl,"controlcenter.Controller.TRUE","controlcenter.Controller.A_enterAOI");

    // TrackNumber US002
    EventHandle US002InAOIEvntHndl =   contrlAgent.createPrimitiveEvent("US002_Enter_AOI","controlcenter.Controller","US002_enterZoneEvent","airTrackApp","myhome");
    contrlAgent.createRule("US002_Enter_AOI_Rule",US002InAOIEvntHndl,"controlcenter.Controller.TRUE","controlcenter.Controller.A_enterAOI");




    /**
     * Rule 2)	When track X, excluding US track(s), leaves the area of
     * interest (AOI). Notify the control center
     *
     * Global primitive event from MAKEFITS (NAVY)
     */
    EventHandle T7001OutAOIEvntHndl =   contrlAgent.createPrimitiveEvent("T7001_Leave_AOI","controlcenter.Controller","T7001_leaveZoneEvent","shipTrackApp","myhome");
    contrlAgent.createRule("T7001_leaveZoneEvent",T7001OutAOIEvntHndl,"controlcenter.Controller.TRUE","controlcenter.Controller.A_leaveAOI");

//    EventHandle T7001InAOIEvntHndl =   contrlAgent.createPrimitiveEvent("T7001_Enter_AOI","controlcenter.Controller","T7001_enterZoneEvent","shipTrackApp","myhome");
//    contrlAgent.createRule("T7001_Enter_AOI_Rule",T7001InAOIEvntHndl,"controlcenter.Controller.TRUE","controlcenter.Controller.A_enterAOI");



    /**
     * Rule 3) When track x, excluding US track(s), is in the AOI
     * for more than Y minutes, notify analyst.
     *
     * Global composite event (NOT). This will be detected at local site
     */
    EventHandle trackInAOIXMinEvntHandl = contrlAgent.createCompositeEvent (
                              EventType.NOT,
                              "trackInAOIXMinEvent",
                              T7001InAOIEvntHndl, // Left Event
                              T7001OutAOIEvntHndl,  //Middle Event
                              //Right Event
                              contrlAgent.createCompositeEvent(
                                          EventType.PLUS,
                                          "plusT7001InAOIEvntHndlEvent",
                                          T7001InAOIEvntHndl,
                                          "0 hrs 0 min 5 sec"));

    contrlAgent.createRule("trackInAOIXMinRule",trackInAOIXMinEvntHandl,"controlcenter.Controller.TRUE","controlcenter.Controller.A_trackStyaInAOIXMin");



    /**
     * Rule 4)	When both hostile air track and hostile marine track are
     * in the area of interest. Notify the control center.
     */

    EventHandle hostileShipInAOIEvntHndl =   contrlAgent.createPrimitiveEvent("Enmity_Ship_Enter_AOI","controlcenter.Controller","AnyEnterZone","shipTrackApp","myhome");
    EventHandle hostileAircraftInAOIEvntHndl =   contrlAgent.createPrimitiveEvent("Enmity_Ship_Enter_AOI","controlcenter.Controller","AnyEnterZone","airTrackApp","myhome");

    /**
     * Global composite event (AND). This will be detected at GED
     */
    EventHandle hostileTracksInAOIEvntHndl = contrlAgent.createCompositeEvent(EventType.AND,"anyHostileEnterAOI",hostileShipInAOIEvntHndl,hostileAircraftInAOIEvntHndl);
    contrlAgent.createRule("Aircraft_Ship_Enter_AOI_Rule",hostileTracksInAOIEvntHndl,"controlcenter.Controller.TRUE","controlcenter.Controller.A_HostileTracksInAOI");


    /**
     * Rule 5) Notify the control center, if no US track(s) encounter the
     * hostile track in (AOI) after hostile track X enters AOI within 2 minutes.
     */

    /**
     * Global composite event (OR). This will be detected at GED
     */
    EventHandle anyUSTrackInAOIEvntHndl = contrlAgent.createCompositeEvent(EventType.OR,"anyUSEnterAOI",US002InAOIEvntHndl,US001InAOIEvntHndl);
    contrlAgent.createRule("US_Enter_AOI_Rule",anyUSTrackInAOIEvntHndl,"controlcenter.Controller.TRUE","controlcenter.Controller.A_USenterAOI");

    /**
     * Global composite event (NOT). This will be detected at local site
     */
    EventHandle USTrackNotComeEvntHandl = contrlAgent.createCompositeEvent (
                              EventType.NOT,"TESTNOTEVENT",
                              T7001InAOIEvntHndl, // Left Event
                              anyUSTrackInAOIEvntHndl,  //Middle Event
                              //Right Event
                              contrlAgent.createCompositeEvent(
                                          EventType.PLUS,
                                          "plusEvent",
                                          T7001InAOIEvntHndl,
                                          "0 hrs 0 min 3 sec"));

    contrlAgent.createRule("US_Not_Enter_AOI_Rule",USTrackNotComeEvntHandl,"controlcenter.Controller.TRUE","controlcenter.Controller.A_USTrackNotCome");




    int inputIn = 1;

    while(inputIn != 0){
      System.out.println("Type 0 to end");

      String input ;
      boolean inputValid = false;
      while(!inputValid){

        try{

          BufferedReader inputStream = new BufferedReader(new InputStreamReader(System.in));
          inputIn  = Integer.parseInt(inputStream.readLine());

          System.out.println("\n\ninputIn = "+inputIn);
          switch(inputIn){
          case 0:
            inputValid = true;
            break;
          }

        }
        catch(NumberFormatException e ){
          inputValid = false;
        }
        catch(Exception ex ){
          inputValid = false;
        }
      }

    }
  }

  public static void startTrackApps(){
    EventHandle[] strt = ECAAgent.getEventHandles("startEvent");
    ECAAgent.getDefaultECAAgent().raiseEndEvent(strt,null);
  }

  public static void stopTrackApps(){
    EventHandle[] stop = ECAAgent.getEventHandles("stopEvent");
    ECAAgent.getDefaultECAAgent().raiseEndEvent(stop,null);
  }

  public void A_enterAOI (ListOfParameterLists parameterLists){
    System.out.println("***** From Action in A_enterAOI ***** ");
    String  dataFromParam = "";
    ParameterList paramList = parameterLists.getFirst();
    System.out.println("*** Check the remote paramter !!!");

    try{
      dataFromParam =  (String) paramList.getObject("TrackNumber");
      System.out.println("The value of the paramter from producer is => " +dataFromParam);
    }
    catch(TypeMismatchException e){
      e.printStackTrace() ;
    }
    catch(ParameterNotFoundException e){
      e.printStackTrace() ;
    }

   alertTextArea.append("\n\nTrack "+ dataFromParam+" is in AOI zone\n" );
  }

  public void A_leaveAOI (ListOfParameterLists parameterLists){

    String  dataFromParam = "";
    ParameterList paramList = parameterLists.getFirst();
    System.out.println("*** Check the remote paramter !!!");

    try{
      dataFromParam =  (String) paramList.getObject("TrackNumber");
      System.out.println("The value of the paramter from producer is => " +dataFromParam);
    }
    catch(TypeMismatchException e){
      e.printStackTrace() ;
    }
    catch(ParameterNotFoundException e){
      e.printStackTrace() ;
    }

   alertTextArea.append("\n\nTrack "+ dataFromParam+" leaves AOI zone\n" );
  }


  public void A_trackStyaInAOIXMin(ListOfParameterLists parameterLists){

    String  dataFromParam = "";
    ParameterList paramList = parameterLists.getFirst();
    System.out.println("*** Check the remote paramter !!!");

    try{
      dataFromParam =  (String) paramList.getObject("TrackNumber");
      System.out.println("The value of the paramter from producer is => " +dataFromParam);
    }
    catch(TypeMismatchException e){
      e.printStackTrace() ;
    }
    catch(ParameterNotFoundException e){
      e.printStackTrace() ;
    }
   alertTextArea.append("\n\nTrack "+ dataFromParam+" is in AOI zone for x min \n" );
  }


  public void A_USenterAOI (ListOfParameterLists parameterLists){
   alertTextArea.append("\n\nUS Track(s) is in AOI zone. Ready to attack the hostile\n");
   System.out.println("ECA Action ");
  }

  public void A_USTrackNotCome (ListOfParameterLists parameterLists){
   alertTextArea.append("\n\nUS Track(s) didn't come to AOI within 10 sec\n");
   System.out.println("ECA Action ");
  }

  public void A_HostileTracksInAOI(ListOfParameterLists parameterLists){
    alertTextArea.append("\n\nUS Enmity(Air craft and ship) are in AOI\n");
    System.out.println("ECA Action ");
  }

  public void TEST(ListOfParameterLists parameterLists){
    alertTextArea.append("Test" );
  }

  public boolean TRUE(ListOfParameterLists parameterLists){
    System.out.println("ECA  TRUE");
    return true;

  }
}
