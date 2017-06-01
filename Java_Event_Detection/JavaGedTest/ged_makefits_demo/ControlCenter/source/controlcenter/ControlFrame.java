package controlcenter;

import java.awt.*;
import javax.swing.*;
import com.borland.jbcl.layout.*;
import java.awt.event.*;
import javax.swing.border.*;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:
 * @author
 * @version 1.0
 */

public class ControlFrame extends JFrame {
  JPanel jPanel1 = new JPanel();
  JPanel statusPanel = new JPanel();
  JPanel menu = new JPanel();
  XYLayout xYLayout2 = new XYLayout();
  XYLayout xYLayout1 = new XYLayout();
  XYLayout xYLayout4 = new XYLayout();
  TitledBorder titledBorder1;
  JTextField statusField = new JTextField();
  JTabbedPane jTabbedPane1 = new JTabbedPane();
  JScrollPane additionTab = new JScrollPane();
  JScrollPane alertTab = new JScrollPane();
  JTextArea alertTextArea = new JTextArea();
  JTextArea infoTextArea = new JTextArea();
  JLabel title = new JLabel();
  BorderLayout borderLayout1 = new BorderLayout();

  public ControlFrame() {
    try {
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }


  private void jbInit() throws Exception {
    titledBorder1 = new TitledBorder("");
    this.getContentPane().setLayout(xYLayout2);
    menu.setBackground(Color.lightGray);
    menu.setForeground(Color.white);
    menu.setBorder(BorderFactory.createRaisedBevelBorder());
    menu.setLayout(borderLayout1);
    statusPanel.setBackground(Color.lightGray);
    statusPanel.setBorder(BorderFactory.createRaisedBevelBorder());
    statusPanel.setLayout(xYLayout4);
    jPanel1.setLayout(xYLayout1);
    jPanel1.setBackground(Color.white);
    xYLayout2.setWidth(310);
    xYLayout2.setHeight(442);
    statusField.setBackground(Color.lightGray);
    statusField.setBorder(BorderFactory.createLoweredBevelBorder());
    statusField.setPreferredSize(new Dimension(0, 0));
    statusField.setText("Status ...");
    alertTab.getViewport().setBackground(Color.white);
    alertTab.setPreferredSize(new Dimension(20, 20));
    additionTab.getViewport().setBackground(Color.white);
    additionTab.setPreferredSize(new Dimension(20, 20));
    title.setFont(new java.awt.Font("Monospaced", 1, 12));
    title.setBorder(BorderFactory.createEtchedBorder());
    title.setHorizontalAlignment(SwingConstants.CENTER);
    title.setHorizontalTextPosition(SwingConstants.CENTER);
    title.setText("Control Center");
    this.getContentPane().add(menu, new XYConstraints(4, 4, 302, 38));
    menu.add(title, BorderLayout.CENTER);
    this.getContentPane().add(jPanel1, new XYConstraints(6, 47, 297, 354));
    jPanel1.add(jTabbedPane1, new XYConstraints(0, 1, 299, 354));
    jTabbedPane1.add(alertTab, "alert");
    alertTab.getViewport().add(alertTextArea, null);
    jTabbedPane1.add(additionTab, "additional info");
    this.getContentPane().add(statusPanel, new XYConstraints(2, 404, 307, 35));
    statusPanel.add(statusField, new XYConstraints(3, 5, 166, 22));
    additionTab.getViewport().add(infoTextArea, null);
  }

  void startButton_actionPerformed(ActionEvent e){
   Controller.startTrackApps();
  }

  void exitButton_actionPerformed(ActionEvent e) {
    System.exit(0);
  }


  JTextArea getAlertTextArea(){
    return alertTextArea;
  }

  JTextArea getInfoTextArea(){
    return infoTextArea;
  }
}