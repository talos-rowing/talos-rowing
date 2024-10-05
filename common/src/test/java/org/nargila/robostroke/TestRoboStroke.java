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
package org.nargila.robostroke;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;

import org.nargila.robostroke.common.LocationUtil;
import org.nargila.robostroke.data.FileDataInput;
import org.nargila.robostroke.data.SensorDataInput;
import org.nargila.robostroke.way.DistanceResolver;

public class TestRoboStroke {

	
	
	private final File inputFile;
	private static File logFile;
	
	final RoboStroke roboStroke;
	final RoboStrokeEventBus bus;

	public TestRoboStroke(String dataInputName) throws Exception {		
		
		inputFile = File.createTempFile("RoboStrokeTest-inputData-", ".txt");
//		inputFile.deleteOnExit();

		logFile = File.createTempFile("RoboStrokeTest-outputData-", ".txt");
		
		InputStream in = TestRoboStroke.class.getResourceAsStream(dataInputName);		
		
		try {
		
			FileOutputStream fout = new FileOutputStream(inputFile);
			
			try {
		
				fout.getChannel().transferFrom(Channels.newChannel(in), 0, Long.MAX_VALUE);
			} finally {
				fout.close();
			}
			
		} finally {
			in.close();
		}


		roboStroke = new RoboStroke(new DistanceResolver() {
			
			@Override
			public float calcDistance(double[] loc1, double[] loc2) {
				return (float) LocationUtil.distVincenty(loc1[0], loc1[1], loc2[0], loc2[1]);
			}
		});
		
		bus = roboStroke.getBus();

		
		System.setProperty("org.nargila.robostroke.data.FileDataInput.batchMode", "true");

	}
	
	public void start(SensorDataInput dataInput, long startTime) throws IOException {
		
		if (dataInput == null) {
			dataInput = fileInput(startTime);
		}
		
		roboStroke.setInput(dataInput);
		
		roboStroke.setDataLogger(logFile);		
	}

	public FileDataInput fileInput(long startTime) throws IOException {
		FileDataInput input = new FileDataInput(roboStroke, inputFile);
		input.setTime(startTime);
		return input;		
	}
	
	public void tearDown() {
		roboStroke.destroy();
	}		
}
