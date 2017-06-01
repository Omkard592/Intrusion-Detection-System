/**
 * Condition.java
 * Author          : Seokwon Yang
 * Created On      : Fri Jul 23 19:27:19 1999
 * Last Modified By: Seokwon Yang
 * Last Modified On: Fri Jul 23 23:59:43 1999
 * Copyright (C) University of Florida 1999
 */

 package sentinel.led;

public abstract interface Condition {
  public abstract boolean check(Object objContext);
}
