package MAKEFITS;

import java.awt.*;
import javax.swing.JPanel;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:
 * @author
 * @version 1.0
 */

public class MonitoringAreaPanel extends JPanel {
   private Image image ;
  private Image  image1;
  private Image  image2;
  private Image  image3;

  public MonitoringAreaPanel() {
    try {
      jbInit();
    }
    catch(Exception ex) {
      ex.printStackTrace();
    }
  }

  void jbInit() throws Exception {

  }

  public void paintComponent(Graphics g){
    super.paintComponents(g);

  }

  public synchronized void update(Graphics g) {

  }

}
