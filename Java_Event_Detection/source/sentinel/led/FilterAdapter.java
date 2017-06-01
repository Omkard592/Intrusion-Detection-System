/**
 * FilterAdapter.java --
 * Author          : Seokwon Yang, H. Kim
 * Created On      : Jan ?? 1999
 * Last Modified By: Seokwon Yang
 * Last Modified On: Thu Aug 12 02:33:20 1999
 * Copyright (C) University of Florida 1999
 */

package sentinel.led;

public class FilterAdapter implements Filter {
  public boolean check(Reactive reactive) {return false;}
  public boolean check() {return false;}
}
