/**
 * PrimitiveEventHandle.java --
 * Author          : Seokwon Yang
 * Created On      : Fri Jan  8 23:06:54 1999
 * Last Modified By: Seokwon Yang
 * Last Modified On: Fri Jan  8 23:06:56 1999
 * RCS             : $Id: header.el,v 1.1 1997/02/17 21:45:38 seyang Exp seyang $
 * Copyright (C) University of Florida 1999
 */


package sentinel.led;

/** This class denotes a primitive event handle used for creating composite events
 *  and rules. It is used in composite event creation and rule creation API. The
 *  event handle is also used to insert the parameters of a primitive event and raise
 *  the event through raiseBeginEvent() or raiseEndEvent() API.
 */
public class PrimitiveEventHandle extends EventHandle {
  ParameterList paramList;
  String className;
  String methodSig;

  PrimitiveEventHandle(Event event, String eventName,
			  String className, String methodSig) {
    super(event, eventName);
    this.className = className;
    this.methodSig = methodSig;
    paramList = new ParameterList();
    paramList.setMethodSignature(methodSig);
  }

  // added by seyang on 9 Aug 99
  protected boolean isFilterEvent(){
    return (eventNode instanceof FilterEvent);
  }

  protected boolean check(Reactive instance) {
    if(instance.isJSEngineAvail()) {
      instance.loadEngine();
    }
    FilterEvent event = (FilterEvent)eventNode;
    return event.check(instance);
  }

  protected boolean check() {
    FilterEvent event = (FilterEvent)eventNode;
    return event.check();
  }
  // added by seyang on 9 Aug 99

  String getMethodSig() {
    return methodSig;
  }

  String getBeginSignature() {
    return (Utilities.remWhiteSpaces(className + "begin" + methodSig));
  }

   String getEndSignature() {
      return (Utilities.remWhiteSpaces(className + "end" + methodSig));
   }

   ParameterList getParamList() {
      return paramList;
   }

   /** This method inserts an integer parameter into the parameter list of
    *  the event handle.
    *
    *  @param varName		Name of the integer variable
    *
    *  @param intValue		Value of the integer variable
    */
   public void insert(String varName, int intValue) {
      paramList.insertInt(varName,intValue);
   }

   /** This method inserts a float parameter into the parameter list of
    *  the event handle.
    *
    *  @param varName			Name of the float variable
    *
    *  @param floatValue		Value of the float variable
    */
   public void insert(String varName, float floatValue) {
      paramList.insertFloat(varName,floatValue);
   }

   /** This method inserts a byte parameter into the parameter list of
    *  the event handle.
    *
    *  @param varName		Name of the byte variable
    *
    *  @param byteValue		Value of the byte variable
    */
   public void insert(String varName, byte byteValue) {
      paramList.insertByte(varName,byteValue);
   }

   /** This method inserts a short parameter into the parameter list of
    *  the event handle.
    *
    *  @param varName			Name of the short variable
    *
    *  @param shortValue		Value of the short variable
    */
   public void insert(String varName, short shortValue) {
      paramList.insertShort(varName,shortValue);
   }

   /** This method inserts a long parameter into the parameter list of
    *  the event handle.
    *
    *  @param varName		Name of the long variable
    *
    *  @param longValue		Value of the long variable
    */
   public void insert(String varName, long longValue) {
      paramList.insertLong(varName,longValue);
   }

	/** This method inserts a double parameter into the parameter list of
    *  the event handle.
    *
    *  @param varName			Name of the double variable
    *
    *  @param doubleValue		Value of the double variable
    */
   public void insert(String varName, double doubleValue) {
      paramList.insertDouble(varName,doubleValue);
   }

   /** This method inserts a char parameter into the parameter list of
    *  the event handle.
    *
    *  @param varName		Name of the char variable
    *
    *  @param charValue		Value of the char variable
    */
   public void insert(String varName, char charValue) {
      paramList.insertChar(varName,charValue);
   }

   /** This method inserts a boolean parameter into the parameter list of
    *  the event handle.
    *
    *  @param varName			Name of the boolean variable
    *
    *  @param boolValue			Value of the boolean variable
    */
   public void insert(String varName, boolean boolValue) {
      paramList.insertBoolean(varName,boolValue);
   }

   /** This method inserts an Object parameter into the parameter list of
    *  the event handle.
    *
    *  @param varName		Name of the Object variable
    *
    *  @param object		Value of the Object variable
    */
   public void insert(String varName, Object object) {
      paramList.insertObject(varName,object);
   }
}
