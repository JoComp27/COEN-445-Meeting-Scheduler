package Tools;

import java.io.IOException;
import java.net.*;

public class UdpSend {

    public static void sendMessage(String message, DatagramSocket senderSocket, SocketAddress socketAddress) {

            // convert the String input into the byte array.
            byte buf[] = message.getBytes();

        DatagramPacket DpSend = null;
        DpSend = new DatagramPacket(buf, buf.length);
        DpSend.setSocketAddress(socketAddress);

        try {
            senderSocket.send(DpSend);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("SERVER SENT");
    }

}
