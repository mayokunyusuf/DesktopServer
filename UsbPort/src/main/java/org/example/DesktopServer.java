package org.example;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.PrintWriter;
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

    public static void startServer(String purchaseAmount) throws IOException {
        final int PORT = 1430;

        // Initialize JmDNS
        JmDNS jmdns = JmDNS.create();
        ServiceInfo serviceInfo = ServiceInfo.create("_pos_terminal._tcp.local.", "POS Terminal", PORT, "POS Terminal Service");
        jmdns.registerService(serviceInfo);
        System.out.println("Service registered");

        // Start server
        ServerSocket serverSocket = new ServerSocket(PORT);
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface networkInterface = interfaces.nextElement();
            // Filters out 127.0.0.1 and inactive interfaces
            if (networkInterface.isLoopback() || !networkInterface.isUp())
                continue;
            Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress addr = addresses.nextElement();
                System.out.println(networkInterface.getDisplayName() + " " + addr.getHostAddress());
            }
        }
        System.out.println("Waiting for connection...");

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
    }
}
