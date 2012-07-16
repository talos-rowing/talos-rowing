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

package org.nargila.robostroke.ui.meters.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import org.nargila.robostroke.ui.LayoutMode;
import org.nargila.robostroke.ui.RSTextView;
import org.nargila.robostroke.ui.RSView;
import org.nargila.robostroke.ui.meters.MeterView;
import org.nargila.robostroke.ui.swing.SwingTextView;
import org.nargila.robostroke.ui.swing.SwingView;

public class SwingMeterView extends JPanel implements MeterView {
	
	private static final long serialVersionUID = 1L;

	private JLabel avgSpmTxt;
	private JLabel splitStrokeCountTxt;
	private JLabel totalTimeTxt;
	private JLabel avgSpeedTxt;
	private JLabel totalDistanceTxt;
	private JLabel spmTxt;
	private JLabel splitTimeTxt;
	private JLabel speedTxt;
	private JLabel splitDistanceTxt;
	private JPanel speedAcuracyHighglight;
	private JPanel strokeModeHighlighter;

	private final RSView speedAcuracyHighlightWrap;

	private final SwingView strokeModeHighlighterWrap;

	private final SwingTextView avgSpmTxtWrap;

	private final SwingTextView splitStrokesTxtWrap;

	private final SwingTextView totalTimeTxtWrap;

	private final SwingTextView avgSpeedTxtWrap;

	private final SwingTextView totalDistanceTxtWrap;

	private final SwingTextView spmTxtWrap;

	private final SwingTextView splitTimeTxtWrap;

	private final SwingTextView speedTxtWrap;

	private final SwingTextView splitDistanceTxtWrap;

	private final SwingTextView splitStrokeCountTxtWrap;
	
	/**
	 * Create the panel.
	 */
	public SwingMeterView() {
		
		JPanel panel = new JPanel();
		panel.setBackground(Color.BLACK);
		
		JPanel panel_2 = new JPanel();
		panel_2.setBackground(Color.BLACK);
		panel_2.setLayout(new BorderLayout(0, 0));
		
		speedAcuracyHighglight = new JPanel();
		speedAcuracyHighglight.setBorder(new EmptyBorder(0, 5, 0, 5));
		speedAcuracyHighglight.setBackground(Color.BLACK);
		panel_2.add(speedAcuracyHighglight, BorderLayout.SOUTH);
		speedAcuracyHighglight.setLayout(new BorderLayout(0, 0));
		
		JLabel lblNewLabel = new JLabel("Avg");
		lblNewLabel.setForeground(Color.WHITE);
		speedAcuracyHighglight.add(lblNewLabel, BorderLayout.WEST);
		
		JLabel lblNewLabel_1 = new JLabel("/500m");
		lblNewLabel_1.setForeground(Color.WHITE);
		speedAcuracyHighglight.add(lblNewLabel_1, BorderLayout.EAST);
		
		speedTxt = new JLabel("0:00");
		speedTxt.setForeground(Color.WHITE);
		speedTxt.setHorizontalAlignment(SwingConstants.CENTER);
		speedTxt.setFont(new Font("Dialog", Font.BOLD, 60));
		panel_2.add(speedTxt, BorderLayout.CENTER);
		
		JPanel panel_5 = new JPanel();
		panel_5.setBackground(Color.BLACK);
		panel_5.setBorder(new EmptyBorder(10, 5, 0, 0));
		panel_2.add(panel_5, BorderLayout.WEST);
		panel_5.setLayout(new BorderLayout(0, 0));
		
		avgSpeedTxt = new JLabel("0:00");
		avgSpeedTxt.setFont(new Font("Dialog", Font.PLAIN, 40));
		avgSpeedTxt.setForeground(Color.WHITE);
		avgSpeedTxt.setVerticalAlignment(SwingConstants.TOP);
		panel_5.add(avgSpeedTxt, BorderLayout.CENTER);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		panel.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new EmptyBorder(0, 5, 0, 5));
		panel_1.setBackground(Color.BLACK);
		panel.add(panel_1, BorderLayout.SOUTH);
		panel_1.setLayout(new BorderLayout(0, 0));
		
