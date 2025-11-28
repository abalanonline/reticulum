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

import java.util.logging.Level;
import java.util.logging.Logger;

public class Rns {

  static {
    //System.getProperties().setProperty("java.util.logging.SimpleFormatter.format", "%1$tT %4$s: %5$s [%2$s]%6$s%n");
    System.getProperties().setProperty("java.util.logging.SimpleFormatter.format", "%1$tT: %5$s%6$s%n");
  }

  public static void log(String s) {
    Logger.getAnonymousLogger().info(s);
  }

  public static void logError(String s) {
    Logger.getAnonymousLogger().severe(s);
  }

  public static void trace_exception(Exception e) {
    Logger.getAnonymousLogger().log(Level.SEVERE, "", e);
  }

  public static String prettyhexrep(byte[] data) {
    return "<" + Utils.toHex(data).toLowerCase() + ">";
  }

}
