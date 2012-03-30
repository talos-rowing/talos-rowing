package org.nargila.robostroke.common;

import java.io.Closeable;
import java.io.IOException;

public abstract class DataConverter<IN extends Closeable, OUT extends Closeable> implements Runnable {
	
	protected final IN in;
	protected final OUT out;
	
	private final long inputLength;
	
	private boolean cancelled;
	
	private boolean good;
	private Exception error;
	
	public DataConverter(IN in, OUT out, long inputLength) {
		this.in = in;
		this.out = out;
		this.inputLength = inputLength;
	}

	public void cancel() {
		cancelled = true;
	}
	
	protected abstract int processNext() throws IOException;
	
	public void run() {
		
		onStart();
				
		try {

			long accum = 0;

			for (int i =  processNext(); !cancelled && i != -1; i =  processNext()) {

				accum += i;

				cancelled = !onProgress(accum / (double) inputLength);
			}

			if (!cancelled) {
				good = true;
			}
		} catch (Exception e) {
			error = e;
			onError(e);
		} finally {
			
			if (in != null) try {in.close();} catch (Exception e) {}
			if (out != null) try {out.close();} catch (Exception e) {}
			
			onFinish();
		}
	}

	public boolean isGood() {
		return good;
	}
	
	public Exception getError() {
		return error;
	}
	
	public boolean isCancelled() {
		return cancelled;
	}
	
	protected void onStart() {}
	protected abstract boolean onProgress(double d);
	protected void onFinish() {}
	protected void onError(Exception e) {}
}
