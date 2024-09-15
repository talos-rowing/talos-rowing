/*
 * Copyright (c) 2012 Tal Shalif
 *
 * This file is part of Talos-Rowing.
 *
 * Talos-Rowing is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Talos-Rowing is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Talos-Rowing.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.nargila.robostroke.data.remote;

import java.io.IOException;

public class DatagramDataReceiver extends DatagramData implements DataReceiver {

  private Listener dataListener;

  public DatagramDataReceiver(String address, int port, Listener dataListener) throws DataRemoteError {
    super(DatagramSocketType.RECEIVER, address, port);

    this.dataListener = dataListener;
  }


  @Override
  public void setListener(Listener dataListener) {
    this.dataListener = dataListener;
  }


  @Override
  protected void processNextItem(DatagramSocketHelper dsh) throws IOException {

    String received = dsh.receiveData();

    if (dataListener != null && received != null && !received.equals("")) {
      dataListener.onDataReceived(received);
    }
  }
}
