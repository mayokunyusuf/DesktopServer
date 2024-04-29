package org.example;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import java.io.IOException;
import java.net.*;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Enumeration;

public class DesktopServer {

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.err.println("Usage: DesktopServer <purchase_amount>");
            System.exit(1);
        }

        String purchaseAmount = args[0];
        startServer(purchaseAmount);
    }

    public static void startServer(String purchaseAmount) {
        try {
            InetAddress localHost = findSuitableHost();
            if (localHost == null) {
                System.err.println("No suitable network interface found.");
                return;
            }

            ServerSocket serverSocket = new ServerSocket(0, 50, localHost);
            int port = serverSocket.getLocalPort();
            System.out.println("Server started on IP: " + localHost.getHostAddress() + " and port: " + port);

            JmDNS jmdns = JmDNS.create(localHost);
            ServiceInfo serviceInfo = ServiceInfo.create(
                    "_pos_terminal._tcp.", "POS Terminal", port, "POS Terminal Service"
            );
            jmdns.registerService(serviceInfo);
            System.out.println("Service registered");

            handleConnections(serverSocket, purchaseAmount);
        } catch (IOException e) {
            System.err.println("Failed to start server: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void handleConnections(ServerSocket serverSocket, String purchaseAmount) {
        try {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Connected to " + clientSocket.getInetAddress());

                // Send purchase amount to POS terminal
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                out.println(purchaseAmount);

                // Close the connection
                out.close();
                clientSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error during connection handling: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                serverSocket.close();
            } catch (IOException e) {
                System.err.println("Error closing server socket: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private static InetAddress findSuitableHost() throws SocketException {
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface networkInterface = interfaces.nextElement();
            if (networkInterface.isLoopback() || !networkInterface.isUp())
                continue;

            for (InetAddress address : Collections.list(networkInterface.getInetAddresses())) {
                if (address instanceof Inet4Address && !address.isLoopbackAddress()) {
                    return address;
                }
            }
        }
        return null;
    }
}
