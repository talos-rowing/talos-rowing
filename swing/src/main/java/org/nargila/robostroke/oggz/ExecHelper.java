package org.nargila.robostroke.oggz;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

class ExecHelper {

    private static final Logger logger = LoggerFactory.getLogger(ExecHelper.class);

    static String fixPath(File f) {
        return f.getAbsolutePath().replace('\\', '/');
    }

    static void exec(String... args) throws Exception {
        exec(true, args);
    }

    static void exec(boolean waitFor, String... args) throws Exception {

        logger.info("executing {}", (Object) args);

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
            String msg = "command " + args[0] + " ended with exit code " + status;
            logger.error("error executing {}: {}", (Object) args, msg);
            throw new IllegalStateException(msg);
        }

    }

    static File getExecutable(String name) throws Exception {

        InputStream ins = ExecHelper.class.getResourceAsStream(name + ".exe");

        File exe;

        if (CheckOS.isWindows()) {
            exe = File.createTempFile(name, ".exe");

            exe.deleteOnExit();

            FileOutputStream fout = new FileOutputStream(exe);


            byte[] buff = new byte[4096];

            for (int len = ins.read(buff); len != -1; len = ins.read(buff)) {
                fout.write(buff, 0, len);
            }

            fout.close();
            ins.close();

            exe.setExecutable(true);
        } else {
            exe = new File("/usr/bin/" + name);
        }

        return exe;
    }
}
