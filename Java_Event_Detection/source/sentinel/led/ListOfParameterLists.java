/**
 * ListOfParameterLists.java --
 * Author          : Seokwon Yang, H. Kim
 * Created On      : Jan ?? 1999
 * Last Modified By: Seokwon Yang
 * Last Modified On: Thu Aug 12 02:33:20 1999
 * Copyright (C) University of Florida 1999
 */
package sentinel.led;

import java.util.*;
import java.io.*;

/** The ListOfParameterLists class is used to send parameters of an event
 * to condition and action methods.It is a list of the parameter lists
 * of the constituent primitive events. It provides API to extract individual
 * ParameterLists from the ListOfParameterLists.
 *
 * All condition and action methods take ListOfParameterLists as the only
 * argument.
 */

public class ListOfParameterLists implements Serializable{

  Vector paramLists;

  public  ListOfParameterLists() {
    paramLists = new Vector();
  }

  /** This method returns the first ParameterList in the ListOfParameterLists.
   *
   *  @return ParameterList
   */
  public ParameterList getFirst() {
    return((ParameterList) paramLists.firstElement());
  }

  public void add(ParameterList paramList) {
    paramLists.addElement(paramList);
  }

  /** This method returns an enumeration of all the ParameterLists.
   *
   *  @return Enumeration of ParameterLists
   */
  public Enumeration elements() {
    Enumeration en = paramLists.elements();
    return en;
  }

  /** This method returns a ParameterList that contains the given instance as
   *  its event instance. The event instance is the instance over which the
   *  event method is invoked.
   *
   *  @param The event instance associated with the ParameterList
   *  @return ParameterList
   */
  public ParameterList getParamListWithInstance(Object instance) {
      ParameterList paramList = null;
      Enumeration en = paramLists.elements();
      while (en.hasMoreElements()) {
         paramList = (ParameterList) en.nextElement();
         if (paramList.hasInstance(instance))
            break;
      }
      return paramList;
   }

   // added by weera on Aug 15,2000
   // rational : This method assists a user to get instance easier.

  /** This method returns a ParameterList that contains the given instance as
   *  its event instance. The event instance is the instance over which the
   *  event method is invoked.
   *
   *  @param The event instance associated with the methodSignature
   *  @return ParameterList
   */
  public ParameterList getParamListWithMethodSignature(String methodSignature) {
    ParameterList paramList = null;
    Enumeration en = paramLists.elements();
    boolean methodExist =  false;
    while (en.hasMoreElements()) {
      paramList = (ParameterList) en.nextElement();
      if (paramList.hasInstanceWithMethodSignature(methodSignature)){
        methodExist = true;
        break;
      }
    }
    if(!methodExist)
      System.out.println("ERROR !! The given methodSignature doesn't exist");
    return paramList;
  }

  // end added

 /** This method returns a ParameterList with an event instance of the given
  *  type.
  *
  *  @param The name of the class denoting the type.
  *  @retun ParameterList
  */
  public ParameterList getParamListWithInstanceType(String className) {
    ParameterList paramList = null;
    Enumeration en = paramLists.elements();
    while (en.hasMoreElements()) {
      paramList = (ParameterList) en.nextElement();
      if (paramList.hasInstanceWithType(className))
       return paramList;
      //  break;
    }
    paramList = null;
    return paramList;
  }
}
