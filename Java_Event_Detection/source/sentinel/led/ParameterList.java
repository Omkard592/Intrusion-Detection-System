
/**
 * ParameterList.java --
 * Author          : Seokwon Yang
 * Created On      : Fri Jan  8 23:06:54 1999
 * Last Modified By: Seokwon Yang
 * Last Modified On: Fri Jan  8 23:06:56 1999
 * RCS             : $Id: header.el,v 1.1 1997/02/17 21:45:38 seyang Exp seyang $
 * Copyright (C) University of Florida 1999
 */

package sentinel.led;

import java.util.*;
import java.io.*;

/** The ParameterList class denotes the list of parameters of a
 *  primitive event. It stores the parameters in a Hashtable. It
 *  also contains the TimeStamp of the event and the event
 *  instance.
 */
public class ParameterList implements Serializable{

//  static transient boolean eventDetectionDebug = DebuggingHelper.isDebugFlagTrue("eventDetectionDebug");

  private  Hashtable parameters;
  private  String methodSignature;
  private  TimeStamp timeStamp;
  transient  private  Object eventObject;

  public ParameterList() {
    parameters = new Hashtable();
  }

  public ParameterList(Hashtable parameters, String methodSig) {
    this.parameters = parameters;
    this.methodSignature = methodSig;
  }

  public void setTS() {
    this.timeStamp = new TimeStamp();
  }

  void setTS(long eventId) {
    this.timeStamp = new TimeStamp(eventId);
  }

  public TimeStamp getTS() {
    return timeStamp;
  }

  public void setEventInstance(Object instance) {
    eventObject = instance;
  }

  void setMethodSignature(String methodSig) {
    this.methodSignature = methodSig;
  }

  String getMethodSignature() {
    return methodSignature;
  }

   /** This method return true if this ParameterList contains the given instance
    *  as its event instance. The event instance is the instance over which the
    *  event method is invoked.
    */
  boolean hasInstance(Object instance) {
    if (instance == eventObject){
      //  if(eventDetectionDebug)
      System.out.println("The instance matches with the event object in ParameterList");
      return true;
    }
    else
      return false;
  }

   /** This method returns true if the given class name is same as the type of the
    *  event instance of this ParameterList.
    */
  boolean hasInstanceWithType(String className) {
    if (eventObject == null)
      return false;
    String varType = eventObject.getClass().getName();
    if (varType.equals(className))
      return true;
    else
      return false;
  }

   // added by weera Aug 15,2000
   // rational :  allow a user to access target instance

   /** This method returns true if the given methodSignature is same as the type of the
    *  event instance of this ParameterList.
    */
   boolean hasInstanceWithMethodSignature(String MethodSignature) {
     if (eventObject == null)
       return false;
     String methdSig = getMethodSignature();
     if (methdSig.equals(MethodSignature))
      return true;
    else
      return false;
  }

   Hashtable getHashtable() {
      return parameters;
   }

   /** This method returns the event instance as an Object.
    *
    *  @return Object
    */
   public Object getEventInstance() {
      return eventObject;
   }

   /** This method is used to insert a primitive integer variable
    *  into the ParameterList
    *
    *  @param variableName	Name of the variable
    *  @param value		Value of the variable
    */
   void insertInt(String variableName, int value) {
       ParaNode paraNode = new ParaNode(new Integer(value));
       parameters.put(variableName, paraNode);
   }

   /** This method is used to insert a primitive float variable
    *  into the ParameterList
    *
    *  @param variableName	Name of the variable
    *  @param value		Value of the variable
    */
   void insertFloat(String variableName, float value) {
       ParaNode paraNode = new ParaNode(new Float(value));
       parameters.put(variableName, paraNode);
   }

   /** This method is used to insert a primitive double variable
    *  into the ParameterList
    *
    *  @param variableName	Name of the variable
    *  @param value		Value of the variable
    */
   void insertDouble(String variableName, double value) {
       ParaNode paraNode = new ParaNode(new Double(value));
       parameters.put(variableName, paraNode);
   }

