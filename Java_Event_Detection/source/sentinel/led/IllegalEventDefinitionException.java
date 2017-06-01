/**
 * IllegalEventDefinitionException.java --
 * Author          : Seokwon Yang, H. Kim
 * Created On      : Jan ?? 1999
 * Last Modified By: Seokwon Yang
 * Last Modified On: Sun Sep 19 23:25:10 1999
 * Copyright (C) University of Florida 1999
 */

package sentinel.led;

import java.lang.Exception;

public class IllegalEventDefinitionException extends Exception {

  public IllegalEventDefinitionException(int eventType, String eventName) {
    super("\n\n Definition of event \"" + eventName +"\" using \""
      + EventType.getName(eventType) + "\" Operator is Illegal");
  }
}
