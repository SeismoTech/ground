package org.seismotech.ground.net;

import java.net.InetSocketAddress;

public class AddressParsing {

  public static InetSocketAddress hostPort(String repr) {
    return hostPort(repr, null, -1);
  }

  public static InetSocketAddress hostPort(String repr,
      String defaultHost, int defaultPort) {
    int colon = repr.lastIndexOf(':');
    if (colon == -1) colon = repr.length();
    String host = repr.substring(0, colon);
    final String portRepr = repr.substring(Math.min(colon+1, repr.length()));
    if ("".equals(host)) {
      if (defaultHost == null) absentHostError(repr);
      host = defaultHost;
    }
    int port = 0;
    if ("".equals(portRepr)) {
      if (defaultPort == -1) absentPortError(repr);
      port = defaultPort;
    } else {
      try {
        port = Integer.parseInt(portRepr);
      } catch (IllegalArgumentException e) {
        badPortError(repr);
      }
    }
    return new InetSocketAddress(host, port);
  }

  private static void absentHostError(String repr) {
    throw new IllegalArgumentException("No host at address " + repr);
  }

  private static void absentPortError(String repr) {
    throw new IllegalArgumentException("No port at address " + repr);
  }

  private static void badPortError(String repr) {
    throw new IllegalArgumentException("Bad port at address " + repr);
  }
}