   /** This method is used to insert a primitive byte variable
    *  into the ParameterList
    *
    *  @param variableName	Name of the variable
    *  @param value		Value of the variable
    */
   void insertByte(String variableName, byte value) {
       ParaNode paraNode = new ParaNode(new Byte(value));
       parameters.put(variableName, paraNode);
   }

   /** This method is used to insert a primitive short variable
    *  into the ParameterList
    *
    *  @param variableName      Name of the variable
    *  @param value		Value of the variable
    */
   void insertShort(String variableName, short value) {
       ParaNode paraNode = new ParaNode(new Short(value));
       parameters.put(variableName, paraNode);
   }

   /** This method is used to insert a primitive long variable
    *  into the ParameterList
    *
    *  @param variableName	Name of the variable
    *  @param value		Value of the variable
    */
   void insertLong(String variableName, long value) {
       ParaNode paraNode = new ParaNode(new Long(value));
       parameters.put(variableName, paraNode);
   }

   /** This method is used to insert a primitive char variable
    *  into the ParameterList
    *
    *  @param variableName	Name of the variable
    *  @param value		Value of the variable
    */
   void insertChar(String variableName, char value) {
       ParaNode paraNode = new ParaNode(new Character(value));
       parameters.put(variableName, paraNode);
   }

   /** This method is used to insert a primitive boolean variable
    *  into the ParameterList
    *
    *  @param variableName	Name of the variable
    *  @param value		Value of the variable
    */
   void insertBoolean(String variableName, boolean value) {
       ParaNode paraNode = new ParaNode(new Boolean(value));
       parameters.put(variableName, paraNode);
   }

   /** This method is used to insert any Object type variable
    *  into the ParameterList
    *
    *  @param variableName	Name of the variable
    *  @param value		Value of the variable
    */
  void insertObject(String variableName, Object value) {
    ParaNode paraNode = new ParaNode(value);
    parameters.put(variableName, paraNode);
  }

   /** This method can be used to print the contents of the ParameterList.
    *  It prints the names of the parameters and their values.
    */
  public void print() {
    ParaNode paraNode;
    Enumeration en = parameters.keys();
    // if(ruleSchedulerDebug)
    // System.out.println("\nPrinting ParameterList ->> ");
    //System.out.print(methodSignature);
    //System.out.println(timeStamp.getSequence());
    while (en.hasMoreElements()) {
      String key = (String) en.nextElement();
      paraNode = (ParaNode) parameters.get(key);
      paraNode.print();
    }
  }

  private Object getParameterObj(String varName)
			throws ParameterNotFoundException {

    ParaNode pn = (ParaNode) parameters.get(varName);
    if (pn == null)
      throw new ParameterNotFoundException(varName);

    Object obj = pn.getValue();
    return obj;
  }

   /** This method is used to get the value of a primitive
    *  integer variable from the ParameterList
    *
    *  @param variableName		Name of the variable
    *
    *  @return					The value of the integer variable
    */
  public int getInt(String variableName)throws ParameterNotFoundException,
        TypeMismatchException {
    Object obj = getParameterObj(variableName);
    String varType = obj.getClass().getName();
    Integer intObj = null;
    // System.out.println("varType = " + varType);
    if (varType.equals("java.lang.Integer"))
      intObj = (Integer) obj;
    else
      throw new TypeMismatchException("int", variableName);
    return intObj.intValue();
  }

   /** This method is used to get the value of a primitive
    *  float variable from the ParameterList
    *
    *  @param variableName	Name of the variable
    *  @return			The value of the float variable
    */
  public float getFloat(String variableName)throws ParameterNotFoundException,
          TypeMismatchException {
    Object obj = getParameterObj(variableName);
    String varType = obj.getClass().getName();
    Float floatObj = null;
    if (varType.equals("java.lang.Float"))
      floatObj = (Float) obj;
    else
      throw new TypeMismatchException("float", variableName);
    return floatObj.floatValue();
  }

