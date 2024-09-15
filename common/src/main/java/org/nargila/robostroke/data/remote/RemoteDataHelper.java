package org.nargila.robostroke.data.remote;

import org.nargila.robostroke.ParamKeys;
import org.nargila.robostroke.RoboStroke;
import org.nargila.robostroke.data.SessionRecorderConstants;

public class RemoteDataHelper {

  public static String getAddr(RoboStroke rs) {

    String res = rs.getParameters().getValue(ParamKeys.PARAM_SESSION_BROADCAST_HOST.getId());

    return res == null ? SessionRecorderConstants.BROADCAST_HOST : res;
  }

  public static int getPort(RoboStroke rs) {
    Integer res = rs.getParameters().getValue(ParamKeys.PARAM_SESSION_BROADCAST_PORT.getId());
    return res == null ? SessionRecorderConstants.BROADCAST_PORT : res;
  }



}
