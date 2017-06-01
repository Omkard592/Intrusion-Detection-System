package MAKEFITS;

/**
 * Author          : Weera Tanpisuth
 *                   Robert Yowell
 * Created On      : Mon Jul 19 17:11:11 1999
 * Last Modified By: Robert Yowell
 * Last Modified On: Sat Oct 7 2000
 * Copyright (C) University of Florida 1999
 */

import java.util.*;
import java.awt.*;
import java.applet.Applet;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.ImageIcon;

class GraphPanel extends Panel implements Runnable {

  static boolean debug = DebugHelper.isDebugFlagTrue("debug");

  // Graph
  Graph graph;

  /** Backgroud map */
  ImageIcon map = new ImageIcon(TrackImages.map);

  /**
   * Array of track nodes
   */
  static  Node nodes[] = new Node[100];


  /**
   * Constructor
   */
  GraphPanel(Graph graph) {
    this.graph = graph;
    this.setCoordinatesForAOI();
    Worker = new Thread(this);
  }


  /**
   * Painting thread periodically updates the graphic
   */
  Thread Worker;

  /**
   * Periodically repaints the graphic
   */
  public void run(){
    while(true){
      Thread me = Thread.currentThread();
      repaint();
      try{
	Thread.sleep(250);
        repaint();
      }
      catch (InterruptedException e){
        break;
      }
    }
  }

  /**
   * Start worker thread method
   */
  public void start() {
    Worker = new Thread(this);
    Worker.start();
  }

  /**
   * Stop worker thread method
   */
  public void stop() {
    Worker = null;
  }


  /**
   * Add node into the map, and keep it in the array of track nodes
   */

  public int addNode(String lbl, Latitude lat, Longitude lon,  String category){
    Node n = new Node(lbl,category);
//    Node n = new Node();
    // n.setLabelLocation(-10, -10);
    n.setX(GraphPanel.mapLong(lon));
    n.setY(GraphPanel.mapLat(lat));

    //    System.out.println("Show category ::"category);
    // n.setLbl(lbl);
    // n.setCategory(category);
    n.setTrackType(category);
    nodes[nnodes] = n;

    // added by weera
    // System.out.println("Show the data on the screen");
    // System.out.println("-------------------------"+lon.getValue()+" "+lat.getValue());
    // System.out.println("-------------------------"+GraphPanel.mapLong(lon)+" "+GraphPanel.mapLat(lat));
    repaint();
    // added by weera

    return nnodes++; // if you can not find the node from canvas, just return -1
  }

  /**
   * Find the node index
   */
  public int findNode(String lbl) {
    for (int i = 0 ; i < nnodes ; i++) {
      if (nodes[i].getLbl().equals(lbl)) {
    	return i;
      }
    }
    return -1; // if you can not find the node from canvas, just return -1
  }

/*  public int removeNode(String lbl){
    Node n = nodes[this.findNode(lbl)];
    n = null;
    return nnodes--;
  }
*/


  /**
   * Map the longitude into pixel
   */

  public static double mapLong(Longitude lon){
    double lonbar = 0;
    double xPixScale = 0;

    try{
      xPixScale = GraphPanel.xLong/(GraphPanel.longMax - GraphPanel.longMin);
    }catch(Exception e){
      System.out.println("Divide by zero exception caught in Class GraphPanel"+e);
    }

    if(lon.hemisphere.equals("W")){
     	lonbar = (double)lon.getValue() - longMin;
    }else if(lon.hemisphere.equals("E")){
     	lonbar = (double)lon.getValue() - longMin;
    }else
      lonbar = -1;
    return xPixScale*lonbar;
  }

  /**
   * Map the longitude value into pixel
   */

  public static double mapLong(int lon){
    double lonbar = 0;
    double xPixScale = 0;

    try{
      xPixScale = GraphPanel.xLong/(GraphPanel.longMax - GraphPanel.longMin);
    }catch(Exception e){
      System.out.println("Divide by zero exception caught in Class GraphPanel"+e);
    }

    if(lon < 0){
      lonbar = (double)lon - longMin;
    }else if(lon >= 0){
      lonbar = (double)lon - longMin;
    }else
      lonbar = -1;
    return xPixScale*lonbar;
 }

  /**
   * Map the latitude value into pixel
   */

  public static double mapLat(Latitude lat){
    double latbar = 0;
    double yPixScale = 0;
    try{
      yPixScale = (double)GraphPanel.yLat/((double)GraphPanel.latMax - (double)GraphPanel.latMin);
    }catch(Exception e){
      System.out.println("Divide by zero exception caught in Class GraphPanel"+e);
    }

    if(lat.hemisphere.equals("N")){
      latbar = latMax - (double)lat.getValue();
    }else if(lat.hemisphere.equals("S")){
      latbar = latMax - (double)lat.getValue();
    }else
      latbar = -1;
    return yPixScale*latbar;
  }

  /**
   * Map the latitude value into pixel
   */

  public static double mapLat(int lat){
    double latbar = 0;
    double yPixScale = 0;
    try{
      yPixScale = (double)GraphPanel.yLat/((double)GraphPanel.latMax - (double)GraphPanel.latMin);
    }catch(Exception e){
      System.out.println("Divide by zero exception caught in Class GraphPanel"+e);
    }

    if(lat < 0){
      latbar = latMax - (double)lat;
    }else if(lat >= 0){
      latbar = latMax - (double)lat;
    }else
      latbar = -1;
    return yPixScale*latbar;
  }


  /**
   * update location of the node
   * lbl is the track name
   */