   /** This method is used to get the value of a primitive
    *  double variable from the ParameterList
    *
    *  @param variableName	Name of the variable
    *  @return			The value of the double variable
    */
   public double getDouble(String variableName)throws ParameterNotFoundException,
        TypeMismatchException {
    Object obj = getParameterObj(variableName);
    String varType = obj.getClass().getName();
    Double doubleObj = null;
    if (varType.equals("java.lang.Double"))
      doubleObj = (Double) obj;
    else
      throw new TypeMismatchException("double", variableName);
    return doubleObj.doubleValue();
  }

   /** This method is used to get the value of a primitive
    *  byte variable from the ParameterList
    *
    *  @param variableName	Name of the variable
    *  @return			The value of the byte variable
    */
   public byte getByte(String variableName)throws ParameterNotFoundException,
          TypeMismatchException {
    Object obj = getParameterObj(variableName);
    String varType = obj.getClass().getName();
    Byte byteObj = null;
    if (varType.equals("java.lang.Byte"))
      byteObj = (Byte) obj;
    else
      throw new TypeMismatchException("byte", variableName);
    return byteObj.byteValue();
  }

   /** This method is used to get the value of a primitive
    *  short variable from the ParameterList
    *
    *  @param variableName	Name of the variable
    *  @return			The value of the short variable
    */
   public short getShort(String variableName)throws ParameterNotFoundException,
         TypeMismatchException {
    Object obj = getParameterObj(variableName);
    String varType = obj.getClass().getName();
    Short shortObj = null;
    if (varType.equals("java.lang.Short"))
      shortObj = (Short) obj;
    else
      throw new TypeMismatchException("short", variableName);
    return shortObj.shortValue();
  }

   /** This method is used to get the value of a primitive
    *  long variable from the ParameterList
    *
    *  @param variableName	Name of the variable
    *  @return			The value of the long variable
    */
  public long getLong(String variableName) throws ParameterNotFoundException,
        TypeMismatchException {
    Object obj = getParameterObj(variableName);
    String varType = obj.getClass().getName();
    Long longObj = null;
    if (varType.equals("java.lang.Long"))
      longObj = (Long) obj;
    else
      throw new TypeMismatchException("long", variableName);
    return longObj.longValue();
  }

   /** This method is used to get the value of a primitive
    *  character variable from the ParameterList
    *
    *  @param variableName	Name of the variable
    *  @return			The value of the character variable
    */
   public char getChar(String variableName)throws ParameterNotFoundException,
          TypeMismatchException {
    Object obj = getParameterObj(variableName);
    String varType = obj.getClass().getName();
    Character charObj = null;
    if (varType.equals("java.lang.Character"))
      charObj = (Character) obj;
    else
      throw new TypeMismatchException("char", variableName);
    return charObj.charValue();
   }

   /** This method is used to get the value of a primitive
    *  boolean variable from the ParameterList
    *
    *  @param variableName	Name of the variable
    *  @return			The value of the boolean variable
    */
   public boolean getBoolean(String variableName) throws ParameterNotFoundException,
          TypeMismatchException {
    Object obj = getParameterObj(variableName);
    String varType = obj.getClass().getName();
    Boolean boolObj = null;
    if (varType.equals("java.lang.Boolean"))
      boolObj = (Boolean) obj;
    else
      throw new TypeMismatchException("boolean", variableName);
    return boolObj.booleanValue();
  }

   /** This method is used to get the value of any Object
    *  variable from the ParameterList
    *
    *  @param variableName    Name of the variable
    *  @return		      The value of the Object variable
    */
   public Object getObject(String variableName) throws ParameterNotFoundException,
        TypeMismatchException {

    Object obj = getParameterObj(variableName);
    return obj;
  }
}


