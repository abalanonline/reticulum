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

import rns.Identity;
import rns.Packet;
import rns.interfaces.Hdlc;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

public class TcpForwarder {

  private int localPort;
  private String remoteHost;
  private int remotePort;

  public TcpForwarder(int localPort, String remoteHost, int remotePort) {
    this.localPort = localPort;
    this.remoteHost = remoteHost;
    this.remotePort = remotePort;
  }

  public void start() {
    try (ServerSocket serverSocket = new ServerSocket(localPort)) {
      System.out.println("Listening on port " + localPort);

      while (true) {
        Socket clientSocket = serverSocket.accept();
        System.out.println("Accepted connection from " + clientSocket.getInetAddress());

        Socket remoteSocket = new Socket(remoteHost, remotePort);
        System.out.println("Connected to remote " + remoteHost + ":" + remotePort);

        new Thread(new Forwarder(clientSocket, remoteSocket)).start();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static class Forwarder implements Runnable {
    private Socket clientSocket;
    private Socket remoteSocket;

    public Forwarder(Socket clientSocket, Socket remoteSocket) {
      this.clientSocket = clientSocket;
      this.remoteSocket = remoteSocket;
    }

    @Override
    public void run() {
      try {
        // Forward data from client to remote
        new Thread(() -> forwardData(clientSocket, remoteSocket)).start();
        // Forward data from remote to client
        forwardData(remoteSocket, clientSocket);
      } finally {
        try {
          clientSocket.close();
          remoteSocket.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }

    private void print(byte[] buffer, Socket destination) {
      buffer = Hdlc.decode(buffer);
      Packet packet = new Packet(buffer);
      if (packet.packetType == Packet.ANNOUNCE) Identity.validateAnnounce(packet);
      StringBuilder s = new StringBuilder();
      int flags = buffer[0] & 0xFF;
//      if (destination != remoteSocket) return;
      s.append(destination == remoteSocket ? "O" : "I");
      s.append(" ");
      //s.append(" IFAC");
      s.append(flags >> 7);
      if (flags >> 7 > 0) s.append(" IFAC "); //IFAC Flag
      //s.append(" Header");
      s.append(flags >> 6 & 1);
      //s.append(" Context");
      s.append(flags >> 5 & 1);
      //s.append(" Propagation");
      s.append(flags >> 4 & 1);
      s.append(" ");
      //s.append(" Destination");
      s.append(flags >> 2 & 3);
      //s.append(" Packet");
      s.append(flags & 3);
      s.append(" ");
      //s.append(" Hops");
      s.append(buffer[1]);
      s.append(" length");
      s.append(buffer.length);

      s.append("\n  ");
      s.append(toHex(buffer));
      //s.append(" ").append(new String(buffer));
      s.append("\n");
      System.out.print(s);
    }

    private void forwardData(Socket source, Socket destination) {
      try (InputStream in = source.getInputStream();
           OutputStream out = destination.getOutputStream()) {
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = in.read(buffer)) != -1) {
          print(Arrays.copyOf(buffer, bytesRead), destination);
          out.write(buffer, 0, bytesRead);
          out.flush();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public static String toHex(byte[] bytes) {
    StringBuilder s = new StringBuilder();
    for (byte b : bytes) s.append(String.format("%02X", b));
    return s.toString();
  }

  public static void main(String[] args) {
    if (args.length != 3) {
      System.err.println("Usage: java TcpForwarder <localPort> <remoteHost> <remotePort>");
      System.exit(1);
    }

    int localPort = Integer.parseInt(args[0]);
    String remoteHost = args[1];
    int remotePort = Integer.parseInt(args[2]);

    TcpForwarder forwarder = new TcpForwarder(localPort, remoteHost, remotePort);
    forwarder.start();
  }
}
