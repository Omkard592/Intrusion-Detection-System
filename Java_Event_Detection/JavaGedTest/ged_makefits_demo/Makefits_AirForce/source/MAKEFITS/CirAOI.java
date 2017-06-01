//Title:        Your Product Name
//Version:
//Copyright:    Copyright (c) 1999
//Author:       Weera Tanpisuth
//Company:      University of Texas, Arlington
//Description:  Your description

package MAKEFITS;

public class CirAOI extends AOI{

  float range; // in degree
  /**
   * Constructs a composite event: aperidoic event
   *
   * @param zoneName : The name of AOI
   * @param latCenterPt : The latitude degree of the center
   * @param lonCenterPt : The longitude degree of the center
   * @param range : The radius form the the center to the bound
   *
   */

  public CirAOI(String zoneName,Latitude latCenterPt, Longitude lonCenterPt, float range){

    super(zoneName,latCenterPt, lonCenterPt);
    this.range = range;

  }

  /**
    * This method is to check whether the provided coordinator is in range or not
    * @param lat : The longitude of the interest track
    * @param lon : The longitude of the interest track
    *
    */
  public boolean inZone(Latitude tlat, Longitude tlon){
    if( calculateDistance(tlat.getValue(),tlon.getValue()) <= range)
      return true;
    else
      return false;
  }


  private float calculateDistance(float tlat, float tlon) {

    return((float)(Math.sqrt(Math.pow((latCenterPt.getDegrees() - tlat),2) +
    			  Math.pow((lonCenterPt.getDegrees() - tlon), 2))));

  }


  public String toString(){
    String s = "The "+zoneName+" has a center at "+latCenterPt.getDegrees()+" lat degree and ";
    s = s+ ""+lonCenterPt+ " lon degree. \n";
    return s;
  }
}