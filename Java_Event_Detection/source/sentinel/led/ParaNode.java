/**
 * ParaNode.java --
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

/** The ParaNode class denotes a parameter in the ParameterList. It stores the type of the
 *  parameter and the value of the parameter. The value of the parameter is stored as a
 *  generic Object type since any Java type can be assigned to an Object.
 */
public class ParaNode  implements Serializable{
  String variableType;
  Object value;
  //private boolean ruleSchedulerDebug = Utilities.isDebugFlagTrue("ruleSchedulerDebug");

  public ParaNode() {}

  public ParaNode(String varType, Object val) {
    variableType = varType;
    value = val;
  }

  public ParaNode(Object val) {
    value = val;
  }

  public void print() {
    //if(ruleSchedulerDebug)
    //   System.out.println("parameter: " + value.getClass().getName()+ " " + value);
  }

  public Object getValue(){
    return value;
  }

  public    String getType(){
    return variableType;
  }
}
