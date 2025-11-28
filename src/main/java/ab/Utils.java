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

package ab;

public class Utils {

  public static String toHex(byte[] bytes) {
    StringBuilder s = new StringBuilder();
    for (byte b : bytes) s.append(String.format("%02X", b));
    return s.toString();
  }

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

  public static int indexOf(byte[] bytes, byte b, int fromIndex) {
    for (int i = fromIndex; i < bytes.length; i++) if (bytes[i] == b) return i;
    return -1;
  }

}
