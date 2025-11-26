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

package rns;

import java.util.Arrays;

public class Packet {

  public static final int DST_LEN = Reticulum.TRUNCATED_HASHLENGTH / 8;
  public static final int HEADER_1 = 0;
  public static final int HEADER_2 = 1;
  // Context flag values
  public static final int FLAG_UNSET = 0;
  public static final int FLAG_SET = 1;
  // Packet types
  public static final int DATA = 0;
  public static final int ANNOUNCE = 1;
  public static final int LINKREQUEST = 2;
  public static final int PROOF = 3;

  int flags;
  int hops;
  int headerType;
  int contextFlag;
  int transportType;
  int destinationType;
  public int packetType;
  byte[] transportId;
  byte[] destinationHash;
  int context;
  byte[] data;

  public Packet(byte[] raw) {
    unpack(this, raw);
  }

  public void unpack(Packet p, byte[] raw) {
    p.flags = raw[0];
    p.hops = raw[0];
    p.headerType = flags >> 6 & 1;
    p.contextFlag = flags >> 5 & 1;
    p.transportType = flags >> 4 & 1;
    p.destinationType = flags >> 2 & 3;
    p.packetType = flags & 3;
    int i = 2;
    p.transportId = p.headerType == HEADER_2 ? Arrays.copyOfRange(raw, i, i + DST_LEN) : null;
    if (p.headerType == HEADER_2) i += DST_LEN;
    p.destinationHash = Arrays.copyOfRange(raw, i, i + DST_LEN);
    i += DST_LEN;
    p.context = raw[i++];
    p.data = Arrays.copyOfRange(raw, i, raw.length);
  }

}
