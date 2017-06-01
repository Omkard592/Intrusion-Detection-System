
//Title:        Your Product Name
//Version:
//Copyright:    Copyright (c) 1999
//Author:       Weera Tanpisuth
//Company:      University of Texas, Arlington
//Description:  Your description

package MAKEFITS;

public abstract class AOI {

  protected String zoneName;
  protected Latitude latCenterPt ;   // center of the zone specified by latitude
  protected Longitude lonCenterPt ;  // center of the zone specified by longitude

  public AOI() {}

  public abstract boolean inZone(Latitude tLat, Longitude tLon); // target lat,lon

  public AOI(String zoneName,Latitude latCenterPt, Longitude lonCenterPt ){
    this.zoneName = zoneName;
    this.latCenterPt = latCenterPt;
    this.lonCenterPt = lonCenterPt;
  }
}