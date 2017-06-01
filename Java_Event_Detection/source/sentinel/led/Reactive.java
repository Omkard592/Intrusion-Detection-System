/**
 * Reactive.java --
 * Author          : Seokwon Yang
 * Created On      : Fri Jan  8 23:06:54 1999
 * Last Modified By: Seokwon Yang
 * Last Modified On: Fri Jan  8 23:06:56 1999
 * RCS             : $Id: header.el,v 1.1 1997/02/17 21:45:38 seyang Exp seyang $
 * Copyright (C) University of Florida 1999
 */

package sentinel.led;

import FESI.jslib.*;
import java.lang.reflect.*;
import java.util.StringTokenizer;

/** This is a parent class of all reactive class.
 *
 */

public abstract class Reactive extends Object {
  JSGlobalObject global = null;
  Field[] fields;
  String reactiveName;
  boolean latch = false;
  /**
   * basic Constructor
   * all the subclass should invoke this constructor using super(name)
   */
  public Reactive(String name) {
    reactiveName = name;
  }

  /**
   * load JavaScript Engine and register all public declared members(attributes) whith this engine
   */
  public void loadEngine() {
    if(!latch) {
      Class c = this.getClass();
      // register all the fileds
      fields = c.getDeclaredFields();

      try {
	global = JSUtil.makeEvaluator();
	for(int i=0; i<fields.length; i++) {
	  if(fields[i].getModifiers() == java.lang.reflect.Modifier.PUBLIC) {
	    setMember(fields[i].getName(),fields[i], this);
	  }
	}
      } catch(JSException e) {
	System.out.println("Error in creating interpretor  ");
      }
      latch =true;
    }
  }
  public boolean isJSEngineAvail() {
    return latch;
  }
  public String toString() {
    if(reactiveName != null) return reactiveName;
    else return this.toString();
  }
  /**
   * returns whether the token is the field of class
   *
   * @param token a value of type 'String'
   * @return a value of type 'boolean'
   */
  private boolean isField(String token) {
    if(fields.length == 0) return false;
    for(int i = 0; i<fields.length;i++)
      if(token.equals(fields[i].getName())) return true;
    return false;
  }

  /**
   * evalutes a cmd string and return the result
   *
   * @param cmd a value of type 'String'
   * @return a value of type 'boolean'
   */
  public boolean check(String cmd) {
    if(global == null) loadEngine();

    String command = ""; String token = null;
    // construct the comand following the javascript syntax
    // Note we can get field value with a function format
    // ex) value1 will be value1()
    // return token including delimeters <>=
    StringTokenizer st = new StringTokenizer(cmd, " <>=&!\t", true);
    while(st.hasMoreTokens()) {
      token = st.nextToken();
      if(isField(token)) command+= token +"()";
      else command+= token;
    }

    Object o = eval(command);

    Class c = o.getClass();
    if((c.getName()).equals("java.lang.Boolean")) {
      Boolean bool = (Boolean)o;
      return bool.booleanValue();
    }
    return false;

  }
  /**
   * evalutes a cmd string and return the result
   * The difference from check is this function can return any java object.
   * @param command a value of type 'java.lang.String'
   * @return a value of type 'java.lang.Object'
   */
  public java.lang.Object eval(java.lang.String s) {
    // if Javascript engine has yet to be load, just return false
    if(global == null) return null;

    Object o= null;

    try {
      o = global.eval(s);
    }
    catch(JSException e) {
      e.printStackTrace();
      System.out.println("Error in evaluation ");
    }
    return o;
  }

  /**
   * register each field with javascript engine
   *
   * @param fieldName a value of type 'String'
   * @param Field a value of type 'final'
   * @param Object a value of type 'final'
   * @exception JSException if an error occurs
   */
  private void setMember(String fieldName, final Field field,final Object targetInstance)
  throws JSException{

    global.setMember(fieldName,
		     new JSFunctionAdapter() {
      public Object doCall(JSObject thisObject, Object args[]) throws JSException {
	Object o = null;
	try {
	  o= field.get(targetInstance);
	} catch(IllegalAccessException e) {}
	return o;
      }
    });
  }
  /**
   * returns the value of the field of name
   * Note that it return a object so to get primitive value, users need to use the cast operator
   * @param name a value of type 'String'
   * @return a value of type 'java.lang.Object'
   */
  public java.lang.Object getProperty(String name) {
    return eval(name+"()");
  }

}
