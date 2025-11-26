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

public class Identity {

  public static final int KEYSIZE = 256 * 2;
  public static final int RATCHETSIZE = 256;
  public static final int NAME_HASH_LENGTH = 80;
  public static final int RANDOM_HASH_LENGTH = 80;
  public static final int SIGLENGTH = KEYSIZE;

  public static byte[] concatenate(byte[]... byteArrays) {
    int i = 0;
    for (byte[] byteArray : byteArrays) i += byteArray.length;
    byte[] bytes = new byte[i];
    i = 0;
    for (byte[] byteArray : byteArrays) {
      System.arraycopy(byteArray, 0, bytes, i, byteArray.length);
      i += byteArray.length;
    }
    return bytes;
  }

  public static void validateAnnounce(Packet packet) {
    if (packet.packetType != Packet.ANNOUNCE) throw new IllegalStateException();
    int keysize = KEYSIZE / 8;
    int ratchetsize = RATCHETSIZE / 8;
    int nameHashLen = NAME_HASH_LENGTH / 8;
    int randomHashLen = RANDOM_HASH_LENGTH / 8;
    int sigLen = SIGLENGTH / 8;
    byte[] destinationHash = packet.destinationHash;
    int i = 0;
    byte[] publicKey = Arrays.copyOfRange(packet.data, i, i + keysize);
    i += keysize;
    byte[] nameHash = Arrays.copyOfRange(packet.data, i, i + nameHashLen);
    i += nameHashLen;
    byte[] randomHash = Arrays.copyOfRange(packet.data, i, i + randomHashLen);
    i += randomHashLen;
    byte[] ratchet = new byte[0];
    if (packet.contextFlag == Packet.FLAG_SET) {
      ratchet = Arrays.copyOfRange(packet.data, i, i + ratchetsize);
      i += ratchetsize;
    }
    byte[] signature = Arrays.copyOfRange(packet.data, i, i + sigLen);
    i += sigLen;
    byte[] appData = Arrays.copyOfRange(packet.data, i, packet.data.length);

    byte[] signedData = concatenate(destinationHash, publicKey, nameHash, randomHash, ratchet, appData);
    if (!SignatureUtils.verify25519(Arrays.copyOfRange(publicKey, keysize / 2, keysize), signature, signedData))
      System.out.print("ERROR ");
    System.out.println(new String(appData));

  }

}
