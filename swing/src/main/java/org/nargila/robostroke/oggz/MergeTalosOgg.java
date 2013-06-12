package org.nargila.robostroke.oggz;

import java.io.File;

import org.nargila.robostroke.common.Pair;

public class MergeTalosOgg {
	
	public interface StatusListener {
		public void onStatus(String msg);
		public boolean isCanceled();
	}
	
	private File ogg;
	private File talos;
	private File output;

	private StatusListener statusListener;
	
	public MergeTalosOgg(File ogg, File talos, File output) {
		this.ogg = ogg;
		this.talos = talos;
		this.output = output;
	}
	
	public void process(StatusListener statusListener) throws Exception {
		
		this.statusListener = statusListener;
		
		if (!updateStatus("searching OGG for alignment QR mark...")) {
			return;
		}
		
		FindQrMarkPipeline findQr = new FindQrMarkPipeline(ogg);
		Pair<Integer, Long> res = findQr.findMark(30);
		
		if (!updateStatus("generating Talos Kate data aligned at " + res.second + "ms")) {
			return;
		}
		
		File kate = File.createTempFile("talos", ".kate");
		kate.deleteOnExit();
		
		Talos2Kate t2k = new Talos2Kate(talos, kate, res.first, res.second);
		
		t2k.process();
		
		if (!updateStatus("converting Talos Kate to OGG")) {
			return;
		}
		
		File kateOgg = kate2ogg(kate);
		
		if (!updateStatus("final merging OGG + Talos Kate")) {
			return;
		}
		
		merge(ExecHelper.fixPath(ogg), ExecHelper.fixPath(kateOgg), ExecHelper.fixPath(output));
		
	}


	
	private boolean updateStatus(String msg) {
		if (statusListener != null) {
			
			 if (statusListener.isCanceled()) {
				 return false;
			 }
			 
			statusListener.onStatus(msg);
		}
		
		return true;
	}

	private File kate2ogg(File kate) throws Exception {
		File kateOgg = File.createTempFile("talos-kate", ".ogg");
		kateOgg.deleteOnExit();
		
		File exe = ExecHelper.getExecutable("kateenc");
		
		ExecHelper.exec(exe.getAbsolutePath(), "-c", "robostroke", "-l", "ja", "-t", "kate", "-o", kateOgg.getAbsolutePath(), kate.getAbsolutePath());
		
		return kateOgg;
	}

	private void merge(String oggPath, String kateOggPath, String outputPath) throws Exception {
		
		File exe = ExecHelper.getExecutable("oggz-merge");

		ExecHelper.exec(exe.getAbsolutePath(), "-o", outputPath, oggPath, kateOggPath);
	}
	
	
	private static void usage() {
		System.err.println("usage: MergeTalosOgg <ogg> <talos> <outfile>");
		System.exit(1);
	}
	
	public static void main(String[] args) throws Exception {
		if (args.length != 3) {
			usage();
		}
		
		
		MergeTalosOgg prog = new MergeTalosOgg(new File(args[0]), new File(args[1]), new File(args[2]));
		
		prog.process(null);
	}
}