		JLabel lblAvg = new JLabel("Avg");
		lblAvg.setForeground(Color.WHITE);
		panel_1.add(lblAvg, BorderLayout.WEST);
		
		JLabel lblSpm = new JLabel("SPM");
		lblSpm.setForeground(Color.WHITE);
		panel_1.add(lblSpm, BorderLayout.EAST);
		
		JPanel panel_4 = new JPanel();
		panel_4.setBackground(Color.BLACK);
		panel_4.setBorder(new EmptyBorder(10, 5, 0, 0));
		panel.add(panel_4, BorderLayout.WEST);
		panel_4.setLayout(new BorderLayout(0, 0));
		
		avgSpmTxt = new JLabel("00");
		avgSpmTxt.setFont(new Font("Dialog", Font.PLAIN, 40));
		avgSpmTxt.setForeground(Color.WHITE);
		panel_4.add(avgSpmTxt, BorderLayout.CENTER);
		avgSpmTxt.setVerticalAlignment(SwingConstants.TOP);
		
		spmTxt = new JLabel("0");
		spmTxt.setForeground(Color.WHITE);
		spmTxt.setFont(new Font("Dialog", Font.BOLD, 70));
		spmTxt.setHorizontalAlignment(SwingConstants.CENTER);
		panel.add(spmTxt, BorderLayout.CENTER);
		add(panel);
		
		JSeparator separator = new JSeparator();
		add(separator);
		
		JPanel panel_6 = new JPanel();
		panel_6.setBackground(Color.BLACK);
		add(panel_6);
		panel_6.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_7 = new JPanel();
		panel_7.setBackground(Color.BLACK);
		panel_7.setBorder(new EmptyBorder(10, 5, 0, 0));
		panel_6.add(panel_7, BorderLayout.WEST);
		panel_7.setLayout(new BorderLayout(0, 0));
		
		splitStrokeCountTxt = new JLabel("0");
		splitStrokeCountTxt.setFont(new Font("Dialog", Font.PLAIN, 60));
		splitStrokeCountTxt.setForeground(Color.WHITE);
		panel_7.add(splitStrokeCountTxt, BorderLayout.CENTER);
		splitStrokeCountTxt.setVerticalAlignment(SwingConstants.TOP);
		
		splitTimeTxt = new JLabel("0:00");
		splitTimeTxt.setForeground(Color.WHITE);
		splitTimeTxt.setHorizontalAlignment(SwingConstants.CENTER);
		splitTimeTxt.setFont(new Font("Dialog", Font.BOLD, 80));
		panel_6.add(splitTimeTxt, BorderLayout.CENTER);
		
		strokeModeHighlighter = new JPanel();
		strokeModeHighlighter.setBorder(new EmptyBorder(0, 5, 0, 5));
		strokeModeHighlighter.setBackground(Color.BLACK);
		panel_6.add(strokeModeHighlighter, BorderLayout.SOUTH);
		strokeModeHighlighter.setLayout(new BorderLayout(0, 0));
		
		JLabel lblNewLabel_2 = new JLabel("Strokes");
		lblNewLabel_2.setForeground(Color.WHITE);
		strokeModeHighlighter.add(lblNewLabel_2, BorderLayout.WEST);
		
		JLabel lblTime = new JLabel("Time");
		lblTime.setForeground(Color.WHITE);
		strokeModeHighlighter.add(lblTime, BorderLayout.EAST);
		
		totalTimeTxt = new JLabel("0:00:00");
		totalTimeTxt.setForeground(Color.WHITE);
		totalTimeTxt.setFont(new Font("Dialog", Font.BOLD, 14));
		totalTimeTxt.setHorizontalAlignment(SwingConstants.CENTER);
		strokeModeHighlighter.add(totalTimeTxt, BorderLayout.CENTER);
		
		JSeparator separator_1 = new JSeparator();
		add(separator_1);
		add(panel_2);
		
		JSeparator separator_2 = new JSeparator();
		add(separator_2);
		
