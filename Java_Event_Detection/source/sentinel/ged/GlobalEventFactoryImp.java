/**
 * Title:        Global Event Detection
 * Description:  Your description
 * Copyright:    Copyright (c) 1999
 * Company:      ITLAB, Unversity of Texas, Arlington
 * @author       Weera Tanpisuth
 * @version
 */

package sentinel.ged;

import sentinel.comm.*;
import java.rmi.*;
import java.rmi.server.*;
import sentinel.led.*;

/**
 * This class provides the methods for creating a global event from the remote site.
 * The client can make remote invocation to create primitive global event and
 * composite global event.
 */

public class GlobalEventFactoryImp extends UnicastRemoteObject
                           implements GlobalEventFactory{

  static boolean glbEvntCreatnDebug = DebuggingHelper.isDebugFlagTrue("glbEvntCreatnDebug");


  private GEDInterface gedIntf;
  private GlobalNodeManager glbNdMngr;

 /**
  * Constructor
  */
  public GlobalEventFactoryImp(GEDInterface gedIntf,GlobalNodeManager glbNdMngr)throws RemoteException{
    this.glbNdMngr = glbNdMngr;
    this.gedIntf = gedIntf;
  }

  /**
   * This method is used to create primitive global event.
   */
  public boolean createPrimGlobalEvent(String prodNm,String appNm,String machNm,String consID){

   // Map the new application id to the old application id
    consID  =  GEDUtilities.getOldAppID(consID);


    if(glbEvntCreatnDebug)
      System.out.println("\nGloabalEventFactory::createPrimGlobalEvent");

    // If this event detection was already requested by other LED
    // put the appID into the consumber list of that event node
    // Otherwise, create new node and put the appID into the consumer list

    StringBuffer sb = new StringBuffer(prodNm);
    sb.append (appNm);
    sb.append (machNm);
    String glbEvntNm = sb.toString();


    if(glbNdMngr.isExistent(glbEvntNm)){
      if(glbEvntCreatnDebug){
        System.out.println("This global event "+glbEvntNm+" already exists");
        System.out.println("Add the "+ consID +" into the consumer list in global node");
      }

      Event glbEvnt =  glbNdMngr.getGlbEvntNd(glbEvntNm);

      // Put the consumer into the consumer list (evntNm_Cons hashtable)
      gedIntf.putGlbEvntNmConsList(glbEvntNm,consID);
    }
    else{
      if(glbEvntCreatnDebug)
        System.out.println("Create a new global node :: "+ glbEvntNm);

      GlobalEvent newGlbEvnt = new GlobalEvent(glbEvntNm,prodNm,appNm,machNm);

      // Put the consumer into the consumer list (evntNm_Cons hashtable)
      gedIntf.putGlbEvntNmConsList(glbEvntNm,consID);

      glbNdMngr.putGlbEvntNm_GlbEvntNd(glbEvntNm,newGlbEvnt);
      GlobalEventHandle gvh = new GlobalEventHandle(newGlbEvnt,glbEvntNm);
      glbNdMngr.putGlbEvntNm_GlbEvntHndle(glbEvntNm,gvh);
    }
    return true;
  }

  /**
   * This method is used to create composite global event (binary operation).
   */
  public boolean createCompGlobalEvent(String eType,String lEvntNm,String rEvntNm,String appID){


    appID  =  GEDUtilities.getOldAppID(appID);

    if(glbEvntCreatnDebug)
      System.out.println("\nGloabalEventFactory::createCompGlobalEvent");

    CompositeEventHandle ceh = null;
    EventType eventType = null;

    if(eType.equals(EventType.AND.getName())){
      if(glbEvntCreatnDebug)
        System.out.println("Creating AND composite event");
      eventType = EventType.AND;
    }
    else if(eType.equals(EventType.SEQ.getName())){
      if(glbEvntCreatnDebug)
        System.out.println("Creating SEQ composite event");
      eventType = EventType.SEQ;
    }
    else if(eType.equals(EventType.OR.getName())){
      if(glbEvntCreatnDebug)
        System.out.println("Creating OR composite event");
      eventType = EventType.OR;
    }
    else{
      System.out.println("ERROR !!! at GlobalEventFactoryImp::createCompGlobalEvent");
    }

    StringBuffer sb = new StringBuffer(eventType.getName());
    sb.append (lEvntNm);
    sb.append (rEvntNm);
    String glbEvntNm = sb.toString();

    if(glbEvntCreatnDebug)
      System.out.println("A composite global event name\n"+ glbEvntNm);


    // If the global event already exists, add the consumer id into the
    // consumer list.
    if(glbNdMngr.isExistent(glbEvntNm)){
       if(glbEvntCreatnDebug){
        System.out.println("This global event "+glbEvntNm+" already exists");
        System.out.println("Add the "+ appID +" into the consumer list in global node");
      }

      GlobalEvent glbEvnt = (GlobalEvent) glbNdMngr.getGlbEvntNd(glbEvntNm);
      gedIntf.putGlbEvntNmConsList(glbEvntNm,appID);
      if(glbEvntCreatnDebug)
        System.out.println("Creation of the event on the server is complete");
    }
    else{

       // get the left event node & right event node
      Event lGlbEvnt = glbNdMngr.getGlbEvntNd(lEvntNm);
      Event rGlbEvnt = glbNdMngr.getGlbEvntNd(rEvntNm);

      // getting the handle of each event
      EventHandle leh = glbNdMngr.getGlbEvntHndl(lEvntNm);
      EventHandle reh = glbNdMngr.getGlbEvntHndl(rEvntNm);

      if(leh == null || reh == null){
        System.out.println("ERROR!! The constituents of the AND event don't exist");
        System.exit(-1);
      }

      if(eventType == EventType.AND) {
        if(glbEvntCreatnDebug)
          System.out.println("Create AND node");

        And andEvent = new And(glbEvntNm,leh,reh,gedIntf);
//        gedIntf.putGlbEvntNmConsList(glbEvntNm,appID);
        glbNdMngr.putGlbEvntNm_GlbEvntNd(glbEvntNm,andEvent);
        ceh = new CompositeEventHandle(andEvent,glbEvntNm);

        if(glbEvntCreatnDebug)
          System.out.println("Creation of AND event is complete");
      }
      else if(eventType == EventType.SEQ) {
        if(glbEvntCreatnDebug)
          System.out.println("Create SEQ node");

        Sequence seqEvent = new Sequence(glbEvntNm,leh,reh,gedIntf);
//        gedIntf.putGlbEvntNmConsList(glbEvntNm,appID);
        glbNdMngr.putGlbEvntNm_GlbEvntNd(glbEvntNm,seqEvent);
        ceh = new CompositeEventHandle(seqEvent,glbEvntNm);
        if(glbEvntCreatnDebug)
          System.out.println("Creation of SEQ event is complete");
      }else if(eventType == EventType.OR) {

        if(glbEvntCreatnDebug)
          System.out.println("Create OR node");

        Or orEvent = new Or(glbEvntNm,leh,reh,gedIntf);
//        gedIntf.putGlbEvntNmConsList(glbEvntNm,appID);
        glbNdMngr.putGlbEvntNm_GlbEvntNd(glbEvntNm,orEvent);
        ceh = new CompositeEventHandle(orEvent,glbEvntNm);
        if(glbEvntCreatnDebug)
          System.out.println("Creation of OR event is complete");
      }

      else{
        System.out.println("ERROR in GlobalEventFactoryImp");
        System.exit(-1);
      }
    }

    glbNdMngr.putGlbEvntNm_GlbEvntHndle(glbEvntNm,ceh);
    gedIntf.putGlbEvntNmConsList(glbEvntNm,appID);

    return true;
  }

  /**
   * This method is used to create composite global event (ternary operation).
   */
  public boolean createCompGlobalEvent(String eType,String lEvntNm,String mEvntNm,String rEvntNm,String appID){

    appID  =  GEDUtilities.getOldAppID(appID);

    if(glbEvntCreatnDebug)
      System.out.println("\nGloabalEventFactory::createCompGlobalEvent");


    CompositeEventHandle ceh = null;
    EventType eventType = null;



    if(eType.equals(EventType.NOT.getName())){
      if(glbEvntCreatnDebug)
        System.out.println("Creating SEQ composite event");
      eventType = EventType.NOT;
    }else{
      System.out.println("ERROR !!! at GlobalEventFactoryImp::createCompGlobalEvent");
    }

    StringBuffer sb = new StringBuffer(eventType.getName());
    sb.append (lEvntNm);
    sb.append (mEvntNm);
    sb.append (rEvntNm);
    String glbEvntNm = sb.toString();

    if(glbEvntCreatnDebug)
      System.out.println("A composite global event name\n"+ glbEvntNm);

    if(glbNdMngr.isExistent(glbEvntNm)){
      if(glbEvntCreatnDebug){
      System.out.println("This global event "+glbEvntNm+" already exists");
        System.out.println("Add the "+ appID +" into the consumer list in global node");
      }

      GlobalEvent glbEvnt = (GlobalEvent) glbNdMngr.getGlbEvntNd(glbEvntNm);
      gedIntf.putGlbEvntNmConsList(glbEvntNm,appID);

      if(glbEvntCreatnDebug)
       System.out.println("Creation of NOT event is complete");
    }
    else{

       // get the left event node & right event node
      Event lGlbEvnt = glbNdMngr.getGlbEvntNd(lEvntNm);
      Event mGlbEvnt = glbNdMngr.getGlbEvntNd(mEvntNm);
      Event rGlbEvnt = glbNdMngr.getGlbEvntNd(rEvntNm);

      // getting the handle of each event
      EventHandle leh = glbNdMngr.getGlbEvntHndl(lEvntNm);
      EventHandle meh = glbNdMngr.getGlbEvntHndl(mEvntNm);
      EventHandle reh = glbNdMngr.getGlbEvntHndl(rEvntNm);

      if(leh == null || meh == null || reh == null){
        System.out.println("ERROR!! The constituents of the AND event don't exist");
        System.exit(-1);
      }

      if(eventType == EventType.NOT) {
        if(glbEvntCreatnDebug)
          System.out.println("Create NOT node");

        Not notEvent = new Not(glbEvntNm,leh,meh,reh,gedIntf);
        gedIntf.putGlbEvntNmConsList(glbEvntNm,appID);
        glbNdMngr.putGlbEvntNm_GlbEvntNd(glbEvntNm,notEvent);
        ceh = new CompositeEventHandle(notEvent,glbEvntNm);
        if(glbEvntCreatnDebug)
          System.out.println("Creation of NOT event is complete");
      }
      else{
        System.out.println("ERROR in GlobalEventFactoryImp");
        System.exit(-1);
      }
    }
    glbNdMngr.putGlbEvntNm_GlbEvntHndle(glbEvntNm,ceh);
    return true;
  }
}