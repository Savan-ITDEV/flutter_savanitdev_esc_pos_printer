package com.savanitdev.printer.flutter_savanitdev_printer;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.regex.Pattern;

import io.flutter.plugin.common.MethodChannel;

public class NetworkUtils {
    // Ping IP address with custom parameters for fast ping and get the average RTT
    public static void fastPingAndGetNetworkSpeed(String ipAddress, int timeout, @NonNull MethodChannel.Result result) {
        try (Socket socket = new Socket()) {
            long startTime = System.currentTimeMillis();

            // Attempt to connect to the remote host on the specified port
            socket.connect(new InetSocketAddress(ipAddress, 9100), timeout);

            long endTime = System.currentTimeMillis();
            long timeTaken = endTime - startTime;

            System.out.println("Ping successful, time taken: " + timeTaken + " ms");
            result.success(String.valueOf(timeTaken));
        } catch (IOException e) {
            System.out.println("Ping failed: " + e.getMessage());
            result.error("ERROR", "PING_FAIL","");
        }

    }
    public static String ping(String ipAddress, int timeout) {
        if(isValidIP(ipAddress)){
            try (Socket socket = new Socket()) {
                long startTime = System.currentTimeMillis();
                // Attempt to connect to the remote host on the specified port
                socket.connect(new InetSocketAddress(ipAddress, 9100), timeout);

                long endTime = System.currentTimeMillis();
                long timeTaken = endTime - startTime;

                System.out.println("Ping successful, time taken: " + timeTaken + " ms");
                return String.valueOf(timeTaken);
            } catch (IOException e) {
                System.out.println("Ping failed: " + e.getMessage());
                return "ERROR_PING";
            }
        }else{
            return "ping support only type internet :" + ipAddress;
        }
    }
    public static boolean isValidIP(String ip) {
        String regex = "^(25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9][0-9]?|0)(\\.(25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9][0-9]?|0)){3}$";
        return Pattern.matches(regex, ip);
    }
    // Parse the output of the ping command to get the network speed (RTT)
    private static void parsePingOutputForSpeed(String pingOutput, @NonNull MethodChannel.Result result) {
        String[] lines = pingOutput.split("\n");
        for (String line : lines) {
            // Find the line that contains "avg" for average RTT
            if (line.contains("avg")) {
                String[] parts = line.split("/");
                if (parts.length >= 5) {
                    // Get the average round-trip time (RTT) in ms
                    Log.d("ping device ========> ", "Average RTT: " + parts[4] + " ms");
                    result.success(parts[4]);
                }
            }
        }
        result.error("ERROR", "Ping fail please check your device","");
    }


    private boolean pingHost(String str,int timeout) {
        boolean result = false;
        BufferedReader bufferedReader = null;
        Process p = null;
        try {
            Thread.sleep(timeout);
            p = Runtime.getRuntime().exec("ping -c 1 -w 5 " + str);
            InputStream ins = p.getInputStream();
            InputStreamReader reader = new InputStreamReader(ins);
            bufferedReader = new BufferedReader(reader);
            Object var6 = null;
            while(true) {
                if (bufferedReader.readLine() == null) {
                    int status = p.waitFor();
                    if (status == 0) {
                        result = true;
                    } else {
                        result = false;
                    }
                    break;
                }
            }
        } catch (IOException var18) {
            result = false;
        } catch (InterruptedException var19) {
            result = false;
        } finally {
            if (p != null) {
                p.destroy();
            }
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException var17) {
                    IOException var17x = var17;
                    var17x.printStackTrace();
                }
            }

        }
        return result;
    }
}
