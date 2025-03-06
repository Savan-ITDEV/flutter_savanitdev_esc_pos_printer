package com.savanitdev.printer.flutter_savanitdev_printer;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import io.flutter.plugin.common.MethodChannel;

public class NetworkUtils {
    public static void netDiscovery(Integer timeout, @NonNull MethodChannel.Result result) {
        new Thread(() -> {
            DatagramSocket socket = null;
            try {
                // Create a new DatagramSocket
                socket = new DatagramSocket(5001);
                socket.setSoTimeout(timeout); // Set the timeout for receiving data
                byte[] sendData = "ZY0001FIND".getBytes();

                // Create and send a broadcast UDP packet
                DatagramPacket sendPacket = new DatagramPacket(
                        sendData,
                        sendData.length,
                        InetAddress.getByName("255.255.255.255"),
                        1460
                );
                socket.send(sendPacket);

                // Prepare buffer for receiving data
                byte[] receiveData = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

                // List to hold discovered printers
                List<Map<String, String>> printersArray = new ArrayList<>();
                boolean listening = true;

                while (listening) {
                    try {
                        socket.receive(receivePacket); // Wait for a response
                        String receivedString = new String(receivePacket.getData(), 0, receivePacket.getLength());

                        // Create a map to store printer information
                        Map<String, String> printerInfo = new HashMap<>();
                        printerInfo.put("address", receivePacket.getAddress().toString());
                        printerInfo.put("name", receivedString);

                        // Add the printer info to the list
                        printersArray.add(printerInfo);
                    } catch (SocketTimeoutException e) {
                        listening = false; // Stop listening on timeout
                    } catch (IOException e) {
                        Log.e("PrinterDiscovery", "Error receiving packet", e);
                        result.error("IO_EXCEPTION", e.toString(), null);
                    }
                }

                result.success(printersArray);

            } catch (IOException e) {
                Log.e("PrinterDiscovery", "Error during discovery", e);
                result.error("DISCOVERY_ERROR", e.toString(), null);
            } finally {
                // Close the socket
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            }
        }).start();
    }
}
