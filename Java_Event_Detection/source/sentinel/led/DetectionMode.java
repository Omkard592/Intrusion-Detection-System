/**
 * DefRuleQueue.java --
 * Author          : Seokwon Yang, H. Kim
 * Created On      : Jan ?? 1999
 * Last Modified By: Seokwon Yang
 * Last Modified On: Sun Sep 19 23:25:10 1999
 * Copyright (C) University of Florida 1999
 */

package sentinel.led;

/**
 *  This class contains the declaration of constants denoting the Detection Modes
 *  defined by the Sentinel ECAAgent. These constants are used in the definition of
 *  primitive events.
 *
 *  The detection of a primitive event is processed (event propagation and rule
 *  execution) synchronously with the application if the primitive event is defined
 *  in the SYNCHRONOUS mode. In other words, the application waits until the event
 *  is processed. In the PARALLEL mode, the detection is processed in parallel
 *  with the application.
 */

public class DetectionMode {
  public final static DetectionMode SYNCHRONOUS = new DetectionMode(1);
  public final static DetectionMode PARALLEL = new DetectionMode(2);
  final static int synchronousMode = 1;
  final static int parallelMode = 2;
  private int detectMode;

  DetectionMode(int detectMode) {
	  this.detectMode = detectMode;
  }

  int getId() {
	  return detectMode;
  }
}
