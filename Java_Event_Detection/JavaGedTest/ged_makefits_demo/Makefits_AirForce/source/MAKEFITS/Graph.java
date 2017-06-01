// Graph.java --
// Author          : Seokwon Yang
//                   Robert Yowell
// Created On      : Mon Jul 19 17:11:11 1999
// Last Modified By: Robert Yowell
// Last Modified On: Sat Oct 7 2000
// Copyright (C) University of Florida 1999
//
package MAKEFITS;

import java.util.*;
import java.awt.*;
import java.applet.Applet;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.ImageIcon;

/*
 * This class displays the tracks on a map.  The map is not flexible. It
 * only displays a map at thes lat/long coordinates.
 *
 */

 public class Graph extends JPanel {

 /*

                            Map Coordinates

             Latitude          Longitude                  Elevation
Tile    Minimum  Maximum   Minimum  Maximum   Minimum  Maximum  Mean  Std.Dev.
-------  ----------------   ----------------   --------------------------------
W140N40    -10       40       -140    -100       -79      4328   1321     744

*/

  static boolean debug = DebugHelper.isDebugFlagTrue("debug");

  GraphPanel panel;
  Panel controlPanel;

  int nnodes;
  static  Node nodes[] = new Node[100];   // Up to 100 track objects can be displayed

  private int xLong = 500;        // 600 pixels in the horizontal
  private int yLat = 334;         // 600 pixels in the vertical

  private static int latMin = -10;
  private static int latMax = 40;
  private static int longMin = -140;
  private static int longMax = -100;


  public void init() {
    setLayout(new BorderLayout());
    panel = new GraphPanel(this);
    panel.setExtents(latMin, latMax, longMin, longMax);
    add("Center", panel);
  }

  public Graph(){
    this.setSize(xLong, yLat);
  }

  public void destroy() {
    remove(panel);
    remove(controlPanel);
  }

  public void start() {
    panel.start();
  }

  public void stop() {
    panel.stop();
  }

  public int addNode(String lbl, Latitude lat,  Longitude lon, String position){
    return panel.addNode(lbl, lat, lon, position);
  }

  public int findNode(String lbl) {
    return panel.findNode(lbl);
  }

  public int setWarning(String lbl) {
    if(debug)
      System.out.println("Changing color in setWarning on label" + lbl);
    return panel.setWarning(lbl);
  }

  public int unsetWarning(String lbl) {
    if(debug)
      System.out.println("Changing color in unsetWarning on label" + lbl);
    return panel.unsetWarning(lbl);
  }

  public int changeLocation(String lbl,  Latitude lat, Longitude lon){
    return panel.changeLocation(lbl, lon, lat);
  }
}