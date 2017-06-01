/**
 * RuleOperatingMode.java --
 * Author          : Seokwon Yang
 * Created On      : Fri Jan  8 23:06:54 1999
 * Last Modified By: Seokwon Yang
 * Last Modified On: Fri Jan  8 23:06:56 1999
 * RCS             : $Id: header.el,v 1.1 1997/02/17 21:45:38 seyang Exp seyang $
 * Copyright (C) University of Florida 1999
 */

package sentinel.led;

/** This class contains symbolic constants that are used denote the status
 *  of an executing rule. These are used when the rules are scheduled for
 *  execution by the rule scheduler. Once a rule is scheduled, it will be
 *  in one of these operating modes until it finishes execution.
 */

class RuleOperatingMode {
  static final int READY = 0;
  static final int EXE = 1;
  static final int WAIT = 2;
  static final int FINISHED = 3;
}



