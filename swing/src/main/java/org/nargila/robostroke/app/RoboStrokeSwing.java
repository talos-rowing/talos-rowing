/*
 * Copyright (c) 2012 Tal Shalif
 *
 * This file is part of Talos-Rowing.
 *
 * Talos-Rowing is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Talos-Rowing is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Talos-Rowing.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.nargila.robostroke.app;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.io.File;
import java.util.Map.Entry;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import javax.swing.JFrame;

import org.nargila.robostroke.RoboStroke;
import org.nargila.robostroke.param.Parameter;
import org.nargila.robostroke.param.ParameterChangeListener;
import org.nargila.robostroke.param.ParameterService;

public class RoboStrokeSwing {

  private static Logger logger = Logger.getLogger(RoboStrokeSwing.class.getName());

  private JFrame frmTalosRowing;

  private final RoboStroke rs = new RoboStroke();

  private RoboStrokeAppPanel roboStrokeAppPanel;

  /**
   * Launch the application.
   */
  public static void main(final String[] args) {

    logger.info("launching Talos Rowing");

    EventQueue.invokeLater(new Runnable() {
      @Override
            public void run() {
        try {
          RoboStrokeSwing window = new RoboStrokeSwing();

          centerOnScreen(window.frmTalosRowing);

          window.frmTalosRowing.setVisible(true);

          if (args.length > 0) {
            File f = new File(args[0]);
            window.roboStrokeAppPanel.start(f, null);
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
  }

  private static void setProperty(String name, String value) {

    String prop = System.getProperty(name);

    if (prop == null) {
      logger.info(String.format("%s is not set - setting to %s", name, value));
      System.setProperty(name, value);
    }
  }

  private static void centerOnScreen(JFrame window) {
    Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();

    // Determine the new location of the window
    int w = window.getSize().width;
    int h = window.getSize().height;
    int x = (dim.width-w)/2;
    int y = (dim.height-h)/2;

    // Move the window
    window.setLocation(x, y);
  }

  /**
   * Create the application.
   */
  public RoboStrokeSwing() {

    initialize();

    loadParams(rs.getParameters());

    roboStrokeAppPanel.init(rs);

  }

  private void loadParams(ParameterService parameters) {


    final Preferences pref = Preferences.userNodeForPackage(getClass());

    for (Entry<String, Parameter> p: parameters.getParamMap().entrySet()) {
      String value = pref.get(p.getKey(), p.getValue().convertToString());
      parameters.setParam(p.getKey(), value);
    }

    parameters.addListener("*", new ParameterChangeListener() {

      @Override
      public void onParameterChanged(Parameter param) {
        pref.put(param.getId(), param.convertToString());
      }
    });
  }

  /**
   * Initialize the contents of the frame.
   */
  private void initialize() {
    frmTalosRowing = new JFrame();
    frmTalosRowing.setTitle("Talos Rowing");
    frmTalosRowing.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    roboStrokeAppPanel = new RoboStrokeAppPanel();
    frmTalosRowing.getContentPane().setLayout(new BorderLayout());
    frmTalosRowing.getContentPane().add(roboStrokeAppPanel, BorderLayout.CENTER);
    frmTalosRowing.pack();

  }
}
