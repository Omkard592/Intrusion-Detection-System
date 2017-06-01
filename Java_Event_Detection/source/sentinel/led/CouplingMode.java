package sentinel.led;

/** The CouplingMode class contains the declaration of constants denoting
 *  the three rule coupling modes defined by Sentinel. These constants are used
 *  by user applications in rule definitions. The default coupling mode is the
 *  IMMEDIATE coupling mode.
 */
public class CouplingMode {
  public static final CouplingMode IMMEDIATE = new CouplingMode(0);
  public static final CouplingMode DEFERRED = new CouplingMode(1);
  public static final CouplingMode DETACHED = new CouplingMode(2);
  public static final CouplingMode DEFAULT = IMMEDIATE;

  private int coupling;

  CouplingMode(int coupling) {
    this.coupling = coupling;
  }

  int getId() {
    return coupling;
  }
}

