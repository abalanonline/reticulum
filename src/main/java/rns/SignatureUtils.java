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

import org.bouncycastle.jcajce.spec.RawEncodedKeySpec;

import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;

public class SignatureUtils {

  static {
    Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
  }

  public static boolean verify25519(byte[] key, byte[] signature, byte[] data) {
    // FIXME: 2025-11-26 make the code Java 17 compatible
    try {
      PublicKey k = KeyFactory.getInstance("Ed25519").generatePublic(new RawEncodedKeySpec(key));
      Signature s = Signature.getInstance("Ed25519");
      s.initVerify(k);
      s.update(data);
      return s.verify(signature);
    } catch (GeneralSecurityException e) {
      throw new IllegalStateException(e);
    }
  }

}
