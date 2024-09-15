package org.nargila.robostroke.data.remote;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import org.nargila.robostroke.common.ThreadedQueue;
import org.nargila.robostroke.data.DataRecord;
import org.nargila.robostroke.data.SessionRecorderConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SocketDataSender implements DataSender {

  private static final Logger logger = LoggerFactory.getLogger(SocketDataSender.class);

  private ServerSocket socket;

  private Socket s;

  private int port;

  private Writer recordOut;

  private final ThreadedQueue<String> recordQueue;

  private Thread listenThread;

  public SocketDataSender(String name, int port) {
    this.port = port;
    recordQueue  = new ThreadedQueue<String>("SocketDataTransport:" + name, 100) {

      @Override
      protected void handleItem(String o) {
        writeRecord(o);
      }

    };
  }

  public void setPort(int port) {
    this.port = port;
  }

  private void writeRecord(String o) {
    try {
      recordOut.write(o + SessionRecorderConstants.END_OF_RECORD + "\n");
      recordOut.flush();
    } catch (SocketException e) {
      logger.warn("socket error - forcing close", e);

      try {
        s.close();
      } catch (IOException e1) {
      }

    } catch (IOException e) {
      logger.warn("error writing out record", e);
    }
  }

  @Override
  public synchronized void start() throws DataRemoteError {
    try {
      socket = new ServerSocket(port);
    } catch (IOException e) {
      throw new DataRemoteError(e);
    }

    listenThread = new Thread("SocketDataTransport listen") {
      @Override
      public void run() {

        while (!socket.isClosed()) {


          try {

            Socket soc = socket.accept();

            synchronized (SocketDataSender.this) {
              s = soc;
            }

            if (s.isConnected()) {

              recordOut = new OutputStreamWriter(s.getOutputStream());

              writeRecord(new DataRecord(DataRecord.Type.LOGFILE_VERSION, -1,
                  SessionRecorderConstants.LOGFILE_VERSION).toString());

              recordQueue.setEnabled(true);

            }

            while (!s.isClosed()) {
              try {
                Thread.sleep(10);
              } catch (InterruptedException e) {
              }
            }

          } catch (IOException e) {
            logger.warn("error in accept loop", e);
          } finally {
            recordQueue.setEnabled(false);
          }
        }
      }
    };

    listenThread.start();

  }

  @Override
  public synchronized void stop() {

    if (socket != null) {
      try {
        socket.close();
      } catch (IOException e) {
      }

      if (s != null) {
        try {
          s.close();
        } catch (IOException e) {
        }
      }
    }

    try {
      listenThread.join();
    } catch (InterruptedException e) {
    }
  }

  public void write(String data) {
    recordQueue.put(data);
  }

  @Override
  public void setAddress(String address) {
    /* not implemented for TCP socket */
  }
}
