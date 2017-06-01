/**
 * TypeMismatchException.java --
 * Author          : Seokwon Yang
 * Created On      : Fri Jan  8 23:06:54 1999
 * Last Modified By: Seokwon Yang
 * Last Modified On: Fri Jan  8 23:06:56 1999
 * RCS             : $Id: header.el,v 1.1 1997/02/17 21:45:38 seyang Exp seyang $
 * Copyright (C) University of Florida 1999
 */

package sentinel.led;
import java.lang.Exception;

/** TypeMismatchException is thrown when the application tries to retrieve the
 *  value of an event parameter from the ParameterList. The exception is thrown
 *  if there is a mismatch between the type of the parameter and the method call
 *  (getDouble(),getInt() etc.) used to retrieve the value.
 */

public class TypeMismatchException extends Exception {

   public TypeMismatchException(String varType, String varName) {
      super("\n\n Variable \"" + varName +
		"\" is not a " + varType + "parameter");
   }
}

