package org.nargila.robostroke.data.remote;

import java.io.IOException;


public interface DataRemote {

  public class DataRemoteError extends IOException {
    private static final long serialVersionUID = 1L;

    public DataRemoteError() {
      super();
    }

    public DataRemoteError(String message, Throwable cause) {
      super(message, cause);
    }

    public DataRemoteError(String message) {
      super(message);
    }

    public DataRemoteError(Throwable cause) {
      super(cause);
    }
  }
  void start() throws DataRemoteError;
  void stop();
  void setPort(int port);
  void setAddress(String address);
}
