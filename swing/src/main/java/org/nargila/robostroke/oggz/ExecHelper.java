package org.nargila.robostroke.oggz;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.TimeoutException;

class ExecHelper {
	
	
	static String fixPath(File f) {
		return f.getAbsolutePath().replace('\\', '/');
	}

	static void exec(String ... args) throws Exception {
		exec(true, args);
	}
		
	static void exec(boolean waitFor, String ... args) throws Exception {
		
		ProcessBuilder pb = new ProcessBuilder(args);
		pb.redirectErrorStream(true);
		
		Process proc = pb.start();
		
		
		if (!waitFor) {
			return;
		} 
		
		InputStream is = proc.getInputStream();
		
		BufferedReader r = new BufferedReader(new InputStreamReader(is, "UTF-8"));
		
		for (String l = r.readLine(); l != null; l = r.readLine()) {
			System.out.println(l);
		}
		
		int status = proc.waitFor();
		
		if (status != 0) {
			throw new IllegalStateException("command " + args[0] + " ended with exit code " + status);
		}
		
	}
	
	static File getExecutable(String name) throws Exception {
		
		InputStream ins = ExecHelper.class.getResourceAsStream(name + ".exe");
		
		File exe = File.createTempFile(name, ".exe");
		
		exe.deleteOnExit();
		
		FileOutputStream fout = new FileOutputStream(exe);
		
		
		byte[] buff = new byte[4096];
		
		for (int len = ins.read(buff); len != -1; len = ins.read(buff)) {
			fout.write(buff, 0, len);
		}
		
		fout.close();
		ins.close();
		
		exe.setExecutable(true);
		
		return exe;
	}
}
