/**
 * TriggerMode.java --
 * Author          : Seokwon Yang
 * Created On      : Fri Jan  8 23:06:54 1999
 * Last Modified By: Seokwon Yang
 * Last Modified On: Fri Jan  8 23:06:56 1999
 * RCS             : $Id: header.el,v 1.1 1997/02/17 21:45:38 seyang Exp seyang $
 * Copyright (C) University of Florida 1999
 */

package sentinel.led;

/** The TriggerMode class contains the declaration of constants denoting
 *  the two rule triggering modes defined by Sentinel. These constants are used
 *  by user applications in rule definitions. The default trigger mode is NOW.
 */
public class TriggerMode {
  public static final TriggerMode NOW = new TriggerMode(0);
  public static final TriggerMode PREVIOUS = new TriggerMode(1);
  public static final TriggerMode DEFAULT = NOW;
  private int triggerMode;

  TriggerMode(int triggerMode) {
    this.triggerMode = triggerMode;
  }
}

