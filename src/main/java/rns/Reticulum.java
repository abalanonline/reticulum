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

import ab.TcpForwarder;
import rns.interfaces.LocalInterface;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Properties;

public class Reticulum {

  public static final int TRUNCATED_HASHLENGTH = 128;
  public static final int HEADER_MINSIZE = 2 + 1 + (TRUNCATED_HASHLENGTH / 8) * 1;
  public static final int HEADER_MAXSIZE = 2 + 1 + (TRUNCATED_HASHLENGTH / 8) * 2;
  private final TcpForwarder tcpForwarder;
  private final LocalInterface.LocalClientInterface spawned_interface = new LocalInterface.LocalClientInterface();

  public Reticulum(String configpath) {
    Path propertiesPath = Paths.get(Optional.ofNullable(configpath).orElse(".")).resolve("reticulum.properties");
    Properties properties = new Properties();
    try {
      properties.load(Files.newInputStream(propertiesPath));
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    tcpForwarder = new TcpForwarder(Integer.parseInt(properties.get("local").toString()),
        properties.get("host").toString(), Integer.parseInt(properties.get("port").toString()));
    tcpForwarder.inbound = spawned_interface::receive;
    tcpForwarder.outbound = spawned_interface::receive;
    tcpForwarder.start();
    Transport.start(this);
  }

}
