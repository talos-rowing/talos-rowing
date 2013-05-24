package org.nargila.robostroke.oggz;

import java.io.File;

import org.nargila.robostroke.common.Pair;

import com.fluendo.jst.Caps;
import com.fluendo.jst.Element;
import com.fluendo.jst.ElementFactory;
import com.fluendo.jst.Message;
import com.fluendo.jst.Pad;
import com.fluendo.jst.PadListener;
import com.fluendo.jst.Pipeline;
import com.fluendo.utils.Debug;

public class FindQrMarkPipeline extends Pipeline implements PadListener {

	
	private Element httpsrc;
	private FindQrMarkSink qrfindsink;
	private Pad ovsinkpad;
	private Element demux;
	private Element buffer;
	private Element videodec;

	public FindQrMarkPipeline(File ogg) {
		build(ogg.toURI().toString());
		

	}
	
	private void start() {
		setState(Pipeline.PLAY);
	}
	
	void stop() {
		setState(Pipeline.STOP);
	}
	
	private void noSuchElement(String elemName) {
		postMessage(Message.newError(this, "no such element: " + elemName
				+ " (check plugins.ini)"));
	}


	private boolean build(String url) {

		httpsrc = ElementFactory.makeByName("httpsrc", "httpsrc");
		
		if (httpsrc == null) {
			noSuchElement("httpsrc");
			return false;
		}

		httpsrc.setProperty("url", url);
		httpsrc.setProperty("userId", null);
		httpsrc.setProperty("password", null);

		httpsrc.setProperty("documentBase", null);

		add(httpsrc);

		buffer = ElementFactory.makeByName("queue", "buffer");
		if (buffer == null) {
			demux = null;
			noSuchElement("queue");
			return false;
		}
		
		buffer.setProperty("isBuffer", Boolean.TRUE);

		add(buffer);
		
		
		httpsrc.getPad("src").link(buffer.getPad("sink"));
		
		demux = ElementFactory.makeByName("oggdemux", "demux");
		if (demux == null) {
			noSuchElement("oggdemux");
			return false;
		}
	

		add(demux);
		

		buffer.getPad("src").link(demux.getPad("sink"));
			

		qrfindsink = new FindQrMarkSink();
		
		if (qrfindsink == null) {
			noSuchElement("videosink");
			return false;
		}


		add(qrfindsink);

		ovsinkpad = qrfindsink.getPad("sink");

		demux.addPadListener(this);

		buffer.setState(PAUSE);
		demux.setState(PAUSE);
	
		return true;
	}
	
	@Override
	public void padAdded(Pad pad) {
		Caps caps = pad.getCaps();

		if (caps == null) {
			Debug.log(Debug.INFO, "pad added without caps: " + pad);
			return;
		}
		
		Debug.log(Debug.INFO, "pad added " + pad);

		String mime = caps.getMime();

		if (mime.equals("video/x-theora")) {
			
			videodec = ElementFactory.makeByName("theoradec", "videodec");
			if (videodec == null) {
				noSuchElement(name);
				return;
			}
			
			add(videodec);
			
			
			// Constructs a chain of the form
			// oggdemux -> v_queue -> theoradec -> v_queue2 -> videosink
			Element v_queue = ElementFactory.makeByName("queue", "v_queue");
			Element v_queue2 = ElementFactory.makeByName("queue", "v_queue2");
			if (v_queue == null) {
				noSuchElement("queue");
				return;
			}


			v_queue.setProperty("maxBuffers", "175");
			v_queue2.setProperty("maxBuffers", "1");

			add(v_queue);
			add(v_queue2);

			pad.link(v_queue.getPad("sink"));
			
			v_queue.getPad("src").link(videodec.getPad("sink"));
			
			videodec.getPad("src").link(v_queue2.getPad("sink"));
			
			if (!v_queue2.getPad("src").link(ovsinkpad)) {
				postMessage(Message.newError(this, "videosink already linked"));
				return;
			}

			videodec.setState(PAUSE);
			v_queue.setState(PAUSE);
			v_queue2.setState(PAUSE);

		}
	}

	public void padRemoved(Pad pad) {

	}
	
	public FindQrMarkSink getVideosink() {
		return qrfindsink;
	}
	
	public Pair<Integer,Long> findMark(int timeoutSeconds) throws Exception {
		
		
		final Object finishSync = getVideosink().finishSync;
		
		synchronized (finishSync) {
			start();
			finishSync.wait(timeoutSeconds * 1000);
		}
		
		stop();
		shutDown();
		
		if (qrfindsink.mark == null) {
			throw new IllegalStateException("could not find QR sync mark in video within " + timeoutSeconds + " seconds");
		}
		
		return Pair.create(new Integer(qrfindsink.mark.split(":")[1]),qrfindsink.timestamp / 1000);
	}

	public static void main(String[] args) throws Exception {
		
		FindQrMarkPipeline qrFind = new FindQrMarkPipeline(new File(args[0]));
		
		qrFind.findMark(30);
	}
}
