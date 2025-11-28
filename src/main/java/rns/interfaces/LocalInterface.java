/*
 * Copyright (C) 2025 Aleksei Balan
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package rns.interfaces;

import ab.Utils;
import rns.Reticulum;
import rns.Rns;
import rns.Transport;

import java.util.Arrays;

public class LocalInterface {
  public static class LocalClientInterface extends Interface {
    Interface parent_interface;
    byte[] frame_buffer = new byte[0];
    Transport owner = new Transport();

    public void process_incoming(byte[] data) {
      rxb += data.length;
      Interface parent_interface = this.parent_interface;
      if (parent_interface != null) parent_interface.rxb += data.length;
      try {
        owner.inbound(data, this);
      } catch (Exception e) {
        Rns.logError("An error in the processing of an incoming frame for {" + this + "}:");
        Rns.trace_exception(e);
      }

    }

    public void handle_hdlc(byte[] data_in) {
      frame_buffer = Utils.concatenate(frame_buffer, data_in);
      while (true) {
        int frame_start = Utils.indexOf(frame_buffer, Hdlc.FLAG, 0);
        if (frame_start < 0) return;
        int frame_end = Utils.indexOf(frame_buffer, Hdlc.FLAG, frame_start + 1);
        if (frame_end < 0) return;
        byte[] frame = Hdlc.unescape(Arrays.copyOfRange(frame_buffer, frame_start + 1, frame_end));
        if (frame.length > Reticulum.HEADER_MINSIZE) process_incoming(frame);
        frame_buffer = Arrays.copyOfRange(frame_buffer, frame_end, frame_buffer.length);
      }
    }

    public synchronized void receive(byte[] data_in) {
      if (data_in.length > 0) {
        handle_hdlc(data_in);
      } else {
        throw new IllegalStateException();
      }
    }

  }
}
