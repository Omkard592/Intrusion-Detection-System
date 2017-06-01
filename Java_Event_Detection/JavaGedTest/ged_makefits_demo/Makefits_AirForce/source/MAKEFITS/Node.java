package MAKEFITS;

// Node.java --
// Author          : Robert Yowell
//
// Created On      : Mon Jul 19 17:11:11 1999
// Last Modified By: Robert Yowell
// Last Modified On: Sat Oct 7 2000
//

import java.util.*;
import java.awt.*;
import java.applet.Applet;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.ImageIcon;

class Node{

  static boolean debug = DebugHelper.isDebugFlagTrue("debug");

  /** x coordinator */
  private double x;

  /** y coordinator */
  private double y;

  private double oldx;

  private double oldy;

  private Image image_hostile = null;

  private Image image_friendly = null;

  private Image image;


  private String trakNm ="Unknown";





   private boolean warning = false;
   private boolean isBlink = false;
   private boolean toggleFlag = false;
   private String lbl; // Track name
   private String category; //"+", "O", "-", "*"


  public Node(String trackNm,String trackType){
    warning = true;
    isBlink = false;
    toggleFlag = false;
    lbl = trackNm; // Track name
    this.trackType = trackType;
    setTrackType(trackType);
  }


  public void paint(Graphics g,  Panel graph){
    if (isBlink){
      toggleFlag = !toggleFlag;
      if (toggleFlag){
        image = image_friendly;
      }	else
       	image = image_hostile;
    }
    else
      image = image_friendly;
    g.setColor(Color.green);
    Font f = new Font("Monospaced", Font.BOLD, 16);
    g.setFont(f);
    g.drawString(lbl, (int)x-10,(int)y+25);
    g.setColor(Color.red);
    g.drawImage(image, (int)x - 16 , (int)y - 16, graph);
  }


  public boolean getWarning(){
    return isBlink;
  }

  public void setWarning(boolean warning){
    this.isBlink = warning;
  }

  public boolean getToggleFlag(){
    return toggleFlag;
  }

  public void setToggleFlag(boolean toggleFlag){
    this.toggleFlag = toggleFlag;
  }

  public boolean getisBlink(){
    return isBlink;
  }

  public void setisBlink(boolean isBlink){
    this.isBlink = isBlink;
  }

  public void unsetisBlink(){
    this.isBlink = false;
  }

  public String getLbl(){
    return lbl;
  }

  public void setLbl(String lbl){
    this.lbl = lbl;
  }


 String trackType;
  /**
   * Get type of the track
   * CA::Commerical Airplane
   * NS::Navy Ship
   * JF::Jet Fighter
   * HC::Helicopter
   */
  public String getType(){
    return trackType;
  }

  public void setTrackType(String trackType){
    this.trackType = trackType;
    if(trackType.equals("CA")){
      if(debug)
	System.out.println("Commerial Airplane " + x +"," + y);
      image_hostile  = new  ImageIcon(TrackImages.commPlaneAlert).getImage();
      image_friendly = new  ImageIcon(TrackImages.commPlane).getImage();
    }
    else if(trackType.equals("NS")){
      if(debug)
	System.out.println("Navy Ship" + x +"," + y);
      image_hostile  = new ImageIcon(TrackImages.navyShipAlert).getImage();
      image_friendly = new ImageIcon(TrackImages.navyShip).getImage();
    }
    else if(trackType.equals("JF")){
      if(debug)
	System.out.println("Jet Fighter" + x +"," + y);
	image_hostile = new ImageIcon(TrackImages.jetFighterAlert).getImage();
	image_friendly =  new ImageIcon(TrackImages.jetFighter).getImage();
    }
    else if(trackType.equals("HC")){
     if(debug)
      System.out.println("Helicopter" + x +"," + y);
	image_hostile = new ImageIcon(TrackImages.copterAlert).getImage();
	image_friendly =  new ImageIcon(TrackImages.copter).getImage();
   }
    else{
      System.err.println("ERROR This track type is not defined in this sytem ::"+trackType );
      System.exit(0);
    }
  }


  public String getCategory(){
    return category;
  }

  public double getX(){
    return x;
  }

  public double getY(){
    return y;
  }

  public void setY(double y){
    this.y = y;
  }

  public void setX(double x){
    this.x = x;
  }

  public void setTrakNm(String trakNm){
    this.trakNm = trakNm;
  }

  public String getTrakNm(){
    return trakNm;
  }






/*  public void setCategory(String category){
    this.category = category;
    if(category.equals("+"))
     {
      if(debug)
	    System.out.println("Airplane " + x +"," + y);
	    image_hostile = airplane_alert.getImage();
	    image_friendly =  airplane.getImage();
     }
     else if(category.equals("0"))
     {
           if(debug)
	    System.out.println("Ship" + x +"," + y);
	    image_hostile = ship_alert.getImage();
	    image_friendly =  ship.getImage();
	 }
	 else if(category.equals("-"))
     {
           if(debug)
	    System.out.println("Sub" + x +"," + y);
	    image_hostile = submarine_alert.getImage();
	    image_friendly =  submarine.getImage();
	 }
     else if(category.equals("*"))
     {
        System.out.println("ownship" + x +"," + y);
	    image_hostile = ownship.getImage();
	    image_friendly =  ownship.getImage();
	 }

   }
*/

/*
   private ImageIcon airplane = new ImageIcon("Airtrack.gif");
   private ImageIcon map = new ImageIcon("w140n40.gif");
   private ImageIcon airplane_alert = new ImageIcon("Airtrackh.gif");
   private ImageIcon ship = new ImageIcon("sur2.gif");
   private ImageIcon ship_alert = new ImageIcon("sur2h.gif");
   private ImageIcon submarine = new ImageIcon("sub2.gif");
   private ImageIcon submarine_alert = new ImageIcon("sub2h.gif");

*/
/*
   public Node(){
      x=0;
      y=0;
      image_hostile = ownship.getImage();
      image_friendly = ownship.getImage();

    //  image_friendly = new ImageIcon(TrackImages.navyShip).getImage();
    //  image_hostile = new ImageIcon(TrackImages.navyShipAlert).getImage();
      warning = true;
      isBlink = false;
      toggleFlag = false;
      lbl = "ownship"; // Track name
//      category = "+"; //"+", "O", "-", "*"
      category = "CA"; //"+", "O", "-", "*"

   }
*/
//   private ImageIcon ownship = new ImageIcon("ownshipm.gif");
}

