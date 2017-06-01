/**
 * EventCategory.java --
 * Author          : Seokwon Yang, H. Kim
 * Created On      : Jan ?? 1999
 * Last Modified By: Seokwon Yang
 * Last Modified On: Sun Sep 19 23:25:10 1999
 * Copyright (C) University of Florida 1999
 */

package sentinel.led;

/** This class contains the symbolic constants used for classifying a primitive event
 *  node in the event graph as local, remote or temporal. By default, the LOCAL
 *  classifier is assigned to a primitive event node.
 */
class EventCategory {
    static final int LOCAL = 0;
    static final int REMOTE = 1;
    static final int TEMPORAL = 2;

   //<GED>
    static final int GLOBAL = 3;
   //<\GED>
    static final int DEFAULT = LOCAL;
}