		JPanel panel_9 = new JPanel();
		panel_9.setBackground(Color.BLACK);
		add(panel_9);
		panel_9.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_10 = new JPanel();
		panel_10.setBackground(Color.BLACK);
		panel_10.setBorder(new EmptyBorder(10, 5, 0, 0));
		panel_9.add(panel_10, BorderLayout.WEST);
		panel_10.setLayout(new BorderLayout(0, 0));
		
		totalDistanceTxt = new JLabel("0");
		totalDistanceTxt.setFont(new Font("Dialog", Font.PLAIN, 40));
		totalDistanceTxt.setForeground(Color.WHITE);
		totalDistanceTxt.setVerticalAlignment(SwingConstants.TOP);
		panel_10.add(totalDistanceTxt, BorderLayout.CENTER);
		
		splitDistanceTxt = new JLabel("0");
		splitDistanceTxt.setForeground(Color.WHITE);
		splitDistanceTxt.setHorizontalAlignment(SwingConstants.CENTER);
		splitDistanceTxt.setFont(new Font("Dialog", Font.BOLD, 60));
		panel_9.add(splitDistanceTxt, BorderLayout.CENTER);
		
		JPanel panel_11 = new JPanel();
		panel_11.setBorder(new EmptyBorder(0, 5, 0, 5));
		panel_11.setBackground(Color.BLACK);
		panel_9.add(panel_11, BorderLayout.SOUTH);
		panel_11.setLayout(new BorderLayout(0, 0));
		
		JLabel lblTotal = new JLabel("Total");
		lblTotal.setForeground(Color.WHITE);
		panel_11.add(lblTotal, BorderLayout.WEST);
		
		JLabel lblM = new JLabel("m");
		lblM.setForeground(Color.WHITE);
		panel_11.add(lblM, BorderLayout.EAST);

		speedAcuracyHighlightWrap = new SwingView(speedAcuracyHighglight);
		strokeModeHighlighterWrap = new SwingView(strokeModeHighlighter);
		avgSpmTxtWrap = new SwingTextView(avgSpmTxt);
		splitStrokesTxtWrap = new SwingTextView(splitStrokeCountTxt);
		totalTimeTxtWrap = new SwingTextView(totalTimeTxt);
		avgSpeedTxtWrap = new SwingTextView(avgSpeedTxt);
		totalDistanceTxtWrap = new SwingTextView(totalDistanceTxt);
		spmTxtWrap = new SwingTextView(spmTxt);
		splitTimeTxtWrap = new SwingTextView(splitTimeTxt);
		speedTxtWrap = new SwingTextView(speedTxt);
		splitDistanceTxtWrap = new SwingTextView(splitDistanceTxt);
		splitStrokeCountTxtWrap = new SwingTextView(splitStrokeCountTxt);		
	}

	@Override
	public void updateLayout(LayoutMode meterLayout) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public RSView getAccuracyHighlighter() {
		return speedAcuracyHighlightWrap;
	}

	@Override
	public RSTextView getAvgSpmTxt() {		
		return avgSpmTxtWrap;
	}
	
	@Override
	public RSTextView getSplitStrokesTxt() {		
		return splitStrokesTxtWrap;
	}
	
	@Override
	public RSTextView getTotalTimeTxt() {		
		return totalTimeTxtWrap;
	}
	
	@Override
	public RSTextView getAvgSpeedTxt() {		
		return avgSpeedTxtWrap;
	}
	@Override
	public RSTextView getTotalDistanceTxt() {		
		return totalDistanceTxtWrap;
	}
	
	@Override
	public RSTextView getSpmTxt() {		
		return spmTxtWrap;
	}
	
	@Override
	public RSTextView getSplitTimeTxt() {		
		return splitTimeTxtWrap;
	}
	
	@Override
	public RSTextView getSpeedTxt() {		
		return speedTxtWrap;
	}
	
	@Override
	public RSTextView getSplitDistanceTxt() {		
		return splitDistanceTxtWrap;
	}
	
	@Override
	public RSTextView getStrokeCountTxt() {		
		return splitStrokeCountTxtWrap;
	}
	
	@Override
	public RSView getStrokeModeHighlighter() {		
		return strokeModeHighlighterWrap;
	}
}
