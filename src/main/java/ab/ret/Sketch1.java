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

package ab.ret;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Arrays;

public class Sketch1 {
  public static void main(String[] args) throws IOException {
    ServerSocketChannel serverSocket = ServerSocketChannel.open();
    serverSocket.bind(new InetSocketAddress(37428));
    SocketChannel socketChannel = serverSocket.accept();
    ByteBuffer buffer = ByteBuffer.allocate(256);
    while (socketChannel.read(buffer) > 0) {
      byte[] array = Arrays.copyOf(buffer.array(), buffer.position());
      System.out.println("Received: " + new String(array));
      buffer.rewind();
    }
    socketChannel.close();
  }
}
