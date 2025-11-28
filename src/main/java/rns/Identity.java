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
import ab.Utils;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Identity {

  public static final int KEYSIZE = 256 * 2;
  public static final int RATCHETSIZE = 256;
  public static final int NAME_HASH_LENGTH = 80;
  public static final int RANDOM_HASH_LENGTH = 80;
  public static final int SIGLENGTH = KEYSIZE;
  public static final int TRUNCATED_HASHLENGTH = rns.Reticulum.TRUNCATED_HASHLENGTH;

  // Storage
  static Map<String, byte[][]> known_destinations = new HashMap<>();
  static Map<String, byte[][]> known_ratchets = new HashMap<>();

  byte[] prv_bytes;
  byte[] sig_prv_bytes;

  byte[] pub_bytes;
  byte[] sig_pub_bytes;

  byte[] hash;
  String hexhash;
  byte[] app_data;

  public static void remember(byte[] packet_hash, byte[] destination_hash, byte[] public_key, byte[] app_data) { // 99
    if (public_key.length != Identity.KEYSIZE / 8) throw new IllegalStateException();
    known_destinations.put(Utils.toHex(destination_hash), new byte[][]{{}, packet_hash, public_key, app_data});
  }

  public static Identity recall(byte[] target_hash, boolean from_identity_hash) { // 107
    if (from_identity_hash) throw new IllegalStateException();
    byte[][] identity_data = known_destinations.get(Utils.toHex(target_hash));
    if (identity_data != null) {
      Identity identity = new Identity();
      identity.load_public_key(identity_data[2]);
      identity.app_data = identity_data[3];
      return identity;

    }
    throw new IllegalStateException();
  }

  static byte[] recall_app_data(byte[] destination_hash) { // 149
    byte[][] identity_data = known_destinations.get(Utils.toHex(destination_hash));
    if (identity_data == null) return null;
    return identity_data[3];
  }

  public static byte[] full_hash(byte[] data) { // 239
    return rns.Cryptography.sha256(data);
  }

  public static byte[] truncated_hash(byte[] data) { // 249
    return Arrays.copyOf(full_hash(data), Identity.TRUNCATED_HASHLENGTH / 8);
  }

  public static boolean validate_announce(Packet packet, boolean only_validate_signature) { // 391
    if (packet.packet_type != Packet.ANNOUNCE) throw new IllegalStateException();
    int keysize = KEYSIZE / 8;
    int ratchetsize = RATCHETSIZE / 8;
    int name_hash_len = NAME_HASH_LENGTH / 8;
    int random_hash_len = RANDOM_HASH_LENGTH / 8;
    int sig_len = SIGLENGTH / 8;
    byte[] destination_hash = packet.destination_hash;
    int i = 0;
    byte[] public_key = Arrays.copyOfRange(packet.data, i, i + keysize);
    i += keysize;
    byte[] name_hash = Arrays.copyOfRange(packet.data, i, i + name_hash_len);
    i += name_hash_len;
    byte[] random_hash = Arrays.copyOfRange(packet.data, i, i + random_hash_len);
    i += random_hash_len;
    byte[] ratchet = new byte[0];
    if (packet.context_flag == Packet.FLAG_SET) {
      ratchet = Arrays.copyOfRange(packet.data, i, i + ratchetsize);
      i += ratchetsize;
    }
    byte[] signature = Arrays.copyOfRange(packet.data, i, i + sig_len);
    i += sig_len;
    byte[] app_data = Arrays.copyOfRange(packet.data, i, packet.data.length);

    byte[] signed_data = Utils.concatenate(destination_hash, public_key, name_hash, random_hash, ratchet, app_data);

    Identity announced_identity = new Identity(false);
    announced_identity.load_public_key(public_key);

    if (!announced_identity.validate(signature, signed_data)) {
      Rns.log("Received invalid announce for " + Rns.prettyhexrep(destination_hash) + ": Invalid signature.");
      return false;
    }
    if (only_validate_signature) return true;
    byte[] hash_material = Utils.concatenate(name_hash, announced_identity.hash);
    byte[] expected_hash = Arrays.copyOfRange(rns.Identity.full_hash(hash_material), 0, rns.Reticulum.TRUNCATED_HASHLENGTH / 8);
    if (!Arrays.equals(destination_hash, expected_hash)) {
      Rns.log("Received invalid announce for " + Rns.prettyhexrep(destination_hash) + ": Destination mismatch.");
      return false;
    }
    remember(packet.get_hash(), destination_hash, public_key, app_data);
    return true;
  }

  public Identity(boolean create_keys) { // 549
    if (create_keys) throw new IllegalStateException();
  }

  public Identity() {
  }

  public byte[] get_public_key() { // 590
    return Utils.concatenate(pub_bytes, sig_pub_bytes);
  }

  void load_private_key(byte[] prv_bytes) { // 596
    this.prv_bytes = Arrays.copyOfRange(prv_bytes, 0, Identity.KEYSIZE / 8 / 2);
    this.sig_prv_bytes = Arrays.copyOfRange(prv_bytes, Identity.KEYSIZE / 8 / 2, prv_bytes.length);
    this.pub_bytes = new byte[0];
    this.sig_pub_bytes = new byte[0];
    update_hashes();
  }

  public void load_public_key(byte[] pub_bytes) { // 625
    int keysize = KEYSIZE / 8;
    this.pub_bytes = Arrays.copyOfRange(pub_bytes, 0, keysize / 2);
    this.sig_pub_bytes = Arrays.copyOfRange(pub_bytes, keysize / 2, keysize);
    update_hashes();
  }

  void update_hashes() { // 643
    hash = rns.Identity.truncated_hash(get_public_key());
    hexhash = Utils.toHex(hash).toLowerCase();
  }

  /**
   * Validates the signature of a signed message.
   * @param signature The signature to be validated as *bytes*.
   * @param message The message to be validated as *bytes*.
   * @return True if the signature is valid, otherwise False.
   * @throws NullPointerException *KeyError* if the instance does not hold a public key.
   */
  boolean validate(byte[] signature, byte[] message) {
    return SignatureUtils.verify25519(Objects.requireNonNull(sig_pub_bytes), signature, message);
  }

}
