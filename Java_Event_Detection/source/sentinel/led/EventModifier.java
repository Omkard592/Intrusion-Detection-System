/**
 * EventModifier.java --
 * Author          : Seokwon Yang, H. Kim
 * Created On      : Jan ?? 1999
 * Last Modified By: Seokwon Yang
 * Last Modified On: Sun Sep 19 23:25:10 1999
 * Copyright (C) University of Florida 1999
 */

package sentinel.led;

/** This class contains the declaration of constants denoting the event modifiers
 *  defined by Sentinel. These constants are used in the definition of
 *  primitive events.
 */
public class EventModifier {
   public final static EventModifier BEGIN = new EventModifier("begin");
   public final static EventModifier END = new EventModifier("end");

   private String modifier;

   EventModifier(String modifier) {
     this.modifier = modifier;
   }

   String getId() {
     return modifier;
   }
}
