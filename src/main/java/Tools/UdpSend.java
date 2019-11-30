package Tools;

import java.io.IOException;
import java.net.*;

public class UdpSend {

    public static void sendMessage(String message, SocketAddress socketAddress) {

            // convert the String input into the byte array.
            byte buf[] = message.getBytes();

        DatagramPacket DpSend = null;
        try {
            DpSend = new DatagramPacket(buf, buf.length, InetAddress.getLocalHost(), 9997);
            DpSend.setSocketAddress(socketAddress);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        DatagramSocket dp = null;
        try {
            dp = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }

        try {
            dp.send(DpSend);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("MESSAGE SENT");
    }

    public static void sendServer(String message) {

        // convert the String input into the byte array.
        byte buf[] = message.getBytes();

        DatagramPacket DpSend = null;
        try {
            DpSend = new DatagramPacket(buf, buf.length, InetAddress.getLocalHost(), 9997);

        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        DatagramSocket dp = null;
        try {
            dp = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }

        try {
            dp.send(DpSend);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("MESSAGE SENT");
    }

}
