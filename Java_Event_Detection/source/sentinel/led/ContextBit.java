package sentinel.led;
import java.util.*;

/** The ContextBit class denotes the four bit pattern associated with an event in the
 *  event table. The first bit denotes the RECENT context, the second bit denotes
 *  CHRONICLE context, the third bit denotes CONTINUOUS context and the fourth bit
 *  denotes CUMULATIVE context.
 *
 *  This class also contains methods that support various operations on the bit pattern
 *  such as setting and resetting particular bits etc.
 */
class ContextBit {

  final static byte RECENT = (byte)8;			// 1000
  final static byte CHRONICLE = (byte)4;		// 0100
  final static byte CONTINUOUS = (byte)2;		// 0010
  final static byte CUMULATIVE = (byte)1;		// 0001

  private static boolean eventDetectionDebug = Utilities.isDebugFlagTrue("eventDetectionDebug");

  static void print(byte b) {
    //System.out.println("byte to print = " + b);
    int set;
    for(int i = 3; i >=0; i--){
      set = (1 << i) & b;
      if (set != 0){
        if(eventDetectionDebug)
	  System.out.print("1");
      }
      else{
        if(eventDetectionDebug)
	System.out.print("0");
      }
    }
    System.out.println("");
  }

  static byte initialize() {
    return RECENT + CHRONICLE + CONTINUOUS + CUMULATIVE;
  }

  static byte and(byte first, byte second) {
    return (byte)(first & second);
  }

  static byte or(byte first, byte second) {
    return (byte)(first | second);
  }

  static byte clearRecent(byte input) {
    if(isRecent(input))
      return (byte)(input - RECENT);    // 15 => 1111
    else return input;
  }

  static byte clearChronicle(byte input) {
    if(isChronicle(input))
      return (byte)(input- CHRONICLE);
    else return input;
  }

  static byte clearContinuous(byte input) {
    if(isContinuous(input))
      return (byte)(input- CONTINUOUS);
    else
      return input;
  }

  static byte clearCumulative(byte input) {
    if(isCumulative(input))
      return (byte)(input- CUMULATIVE);
    else
      return input;
  }

  static boolean isAllZero(byte input) {
    return (input == 0);
  }

  static boolean isRecent(byte bits) {
    if((bits & RECENT) == RECENT)
      return true;
    else return false;
  }

  static boolean isChronicle(byte bits) {
    if((bits & CHRONICLE) == CHRONICLE)
      return true;
    else return false;
  }

  static boolean isContinuous(byte bits) {
    if((bits & CONTINUOUS) ==CONTINUOUS)
      return true;
    else return false;
  }

  static boolean isCumulative(byte bits) {
    if((bits & CUMULATIVE) == CUMULATIVE)
      return true;
    else return false;
  }

  static byte setRecent(byte input) {
    if(isRecent(input))
      return input;
    else return (byte) (input + RECENT);
  }

  static byte setChronicle(byte input) {
    if(isChronicle(input)) return input;
    else return (byte)(input + CHRONICLE);
  }

  static byte setContinuous(byte input) {
    if(isContinuous(input)) return input;
    else return (byte)(input + CONTINUOUS);
  }

  static byte setCumulative(byte input) {
    if(isCumulative(input)) return input;
    else return (byte)(input + CUMULATIVE);
  }
}
