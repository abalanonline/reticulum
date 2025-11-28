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

import rns.interfaces.LocalInterface;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Transport {

  static Set<AnnounceHandler> announce_handlers = new HashSet<>(); // 110
  static Identity identity; // 164

  public static void start(Reticulum reticulum_instance) { // 167
    if (identity == null) {
      identity = new Identity();
      try {
        identity.load_private_key(Files.readAllBytes(Paths.get("../../.reticulum/storage/transport_identity")));
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    }

  }

  public void inbound(byte[] raw, LocalInterface.LocalClientInterface anInterface) { // 1151
    // If the interface does not have IFAC enabled,
    // check the received packet IFAC flag.
    // If the flag is set, drop the packet
    if ((raw[0] & 0x80) == 0x80) return;
    if (identity == null) return;
    Packet packet = new Packet(null, raw);
    if (!packet.unpack()) return;
    if (packet.packet_type == rns.Packet.ANNOUNCE) {
      if (!rns.Identity.validate_announce(packet, true)) throw new IllegalStateException();
      rns.Identity.validate_announce(packet, false);
      Identity announce_identity = rns.Identity.recall(packet.destination_hash, false);
      // 1792
      for (AnnounceHandler handler : announce_handlers) {
        byte[] handler_expected_hash = rns.Destination.hash_from_name_and_identity(handler.aspect_filter(), announce_identity);
        boolean execute_callback = Arrays.equals(packet.destination_hash, handler_expected_hash);
        if (execute_callback) {
          handler.received_announce(packet.destination_hash, announce_identity,
              rns.Identity.recall_app_data(packet.destination_hash), packet.packet_hash,
              packet.context == rns.Packet.PATH_RESPONSE);
        }
      }

    }
  }

  public interface AnnounceHandler {
    default void received_announce(byte[] destination_hash, Identity announced_identity, byte[] app_data) {}
    default void received_announce(byte[] destination_hash, Identity announced_identity, byte[] app_data,
        byte[] announce_packet_hash) {
      received_announce(destination_hash, announced_identity, app_data);
    }
    default void received_announce(byte[] destination_hash, Identity announced_identity, byte[] app_data,
        byte[] announce_packet_hash, boolean is_path_response) {
      received_announce(destination_hash, announced_identity, app_data, announce_packet_hash);
    }
    default boolean receive_path_responses() {
      return false;
    }
    String aspect_filter();
  }

  /**
   * Registers an announce handler.
   * @param handler Must be an object with an *aspect_filter* attribute and a
   *   *received_announce(destination_hash, announced_identity, app_data)* or
   *   *received_announce(destination_hash, announced_identity, app_data, announce_packet_hash)* or
   *   *received_announce(destination_hash, announced_identity, app_data, announce_packet_hash, is_path_response)*
   *   callable. Can optionally have a *receive_path_responses* attribute set to ``True``, to also receive all path
   *   responses, in addition to live announces. See the :ref:`Announce Example<example-announce>` for more info.
   */
  public static void register_announce_handler(AnnounceHandler handler) { // 2167
    announce_handlers.add(handler);
  }

}
