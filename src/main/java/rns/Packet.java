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

import ab.Utils;

import java.util.Arrays;

/**
 * The Packet class is used to create packet instances that can be sent
 * over a Reticulum network. Packets will automatically be encrypted if
 * they are addressed to a ``RNS.Destination.SINGLE`` destination,
 * ``RNS.Destination.GROUP`` destination or a :ref:`RNS.Link<api-link>`.
 *
 * For ``RNS.Destination.GROUP`` destinations, Reticulum will use the
 * pre-shared key configured for the destination. All packets to group
 * destinations are encrypted with the same AES-256 key.
 *
 * For ``RNS.Destination.SINGLE`` destinations, Reticulum will use a newly
 * derived ephemeral AES-256 key for every packet.
 *
 * For :ref:`RNS.Link<api-link>` destinations, Reticulum will use per-link
 * ephemeral keys, and offers **Forward Secrecy**.
 *
 * :param destination: A :ref:`RNS.Destination<api-destination>` instance to which the packet will be sent.
 * :param data: The data payload to be included in the packet as *bytes*.
 * :param create_receipt: Specifies whether a :ref:`RNS.PacketReceipt<api-packetreceipt>` should be created when instantiating the packet.
 */
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
  // Packet context types
  public static final int PATH_RESPONSE = 0x0B;

  int flags;
  int hops;
  int header_type;
  int context_flag;
  int transport_type;
  int destination_type;
  public int packet_type;
  byte[] transport_id;
  byte[] destination_hash;
  int context;
  byte[] data;

  byte[] raw;
  boolean packed;
  boolean fromPacked;
  boolean create_receipt;
  byte[] packet_hash;

  public Packet(Object destination, byte[] data) {
    if (destination != null) {
      throw new IllegalStateException();
    } else {
      raw = data;
      packed = true;
      fromPacked = true;
      create_receipt = false;
    }
  }

  public boolean unpack() {
    flags = raw[0];
    hops = raw[0];
    header_type = flags >> 6 & 1;
    context_flag = flags >> 5 & 1;
    transport_type = flags >> 4 & 1;
    destination_type = flags >> 2 & 3;
    packet_type = flags & 3;
    int i = 2;
    transport_id = header_type == HEADER_2 ? Arrays.copyOfRange(raw, i, i + DST_LEN) : null;
    if (header_type == HEADER_2) i += DST_LEN;
    destination_hash = Arrays.copyOfRange(raw, i, i + DST_LEN);
    i += DST_LEN;
    context = raw[i++];
    data = Arrays.copyOfRange(raw, i, raw.length);
    packed = false;
    update_hash();
    return true;
  }

  void update_hash() { // 347
    packet_hash = get_hash();
  }

  byte[] get_hash() { // 350
    return rns.Identity.full_hash(get_hashable_part());
  }

  byte[] get_hashable_part() { // 356
    return Utils.concatenate(new byte[]{(byte) (raw[0] & 0x0F)},
        Arrays.copyOfRange(raw, 2 + header_type == HEADER_2 ? Reticulum.TRUNCATED_HASHLENGTH / 8 : 0, raw.length));
  }

}
