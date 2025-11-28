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

import ab.SignatureUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SignatureUtilsTest {

  public static byte[] fromHex(String hex) {
    byte[] bytes = new byte[hex.length() / 2];
    for (int i = 0; i < bytes.length; i++) bytes[i] = (byte) Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
    return bytes;
  }

  @Test
  void validateSignature() {
    String key = "" +
        "8BFDE75083EDF2C1C2B004F6F7A60985E9AA858F62E688C91D80A7F6460FC26C";
    String signature = "" +
        "CE102E28266AF7072B13DEBCB15EE35DC492A00B6D9A68AE36BA3416121C49B2" +
        "0A740D420D6C139A0594B73B140105620EA01EC61CE777AAE180231A3A869203";
    String data = "" +
        "234EED3D3775EB1E29CF5A3842961C25B5D0A228394D29AACB127F5F9A791A68" +
        "42F6B95C40A3AF677ADFA8F2DFA1FD728BFDE75083EDF2C1C2B004F6F7A60985" +
        "E9AA858F62E688C91D80A7F6460FC26C213E6311BCEC54AB4FDEF00E34ED1100" +
        "692683F76730306E20436C6F7564202844616C6C617329";
    assertTrue(SignatureUtils.verify25519(fromHex(key), fromHex(signature), fromHex(data)));
    assertFalse(SignatureUtils.verify25519(fromHex(key), fromHex(signature), fromHex(data + key)));
  }
}
