package org.nargila.robostroke.input.version;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.nargila.robostroke.SessionRecorderConstants;

public abstract class DataVersionConverter {
	
	public static class ConverterError extends Exception {

		public ConverterError() {
		}

		public ConverterError(String message, Throwable cause) {
			super(message, cause);
		}

		public ConverterError(String message) {
			super(message);
		}

		public ConverterError(Throwable cause) {
			super(cause);
		}		
	}
	
	
	public interface ProgressListener {
		boolean onProgress(double d);
	}
	
	protected ProgressListener progressListener;
	protected boolean cancelled;
	
	public abstract File convert(File input) throws Exception;
	
	private static int getFileVersion(File input) throws ConverterError {
		
		BufferedReader reader = null;
		
		try {
			reader = new BufferedReader(new FileReader(input));
			 
			String line = reader.readLine();
			
			int idx = line.lastIndexOf(" ");
			
			return new Integer(line.substring(idx + 1, line.length() - SessionRecorderConstants.END_OF_RECORD.length()));
		} catch (IOException e) {
			throw new ConverterError("error getting data file version", e);
		} finally {
			if (reader != null) try {reader.close();} catch (IOException e) {}
		}
	}
	
	public void setProgressListener(ProgressListener progressListener) {
		this.progressListener = progressListener;
	}
	
	public static DataVersionConverter getConvertersFor(File input) throws ConverterError {
		
		int ver = getFileVersion(input);
		
		if (ver == SessionRecorderConstants.LOGFILE_VERSION) {
			return null;
		}
		
		if (ver > SessionRecorderConstants.LOGFILE_VERSION) {
			throw new ConverterError(String.format("input file version %d is in the future - current version is %d", ver, SessionRecorderConstants.LOGFILE_VERSION));
		} else {
			
			DataVersionConverter[] converters = new DataVersionConverter[SessionRecorderConstants.LOGFILE_VERSION - ver];
			
			for (int i = ver; i < SessionRecorderConstants.LOGFILE_VERSION; ++i) {
				converters[i - ver] = createConverter(i);
			}
			
			return new DataVersionConverterChain(converters);
		}
	}

	private static DataVersionConverter createConverter(int ver) throws ConverterError {
		
		try {
			
			Class<DataVersionConverter> clazz = (Class<DataVersionConverter>) Class.forName(DataVersionConverter.class.getPackage().getName() + ".impl.DataVersionConverter_" + ver);
			
			return clazz.newInstance();
			
		} catch (ClassNotFoundException e) {
			throw new ConverterError("no converter found for version " + ver);
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void cancel() {
		cancelled = true;
	}
	
	private static class DataVersionConverterChain extends DataVersionConverter {

		private DataVersionConverter[] converters;

		private DataVersionConverterChain(DataVersionConverter[] converters) {
			this.converters = converters;
		}
		
		@Override
		public File convert(File input) throws Exception {
			
			int count = 0;
			for (DataVersionConverter converter: converters) {
				
				final int completed = count;
				
				if (cancelled) {
					break;
				}
				
				converter.setProgressListener(new ProgressListener() {
					
					@Override
					public boolean onProgress(double d) {
						if (progressListener != null) {
							return !cancelled && progressListener.onProgress(completed / (double)converters.length + d / converters.length);
						}
						
						return !cancelled;
					}
				});
				
				input = converter.convert(input);
				
				count++;
			}
			
			return cancelled ? null : input;
		}
		
	}
}
