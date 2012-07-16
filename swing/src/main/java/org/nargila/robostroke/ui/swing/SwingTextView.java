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

import java.awt.Color;

import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import org.nargila.robostroke.ui.RSTextView;

public class SwingTextView extends SwingView implements RSTextView {
	
	public SwingTextView(JLabel impl) {
		super(impl);
		
		assert impl != null;
	}

	@Override
	public void setText(final String txt) {
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				((JLabel)impl).setText(txt == null ? "" : txt);
			}
		});

	}
	
	@Override
	public void setColor(int... argb) {
		impl.setForeground(new Color(argb[1], argb[2], argb[3], argb[0]));
	}
}
