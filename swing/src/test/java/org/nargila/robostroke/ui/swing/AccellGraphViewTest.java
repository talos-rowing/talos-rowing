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


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.nargila.robostroke.ui.graph.swing.AccellGraphView;

public class AccellGraphViewTest extends GraphTestBase<AccellGraphView> {


  @Test
  public void testEnableDisable() throws Exception {

    assertFalse(graph.isDisabled());

    graph.disableUpdate(true);

    assertTrue(graph.isDisabled());

  }

  @Test
  public void test() throws Exception {
    startRs();
  }

  @Override
  protected AccellGraphView createGraph() {
    return new AccellGraphView(rs);
  }


}
