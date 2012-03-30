package org.nargila.robostroke.common;

import java.io.InputStream;
import java.io.OutputStream;

public abstract class DataConverter implements Runnable {
	
	private final InputStream in;
	private final OutputStream out;
	
	private final long inputLength;
	
	private boolean cancelled;
	
	private boolean good;
	private Exception error;
	
	public DataConverter(InputStream in, OutputStream out, long inputLength) {
		this.in = in;
		this.out = out;
		this.inputLength = inputLength;
	}

	public void cancel() {
		cancelled = true;
	}
	
	public void run() {
		
		onStart();
		
		byte[] buff = new byte[4096];
		
		try {

			long accum = 0;

			for (int i =  in.read(buff); !cancelled && i != -1; i =  in.read(buff)) {

				out.write(buff, 0, i);

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
	
	protected abstract void onStart();
	protected abstract boolean onProgress(double d);
	protected abstract void onFinish();
	protected abstract void onError(Exception e);
}
