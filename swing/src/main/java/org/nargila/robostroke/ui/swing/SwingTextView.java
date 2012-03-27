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
