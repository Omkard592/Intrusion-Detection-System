/**
 * EventType.java --
 * Author          : Seokwon Yang, H. Kim
 * Created On      : Jan ?? 1999
 * Last Modified By: Seokwon Yang
 * Last Modified On: Sun Sep 19 23:25:10 1999
 * Copyright (C) University of Florida 1999
 */

package sentinel.led;

/** The EventType class contains the declaration of constants denoting
 *  SNOOP event operators. These constants are used in the definition of
 *  composite events.
 */
public class EventType {

   public final static EventType AND = new EventType("AND", 1);
   public final static EventType OR = new EventType("OR", 2);
   public final static EventType SEQ = new EventType("SEQ", 3);
   public final static EventType NOT = new EventType("NOT", 4);
   public final static EventType APERIODIC = new EventType("APERIODIC", 5);
   public final static EventType APERIODICSTAR = new EventType("APERIODICSTAR", 6);
   public final static EventType PLUS = new EventType("PLUS", 7);
   public final static EventType PERIODIC = new EventType("PERIODIC", 8);
   public final static EventType PERIODICSTAR = new EventType("PERIODICSTAR", 9);
   final static EventType PRIMITIVE = new EventType("PRIMITIVE", 10);
   final static EventType COMPOSITE = new EventType("COMPOSITE", 11);
   final static EventType TEMPORAL = new EventType("TEMPORAL", 12);

  private String name;
  private int id;

  EventType(String name, int id) {
    this.name = name;
    this.id = id;
  }

  public String getName() {
    return name;
  }

  int getId() {
    return id;
  }

  static String getName(int eventType) {
    if (eventType == 1)
      return ("AND");
    else if (eventType == 2)
      return ("OR");
    else if (eventType == 3)
      return ("SEQ");
    else if (eventType == 4)
        return ("NOT");
    else if (eventType == 5)
      return ("APERIODIC");
    else if (eventType == 6)
      return ("APERIODICSTAR");
    else if (eventType == 7)
      return ("PLUS");
    else if (eventType == 8)
      return ("PERIODIC");
    else if (eventType == 9)
      return ("PERIODICSTAR");
    else if (eventType == 10)
      return ("PRIMITIVE");
    else if (eventType == 11)
      return ("COMPOSITE");
    else if (eventType == 12)
      return ("TEMPORAL");
    else
     return ("UNDEFINED");
  }
}
