package org.nargila.robostroke.oggz;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

public class Talos2Kate {

	private final BufferedReader reader; 
	private final BufferedWriter writer; 
	private final long startOffset;
	private int markId;
	private long start;
	
	public Talos2Kate(File input, File output, int markId, long startOffset) throws IOException {
		
		
		InputStream is = new FileInputStream(input);
		
		if (input.getName().endsWith(".trsd")) {
			is = new GZIPInputStream(is);
		}
		
		reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
		
		
		writer = new BufferedWriter(new FileWriter(output));
		
		this.startOffset = startOffset;
		
		this.markId = markId;
	}
	
	private String genTime(long timestamp) {
		
	
	    long sec = timestamp / 1000;
	    long millis = timestamp - (sec * 1000);
	    
	    long hours = sec / 3600;
	    
	    sec -= hours * 3600;
	    
	    
	    long mins = sec / 60;
	    
	    sec -= mins * 60;
	    

	    return String.format("%02d:%02d:%02d.%03d", hours, mins, sec, millis);
	    
	}
	
	private void genEvent(long timestamp, String data) throws IOException {

	    String start_time = genTime(timestamp);
	    
	    writer.write(String.format("\tevent {\n\t\t%s --> %s\n\t\t\"%s\"\n\t}\n\n",start_time, start_time, data));
	}
	    	
	private void getStart() throws IOException {
				
		for (String l = reader.readLine(); l != null; l = reader.readLine()) {
			String s = l.substring(0, l.length() - 2);
			
			String[] parts = s.split(" ", 2);
			
			long timestamp = Long.parseLong(parts[0]);
			
			String dataLine = parts[1];
			
			parts = dataLine.split(" ");
			
			String eventName = parts[0];
			String data = parts[2];
			
			if ("LOGFILE_VERSION".equals(eventName) || "SESSION_PARAMETER".equals(eventName)) {
				genEvent(0, dataLine);
			} else if ("RECORDING_COUNTDOWN".equals(eventName)) {
				
				String[] d = data.split(",");
				if (markId == Integer.parseInt(d[1])) {
					
					for (long ts = 0; ts < startOffset; ts += 50) {
						genEvent(ts, "ACCEL 0 0,0,0"); // generate dummy events
					}
					
					start = timestamp;
					
					genEvent(startOffset, dataLine);
					
					break;
				}
			}
		}
		
		if (start == 0) {
			throw new IllegalStateException("could not find start mark RECORDING_COUNTDOWN " + markId);
		}		
	}
	
	public void process() throws IOException {
		
		writer.write("kate {\n");
		
		getStart();
		
		for (String l = reader.readLine(); l != null; l = reader.readLine()) {
			l = l.substring(0, l.length() - 2);
			String[] parts = l.split(" ", 2);
			
			long timestamp = Long.parseLong(parts[0]);
			
			genEvent(timestamp - start + startOffset, parts[1]);
		}
		
		writer.write("}\n");
		
		writer.close();
	}
	
	private static void usage() {
		System.err.println("usage: Talos2Kate <input> <output> <mark_id> <start_offset_millis>");
		System.exit(1);
	}
	
	public static void main(String[] args) throws IOException {
		
		if (args.length != 4) {
			usage();
		}
		
		Talos2Kate t2k = new Talos2Kate(new File(args[0]), new File(args[1]), Integer.parseInt(args[2]), Long.parseLong(args[3]));
		
		t2k.process();
	}
}
