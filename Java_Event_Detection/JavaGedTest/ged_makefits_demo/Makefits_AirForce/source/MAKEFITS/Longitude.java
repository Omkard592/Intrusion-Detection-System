package MAKEFITS;
import java.io.*;
import java.text.*;

public class Longitude{

  int degrees;
  int minutes;
  String hemisphere;
  float value;

  /**
   * Creates a new object from the degrees, minutes, seconds, and hemisphere
   * given.
   *
   * @param degress the degrees (0-180)
   * @param minutes the minutes (0-59)
   * @param hemisphere the hemisphere ("E" or "W")
   */

  public Longitude (int degrees, int minutes, String hemisphere)
  {
    this.degrees = degrees;
    this.minutes = minutes;
    this.hemisphere = hemisphere;

    updateValue ();
  }

  /**
   * Creates a new object from the floating point value.
   *
   * @param value the value (negative is west, positive is east)
   */

  public Longitude (float value)
  {
    this.value = value;
  }

  /**
   * Returns the floating point representation (useful for calculations).
   *
   * @return floating point value (-180 to 180)
   */

  public float getValue (){
    return value;
  }

  /**
   * Updates the floating point value from the individual components.
   */

  void updateValue (){

    value = (float) (degrees + (float) minutes / 60.);
    if (hemisphere.equals ("W")){
      value *= -1.;
    }
  }



  /**
   * Returns the degrees portion.
   *
   * @return degrees
   */

  public int getDegrees (){
    return degrees;
  }

  /**
   * Returns the minutes portion.
   *
   * @return minutes
   */

  public int getMinutes (){
    return minutes;
  }
}

