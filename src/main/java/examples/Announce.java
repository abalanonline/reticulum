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

package examples;

import rns.Identity;
import rns.Rns;
import rns.Transport;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;

/**
 * https://github.com/markqvist/Reticulum/blob/master/Examples/Announce.py
 * This RNS example demonstrates setting up announce
 * callbacks, which will let an application receive a
 * notification when an announce relevant for it arrives
 */
public class Announce {
  // Let's define an app name. We'll use this for all
  // destinations we create. Since this basic example
  // is part of a range of example utilities, we'll put
  // them all within the app namespace "example_utilities"
  public static final String APP_NAME = "example_utilities";

  // We initialise two lists of strings to use as app_data
  public static final String[] fruits = {"Peach", "Quince", "Date", "Tangerine", "Pomelo", "Carambola", "Grape"};
  public static final String[] noble_gases = {"Helium", "Neon", "Argon", "Krypton", "Xenon", "Radon", "Oganesson"};

  // This initialisation is executed when the program is started
  static void program_setup(String configpath) {
    // We must first initialise Reticulum
    rns.Reticulum reticulum = new rns.Reticulum(configpath);

    // Randomly create a new identity for our example
    rns.Identity identity = new rns.Identity();

    // Using the identity we just created, we create two destinations
    // in the "example_utilities.announcesample" application space.
    //
    // Destinations are endpoints in Reticulum, that can be addressed
    // and communicated with. Destinations can also announce their
    // existence, which will let the network know they are reachable
    // and automatically create paths to them, from anywhere else
    // in the network.
    rns.Destination destination_1 = new rns.Destination(
        identity,
        rns.Destination.IN,
        rns.Destination.SINGLE,
        APP_NAME,
        "announcesample",
        "fruits"
    );

    rns.Destination destination_2 = new rns.Destination(
        identity,
        rns.Destination.IN,
        rns.Destination.SINGLE,
        APP_NAME,
        "announcesample",
        "noble_gases"
    );

    // We configure the destinations to automatically prove all
    // packets addressed to it. By doing this, RNS will automatically
    // generate a proof for each incoming packet and transmit it
    // back to the sender of that packet. This will let anyone that
    // tries to communicate with the destination know whether their
    // communication was received correctly.
    destination_1.set_proof_strategy(rns.Destination.PROVE_ALL);
    destination_2.set_proof_strategy(rns.Destination.PROVE_ALL);

    // We create an announce handler and configure it to only ask for
    // announces from "example_utilities.announcesample.fruits".
    // Try changing the filter and see what happens.
    ExampleAnnounceHandler announce_handler = new ExampleAnnounceHandler(
        "example_utilities.announcesample.fruits"
    );

    // We register the announce handler with Reticulum
    rns.Transport.register_announce_handler(announce_handler);

    // Everything's ready!
    // Let's hand over control to the announce loop
    announceLoop(destination_1, destination_2);
  }

  static void announceLoop(rns.Destination destination_1, rns.Destination destination_2) {
    // Let the user know that everything is ready
    Rns.log("Announce example running, hit enter to manually send an announce (Ctrl-C to quit)");

    // We enter a loop that runs until the users exits.
    // If the user hits enter, we will announce our server
    // destination on the network, which will let clients
    // know how to create messages directed towards it.
    Random random = ThreadLocalRandom.current();
    while (true) {
      try { System.in.read(); } catch (IOException ignore) {}

      // Randomly select a fruit
      String fruit = fruits[random.nextInt(fruits.length)];

      // Send the announce including the app data
      destination_1.announce(fruit.getBytes());
      Rns.log(
          "Sent announce from "+
          Rns.prettyhexrep(destination_1.hash)+
          " ("+destination_1.name+")"
      );

      // Randomly select a noble gas
      String noble_gas = noble_gases[random.nextInt(noble_gases.length)];

      // Send the announce including the app data
      destination_2.announce(noble_gas.getBytes());
      Rns.log(
          "Sent announce from "+
          Rns.prettyhexrep(destination_2.hash)+
          " ("+destination_2.name+")"
      );
    }
  }

  // We will need to define an announce handler class that
  // Reticulum can message when an announce arrives.
  public static class ExampleAnnounceHandler implements Transport.AnnounceHandler {
    // The initialisation method takes the optional
    // aspect_filter argument. If aspect_filter is set to
    // None, all announces will be passed to the instance.
    // If only some announces are wanted, it can be set to
    // an aspect string.

    String aspect_filter;

    public ExampleAnnounceHandler(String aspect_filter) {
      this.aspect_filter = aspect_filter;
    }

    @Override
    public String aspect_filter() {
      return aspect_filter;
    }

    // This method will be called by Reticulums Transport
    // system when an announce arrives that matches the
    // configured aspect filter. Filters must be specific,
    // and cannot use wildcards.
    @Override
    public void received_announce(byte[] destination_hash, Identity announced_identity, byte[] app_data) {
      Rns.log(
          "Received an announce from "+
          Rns.prettyhexrep(destination_hash)
      );

      if (app_data != null) Rns.log("The announce contained the following app data: " + new String(app_data));
    }
  }

  //////////////////////////////////////////////////////////
  //// Program Startup /////////////////////////////////////
  //////////////////////////////////////////////////////////

  /**
   * This part of the program gets run at startup,
   * and parses input from the user, and then starts
   * the desired program mode.
   */
  public static void main(String[] args) {
    if (args.length > 0 && (args.length != 2 || !"--config".equals(args[0]))) {
      System.out.println("usage: Announce.py [-h] [--config CONFIG]\n\n" +
          "Reticulum example that demonstrates announces and announce handlers\n\n" +
          "options:\n" +
          "  -h, --help       show this help message and exit\n" +
          "  --config CONFIG  path to alternative Reticulum config directory");
      System.exit(1);
    }
    String configarg = args.length > 1 ? args[1] : null;
    program_setup(configarg);
  }
}
