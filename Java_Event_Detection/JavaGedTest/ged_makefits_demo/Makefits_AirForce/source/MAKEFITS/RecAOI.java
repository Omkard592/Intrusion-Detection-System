package MAKEFITS;

public class RecAOI extends AOI{

  static boolean debug = DebugHelper.isDebugFlagTrue("debug");

  private float NBound ; // North bound
  private float SBound ; // South bound
  private float EBound ; // East bound
  private float WBound ; // West bound

  private float lgthX;  // size of bound in x axis
  private float lgthY;  // size of bound in y axis

  public RecAOI(){
  }


  /**
   * Constructs a composite event: aperidoic event
   *
   * @param zoneName : The name of AOI
   * @param latCenterPt : The latitude of the center
   * @param lonCenterPt : The longitude of the center
   * @param lgthX : The length of zone in x axis (0-180 degree) (lon)
   * @param lgthY : The length of zone in y axis (0-90 degree) (lat)
   *
   *   ***********************
   *   *                     *
   *   *        * center     *  lgthY
   *   *                     *
   *   ***********************
   *          lgth x
   *
   */


  public RecAOI(String zoneName,Latitude latCenterPt, Longitude lonCenterPt, int lgthX, int lgthY) {


    super(zoneName,latCenterPt,lonCenterPt);
    this.lgthX = lgthX;
    this.lgthY = lgthY;

    EBound = lonCenterPt.getValue () + lgthX/(float)2;
    WBound = lonCenterPt.getValue() - lgthX/(float)2;
    NBound = latCenterPt.getValue () + lgthY/(float)2;
    SBound = latCenterPt.getValue() - lgthY/(float)2;

    System.out.println("Show the boundary");
    System.out.println("East bound"+ EBound);
    System.out.println("West bound"+ WBound);
    System.out.println("North bound"+ NBound);
    System.out.println("South bound"+ SBound);

  }


	public float getEBound()
  	{
  	    return EBound;
    }

    	public float getWBound()
  	{
  	    return WBound;
    }

    	public float getNBound()
  	{
  	    return NBound;
    }

    	public float getSBound()
  	{
  	    return SBound;
    }



   /**
    * This method is to check whether the provided coordinator is in range or not
    * @param lat : The longitude of the interest track
    * @param lon : The longitude of the interest track
    *
    */


  public boolean inZone(Latitude lat, Longitude lon){
    if(debug){
      System.out.println("Target x,y = lon "+lon.getValue()+" lat"+ lat.getValue());
      System.out.println(toString ());
    }
    if( WBound <= lon.getValue() && EBound >= lon.getValue()){

     if (NBound >= lat.getValue() && SBound <= lat.getValue())
      return true;
     else                         {
     if(debug)
      System.out.println("Target x,y = lon "+lon.getValue()+" lat"+ lat.getValue());

      return false;
      }
    }
    else
    return false;
  }


  public String toString(){
    String s ="";
    if(debug){
    s = "The "+zoneName+" has a center at "+latCenterPt.getValue()+" lat degree and ";
    s = s+ ""+lonCenterPt.getValue()+ " lon degree. \n";
    s = s+"North bound "+NBound +"\n";
    s = s+"South bound "+SBound +"\n";
    s = s+"East bound "+EBound +"\n";
    s = s+"West bound "+WBound +"\n";
    }
    return s;
  }
}