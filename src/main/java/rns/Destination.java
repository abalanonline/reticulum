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

public class Destination {
  // types
  public static final int SINGLE = 0;
  public static final int GROUP  = 1;
  public static final int PLAIN  = 2;
  public static final int LINK   = 3;
  // proof_strategies
  public static final int PROVE_NONE = 0x21;
  public static final int PROVE_APP  = 0x22;
  public static final int PROVE_ALL  = 0x23;
  // directions
  public static final int IN  = 0x11;
  public static final int OUT = 0x12;

  Identity identity;
  int direction;
  int type;
  String app_name;
  String[] aspects;
  public byte[] hash;
  public String name;

  public Destination(Identity identity, int direction, int type, String app_name, String... aspects) {
    this.identity = identity;
    this.direction = direction;
    this.type = type;
    this.app_name = app_name;
    this.aspects = aspects;
  }

  public void set_proof_strategy(int proof_strategy) {

  }

  public void announce(byte[] app_data) {

  }

  static String expand_name(Identity identity, String app_name, String[] aspects) { // 96
    StringBuilder name = new StringBuilder();
    if (app_name.indexOf('.') >= 0) throw new IllegalStateException("Dots can't be used in app names");
    name.append(app_name);
    for (String aspect : aspects) {
      if (aspect.indexOf('.') >= 0) throw new IllegalStateException("Dots can't be used in aspects");
      name.append('.').append(aspect);
    }
    if (identity != null) name.append('.').append(identity.hexhash);
    return name.toString();
  }

  static byte[] hash(Identity identity, String app_name, String[] aspects) { // 116
    byte[] fullHash = Identity.full_hash(Destination.expand_name(null, app_name, aspects).getBytes());
    byte[] name_hash = Arrays.copyOf(fullHash, Identity.NAME_HASH_LENGTH / 8);
    byte[] addr_hash_material = name_hash;
    if (identity != null) addr_hash_material = Utils.concatenate(addr_hash_material, identity.hash);
    return Arrays.copyOf(rns.Identity.full_hash(addr_hash_material), rns.Reticulum.TRUNCATED_HASHLENGTH / 8);
  }

  public static String[] app_and_aspects_from_name(String full_name) { // 133
    return full_name.split("\\.");
  }

  public static byte[] hash_from_name_and_identity(String full_name, Identity identity) { // 141
    String[] appNameAspects = app_and_aspects_from_name(full_name);
    return Destination.hash(identity, appNameAspects[0], Arrays.copyOfRange(appNameAspects, 1, appNameAspects.length));
  }

}
