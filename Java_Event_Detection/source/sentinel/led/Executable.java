/**
 * Executable.java --
 * Author          : Seokwon Yang, H. Kim
 * Created On      : Jan ?? 1999
 * Last Modified By: Seokwon Yang
 * Last Modified On: Sun Sep 19 23:25:10 1999
 * Copyright (C) University of Florida 1999
 */

package sentinel.led;
import java.util.Vector;

/** The Executable interface is implemented by the Rule class. The interface
 *  contains methods to execute the Rule and also methods that return the
 *  attributes of a rule such as its context, coupling mode, priority and name.
 */

interface Executable {
  // implemented by the Rule class for executing rules
  // on events (primitive or composite)
  void execute(ListOfParameterLists paramLists, int context);
  ParamContext getContext();
  CouplingMode getCoupling();
  int getPriority();
  String getName();
}
