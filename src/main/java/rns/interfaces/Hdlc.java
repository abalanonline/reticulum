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

import java.util.Arrays;

// https://en.wikipedia.org/wiki/High-Level_Data_Link_Control
public class Hdlc {

  public static final byte FLAG = 0x7E;
  public static final byte ESCAPE = 0x7D;
  public static final byte MASK = 0x20;

  public static byte[] escape() {
    return null;
  }

  public static byte[] decode(byte[] data) {
    if (data.length < 1 || data[0] != FLAG) throw new IllegalStateException();
    byte[] bytes = new byte[data.length];
    int length = 0;
    for (int i = 1; i < data.length; i++) {
      byte b = data[i];
      if (b == FLAG) break; // end of frame
      if (b == ESCAPE) {
        b = (byte) (data[++i] ^ MASK);
        if (b != FLAG && b != ESCAPE) throw new IllegalStateException();
      }
      bytes[length++] = b;
    }
    return Arrays.copyOf(bytes, length);
  }

}
