/**
 * Title:        Global Event Detection
 * Description:
 * Copyright:    Copyright (c) 1999
 * Company:      ITLAB University of Texas, Arlington
 * @author       Weera Tanpisuth
 */

package sentinel.comm;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * This interface defines a factory for global event factory implementations.
 * It is implemented by the class GlobalEventFacotryImp to create primitive global
 * event and composite global event on the server.
 */

public interface GlobalEventFactory extends Remote{

  /**
   * Create global primitive event
   */

  public boolean createPrimGlobalEvent(String prodEvntNm,String appNm,String machNm,String consID) throws RemoteException;

  /**
   * Create global composite event (binary operation)
   */

  public boolean createCompGlobalEvent(String eventType,String lGlbEvntNm,String rGlbEvntNm,String appID) throws RemoteException;

  /**
   * Create global composite event (ternary operation)
   */

  public boolean createCompGlobalEvent(String eventType,String lGlbEvntNm,String mGlbEvntNm, String rGlbEvntNm,String appID) throws RemoteException;

}



// public boolean createGlobalEvent(String evntNm,String appNm,String machNm) throws RemoteException;
//  public boolean createCompGlobalEvent(String eventType,String lGlbEvntNm,String rGlbEvntNm,String appID) throws RemoteException;
// The method is to create the composite (NOT) event.
//  public boolean createGlobalEvent(String evntNm,String appID1,String appID) throws RemoteException;