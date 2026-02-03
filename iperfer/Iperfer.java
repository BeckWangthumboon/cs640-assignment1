import java.io.*;
import java.net.*;

public class Iperfer {

    public static void main(String[] args) {
        // Check for a mode flag and args
        if (args.length == 0) {
            System.out.println("Error: missing or additional arguments");
            System.exit(1);
        }
        String mode = args[0];
        int port = 0;
        String host = "";
        double time = 0;

        try {
            // Parse CLA
            if (mode.equals("-c")) {
                if (args.length != 7) {
			throw new Exception();
                }
                host = args[2];
                port = Integer.parseInt(args[4]);
                time = Double.parseDouble(args[6]);
                
            } else if (mode.equals("-s")) {
                if (args.length != 3) {
			throw new Exception();
                }
		port = Integer.parseInt(args[2]);
                
            } else {
                // Invalid mode / args
                throw new Exception();
            }

            // Check for valid port range
            if (port < 1024 || port > 65535) {
                System.out.println("Error: port number must be in the range 1024 to 65535");
                System.exit(1);
            }

            // Launch correct method
            if (mode.equals("-c")) {
                runClient(host, port, time);
            } else {
                runServer(port);
            }

        } catch (Exception e) {
            System.out.println("Error: missing or additional arguments");
            System.exit(1);
        }
    }

    public static void runClient(String host, int port, double duration) {
        try {
            Socket clientSocket = new Socket(host, port);
            OutputStream outStream = clientSocket.getOutputStream();

            // Set byte chunks of all zeros
            byte[] packet = new byte[1000];
            long bytesSent = 0;
            
            // run for the specified number of seconds
            long stopTime = System.currentTimeMillis() + (long)(duration * 1000);
            while (System.currentTimeMillis() < stopTime) {
                outStream.write(packet);
                bytesSent += 1000;
            }
            
            clientSocket.close();

            // Calculate statistics
            double sentKB = bytesSent / 1000.0;
            double sentMB = sentKB / 1000.0;
            double rateMbps = (sentMB * 8) / duration;

            System.out.println("sent=" + (long)sentKB + " KB rate=" + String.format("%.3f", rateMbps) + " Mbps");

        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    public static void runServer(int port) {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            Socket clientSocket = serverSocket.accept();
            InputStream inStream = clientSocket.getInputStream();

            byte[] buffer = new byte[1000];
            long bytesRecv = 0;
            int numRead;
            long start = 0;
            long end = 0;
            boolean firstPacket = true;

            while ((numRead = inStream.read(buffer)) != -1) {
                if (firstPacket) {
                    start = System.currentTimeMillis();
                    firstPacket = false;
                }
                bytesRecv += numRead;
                end = System.currentTimeMillis();
            }
            
            clientSocket.close();
            serverSocket.close();

            // Calculate statistics
            double recvKB = bytesRecv / 1000.0;
            double recvMB = recvKB / 1000.0;
            double timeSec = (end - start) / 1000.0;
            
            double rateMbps = (recvMB * 8) / timeSec;

            System.out.println("received=" + (long)recvKB + " KB rate=" + String.format("%.3f", rateMbps) + " Mbps");

        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
