package MAKEFITS;

import java.io.*;
import java.text.*;

/**
 * A Latitude class.
 *
 */

public class Latitude{

  int degrees;
  int minutes;
  String hemisphere;
  float value;

  /**
   * Creates a new object from the degrees, minutes, seconds, and hemisphere
   * given.
   *
   * @param degrees the degress (0-90)
   * @param minutes the minutes (0-59)
   * @param hemisphere the hemisphere ("N" or "S")
   */

  public Latitude (int degrees, int minutes, String hemisphere){
    this.degrees = degrees;
    this.minutes = minutes;
    this.hemisphere = hemisphere;
    updateValue ();
  }

  /**
   * Creates a new object from the floating point value.
   *
   * @param value the value (negative is south, positive is north)
   */

  public Latitude (float value)
  {
    this.value = value;
  }

  /**
   * Returns the floating point representation (useful for calculations).
   *
   * @return floating point value (-90 to 90)
   */

  public float getValue ()
  {
    return value;
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


  /**
   * Updates the floating point value from the individual components.
   */

  void updateValue (){
    value = (float) (degrees + (float) minutes / 60.);
    if (hemisphere.equals ("S")){
      value *= -1.;
    }
  }
}




