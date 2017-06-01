/**
 * Context.java --
 * Author          : Seokwon Yang
 * Created On      : Fri Jan  8 23:06:54 1999
 * Last Modified By: Seokwon Yang
 * Last Modified On: Fri Jan  8 23:06:56 1999
 * RCS             : $Id: header.el,v 1.1 1997/02/17 21:45:38 seyang Exp seyang $
 * Copyright (C) University of Florida 1999
 */

package sentinel.led;

/** The ParamContext class contains the declaration of constants denoting
 *  the four parameter contexts defined by Sentinel. These constants are used
 *  by user applications in rule definitions. The default context is the RECENT
 *  context.
 */
public class ParamContext {
  public static final ParamContext RECENT = new ParamContext(0);
  public static final ParamContext CHRONICLE = new ParamContext(1);
  public static final ParamContext CONTINUOUS = new ParamContext(2);
  public static final ParamContext CUMULATIVE = new ParamContext(3);
  public static final ParamContext DEFAULT = RECENT;

  static final int recentContext = 0;
  static final int chronContext = 1;
  static final int contiContext = 2;
  static final int cumulContext = 3;

  private int context;

  ParamContext(int context) {
    this.context = context;
  }

  public int getId() {
    return context;
  }

  public static String getContext(int context) {
    if ((context == 0) || (context == 4))
      return("RECENT");
    else if (context == 1)
      return("CHRONICLE");
    else if (context == 2)
      return("CONTINUOUS");
    else if (context == 3)
      return("CUMULATIVE");
    else
      return("UNDEFINED");
  }
}

