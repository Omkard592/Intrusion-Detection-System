/**
 * Title:        Global Event Detection
 * Description:  Your description
 * Copyright:    Copyright (c) 1999
 * Company:      ITLAB, Unversity of Texas, Arlington
 * @author       Weera Tanpisuth
 * @version
 */

package sentinel.ged;
import sentinel.led.*;
import java.util.Hashtable ;
import java.util.Vector ;

/**
 * This class in used to manage the global event node. The global event
 * detection can refer the event node by using this class
 */

public class GlobalNodeManager {

//  static boolean glbEvntReqstDebug = DebuggingHelper.isDebugFlagTrue("glbEvntReqstDebug");

  /* This class field contains all the global node managers */
  private static Vector glbNdMngrList = new Vector();

  /**
   * Table contains all the global event name and its corresponding the global
   * node manager.
   *
   * Note !! This is for a extension version of multiple GED.
   */
  private static Hashtable agentName_glbNdManager = new Hashtable();

  private GECAAgent gecaAgent;

  /**
   * Map the global event name and the global even node
   */
  private Hashtable glbEvntNm_GlbEvntNd = new Hashtable();

  /**
   * Map the global event name and the global event handle
   */
  private Hashtable glbEvntNm_GlbEvntHndle = new Hashtable();

  /**
   * Constructor
   */
  GlobalNodeManager(GECAAgent gecaAgent) {
   this.gecaAgent = gecaAgent ;
  }


  /**
   * Add the global node manager into the global node manager list.
   */

   // Note !! the global node manager list will be used when running many
   // global event detectors

  static void addGlbNdMngr(GlobalNodeManager glbNdMngr){
    glbNdMngrList.add (glbNdMngr) ;
  }


  /**
   * This method is used to check the existence of the event node. Whenever
   * the global event factory will create the event node, it will check whether
   * the event node already exists or not
   */
  boolean isExistent(String glbEvntNm){
    if(glbEvntNm_GlbEvntNd.containsKey (glbEvntNm))
      return true;
    else
      return false;
  }

  /**
   * Returns the global event node
   */
 Event getGlbEvntNd(String glbEvntNm){
    Event glbEvnt = null;
    if( (glbEvnt = (Event) glbEvntNm_GlbEvntNd.get (glbEvntNm)) == null){
      System.out.println("Internal ERROR: There is no global event node");
      return glbEvnt;
    }
    else
      return glbEvnt;
  }

  /**
   * Returns the global event handle. This method helps the global event factory
   * to create the composite event.
   */
 EventHandle getGlbEvntHndl(String glbEvntNm){
    EventHandle glbEvntHandl = null;
    if( (glbEvntHandl = (EventHandle) glbEvntNm_GlbEvntHndle.get (glbEvntNm)) == null){
      System.out.println("Internal ERROR: There is no global event handle");
      return glbEvntHandl;
    }
    else
      return glbEvntHandl;
  }

  /**
   * Keep a pair of global event name and global event node into the table which
   * is used in referencing back the event node.
   */
  void putGlbEvntNm_GlbEvntNd(String glbEvntNm,Event glbEvnt){
//    if(glbDetctnReqstDebug)
//      System.out.println("Want to consume this event name "+ glbEvntNm);
    glbEvntNm_GlbEvntNd.put (glbEvntNm,glbEvnt);
  }


  // Note !! This agentName_glbNdManager will be used when running many global
  // event detectors

  static void putAgentName_glbManager(String agentNm, GlobalNodeManager glbNdMngr){
    agentName_glbNdManager.put (agentNm,glbNdMngr);
  }

  /**
   * Keep a pair of global event name and global event handle into the table which
   * is used in referencing back the event handle.
   */
  void putGlbEvntNm_GlbEvntHndle(String glbEvntNm,EventHandle glbEvntHandle){
    glbEvntNm_GlbEvntHndle.put (glbEvntNm,glbEvntHandle);
  }


  /**
   * Returns the global node manager list keeping all the global node managers
   */

  static Vector getGlbNdMngrList(){
    return glbNdMngrList;
  }

  /**
   * The method is to dereference to the GECAAgent that's assocated with
   * this global node manager.
   */
  GECAAgent getAssocAgent(){
    return gecaAgent;
  }
}