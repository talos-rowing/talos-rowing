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
package org.nargila.robostroke.ui.swing;


import org.junit.After;
import org.junit.Before;
import org.nargila.robostroke.RoboStroke;
import org.nargila.robostroke.ui.graph.UpdatableGraphBase;
import org.nargila.robostroke.ui.graph.swing.SwingGraphViewBase;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public abstract class GraphTestBase<T extends SwingGraphViewBase<? extends UpdatableGraphBase>> {

    protected T graph;
    private JFrame frame;
    protected RoboStroke rs;

    protected abstract T createGraph();

    @Before
    public void setUp() throws Exception {
        rs = new RoboStroke();
        graph = createGraph();
        graph.disableUpdate(false);
        graph.setPreferredSize(new Dimension(400, 200));
        frame = new JFrame();
        frame.add(graph, BorderLayout.CENTER);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);


    }

    @After
    public void tearDown() {
        frame.dispose();
    }

    protected void startRs() throws Exception {
        rs.setFileInput(new File("/home/tshalif/src/ws/talos-rowing/swing/src/test/java/org/nargila/robostroke/ui/swing/1288448996868-dataInput.txt"));

        Thread.sleep(20000);
    }


}
