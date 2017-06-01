/**
 * Composite.java --
 * Author          : Seokwon Yang, H. Kim
 * Created On      : Jan ?? 1999
 * Last Modified By: Seokwon Yang
 * Last Modified On: Sun Sep 19 23:25:10 1999
 * Copyright (C) University of Florida 1999
 */

package sentinel.led;

/** The CompositEventHandle class denotes the event handle for a composite event.
 */
public class CompositeEventHandle extends EventHandle {

   public CompositeEventHandle(Event event, String eventName) {
      super(event, eventName);
   }
}