  public int changeLocation(String lbl, Longitude lon, Latitude lat){
    double x = GraphPanel.mapLong(lon);
    double y = GraphPanel.mapLat(lat);

    for (int i = 0 ; i < nnodes ; i++){
      if (nodes[i].getLbl().equals(lbl)){
	nodes[i].setX(x);
	nodes[i].setY(y);
	return i;
      }
    }
    return -1; // if you can not find the node from canvas, just return -1
  }

  /**
   * Set warning to fliker the track image
   */
  public int setWarning(String lbl) {
    for (int i = 0 ; i < nnodes ; i++) {
      if (nodes[i].getLbl().equals(lbl)){
        nodes[i].setWarning(true);
      }
    }
    return -1; // if you can not find the node from canvas, just return -1
  }

  /**
   * Unset warning to stop flikering the track image
   */

  public int unsetWarning(String lbl) {
    for (int i = 0 ; i < nnodes ; i++) {
      if (nodes[i].getLbl().equals(lbl)) {
       nodes[i].setWarning(false);
      }
    }
    return -1; // if you can not find the node from canvas, just return -1
  }


  public void setTrackHistory(){
    this.trackHistory = true;
  }

  public void unsetTrackHistory(){
    this.trackHistory = false;
  }

  public boolean getTrackHistory(){
    return this.trackHistory;
  }



  public synchronized void update(Graphics g) {
    Dimension d = getSize();
    if ((offscreen == null) || (d.width != offscreensize.width) || (d.height != offscreensize.height)) {
      offscreen = createImage(d.width, d.height);
      offscreensize = d;
      offgraphics = offscreen.getGraphics();
      if(this.trackHistory)
        paintBackground(offgraphics, map);    // if you want the track history to show
    }
    g.setColor(Color.black);
    if(!this.trackHistory)
      paintBackground(offgraphics, map);  // if you don't want the history to show

    // draw tracks
    for (int i = 0 ; i < nnodes ; i++)
      nodes[i].paint(offgraphics, this);
    g.drawImage(offscreen, 0, 0, this);
  }


  /**
   * Set the AOI zone
   */
  public void setCoordinatesForAOI(){
    Track t = new Track("test");
    float[] coords = t.getAOIZoneCoordinates();

     x1 = (int) coords[1];
     y1 = (int) coords[2];
     x2 =  (int)coords[1];
     y2 = (int) coords[3];
     x3 =  (int)coords[0];
     y3 = (int) coords[3];
     x4 = (int) coords[0];
     y4 = (int) coords[2];
  }

  public void paintBackground(Graphics g, ImageIcon imageIcon){
    image = imageIcon.getImage();
    g.drawImage(image, 0,0, this);
    g.setColor (Color.red);

    /* xx, yy are the coordinators in the pixel unit*/

    int xx1 = (int) mapLong(x1);
    int xx2 = (int) mapLong(x2);
    int xx3 = (int) mapLong(x3);
    int xx4 = (int) mapLong(x4);
    int yy1 = (int) mapLat(y1);
    int yy2 = (int) mapLat(y2);
    int yy3 = (int) mapLat(y3);
    int yy4 = (int) mapLat(y4);

    /**
     * Draw the zone
     */
    g.drawLine(xx1, yy1, xx2, yy2);
    g.drawLine(xx2, yy2, xx3, yy3);
    g.drawLine(xx3, yy3, xx4, yy4);
    g.drawLine(xx4, yy4, xx1, yy1);
  }

  /** Boundary of the map*/
  public static int latMin, latMax, longMin, longMax;

  public void setExtents(int latMin, int latMax, int longMin, int longMax){
    this.latMin= latMin;
    this.latMax = latMax;
    this.longMin = longMin;
    this.longMax = longMax;
  }

  public int[] getExtents(){
    int[] extents = new int[4];
    extents[0] = latMin;
    extents[1] = latMax;
    extents[2] = longMin;
    extents[3] = longMax;
    return extents;
  }



  public static int xLong = 500;
  public static int yLat = 334;

  /** Number of track in the track array */
  int nnodes;

  private Image image;

  private int x1, y1, x2, y2, x3, y3, x4, y4;
  private boolean trackHistory = false;
  Image offscreen;
  Dimension offscreensize;
  Graphics offgraphics;
  boolean haveNotDrawn = true;
  int oldlat = 0;
  int oldlon = 0;
  final Color warningColor = Color.red;
  final Color edgeColor = Color.black;
  final Color nodeColor = new Color(250, 220, 100);





  /*public static void main(String args[]){
    Graph a = new Graph();
    GraphPanel g = new GraphPanel(a);
    g.setCoordinatesForAOI();
  }*/
}
































/*
  public GraphPanel(int latMin, int latMax, int longMin, int longMax){
    this.latMin = latMin;
    this.latMax = latMax;
    this.longMin = longMin;
    this.longMax = longMax;
  }

  public GraphPanel(){
    this.latMin = latMin;
    this.latMax = latMax;
    this.longMin = longMin;
    this.longMax = longMax;
  }
*/

/*  public int changeLocation(String lbl, double x, double y){
    for (int i = 0 ; i < nnodes ; i++){
      if (nodes[i].getLbl().equals(lbl)){
        nodes[i].setX(x);
	nodes[i].setY(y);
	return i;
      }
    }
    return -1; // if you can not find the node from canvas, just return -1
 }
*/






/*  public void paintNode(Graphics g, Node n) {
    n.paint(g, this);
  }
*/

/*  public int addNode(String lbl, double x, double y, String category){
    Node n = new Node();
    n.setX(x);
    n.setY(y);
    n.setLbl(lbl);
    n.setCategory(category);
//	n.x = x;
//	n.y = y;
//	n.lbl = lbl;
//	n.position = position;
	  nodes[nnodes] = n;
	  return nnodes++;
  }
*/

